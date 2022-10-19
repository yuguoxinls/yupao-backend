package com.jack.yupaobackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jack.yupaobackend.common.ErrorCode;
import com.jack.yupaobackend.common.ResultUtils;
import com.jack.yupaobackend.domain.User;
import com.jack.yupaobackend.exception.BusinessException;
import com.jack.yupaobackend.mapper.UserMapper;
import com.jack.yupaobackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jack.yupaobackend.constant.UserConstant.ADMIN_ROLE;
import static com.jack.yupaobackend.constant.UserConstant.USER_LOGIN_STATE;


/**
* @author yuguoxin
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2022-09-24 15:37:52
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    private static final String Salt = "yupi";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length()<4 || userPassword.length()<8 || checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (planetCode.length()>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String validPattern = "^.*[/^/$/.//,;:'!@#%&/*/|/?/+/(/)/[/]/{/}]+.*$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //账户不能重复
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);

        long count = this.count(queryWrapper);
        if (count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //星球编号不能重复
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPlanetCode, planetCode);

        count = this.count(queryWrapper);
        if (count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 对密码进行加密
        String handledPassword = DigestUtils.md5DigestAsHex((Salt + userPassword).getBytes());
        //3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(handledPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        return user.getId();
        long result = user.getId();
        return result;
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1. 检验信息是否合法
        if (StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (userAccount.length()<4 || userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String validPattern = "^.*[/^/$/.//,;:'!@#%&/*/|/?/+/(/)/[/]/{/}]+.*$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 校验密码是否输入正确，要和数据库中的密文密码去对比
        String handledPassword = DigestUtils.md5DigestAsHex((Salt + userPassword).getBytes());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String password = user.getUserPassword();
        if (!password.equals(handledPassword)){
            log.info("login failed, useAccount does not match userPassword!");
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3. 用户信息脱敏，隐藏敏感信息，防止数据库中的字段泄露
        User safetyUser = getSafetyUser(user);
        //4. 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        //5. 返回脱敏后的用户信息
        return safetyUser;
    }

    @Override
    public User currentUser(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null){
            return null;
        }
        //TODO 校验用户是否合法
        Long id = currentUser.getId();
        User user = this.getById(id);
        String planetCode = user.getPlanetCode();
        if (StringUtils.isBlank(planetCode)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return this.getSafetyUser(user);
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    public User getSafetyUser(User originUser){
        if (originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> searchByTags(List<String> tagsList) {
        if (tagsList == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 这里有2种方式：SQL查询；内存查询
        // 1. SQL查询
        /*LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        for (String tags : tagsList) {
            queryWrapper = queryWrapper.like(User::getTags, tags);
        }
        List<User> userList = this.list(queryWrapper);
        if (userList == null) throw new BusinessException(ErrorCode.NULL_ERROR);
        List<User> safetyUserList = new ArrayList<>();
        for (User user : userList) {
            User safetyUser = getSafetyUser(user);
            safetyUserList.add(safetyUser);
        }
        return safetyUserList;*/
        // 2. 内存查询(将所有数据一次性加载内存中)
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<User> userList = this.list(lambdaQueryWrapper); // 查询所有用户
        Gson gson = new Gson(); // JSON序列化包
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags(); // 获得用户的标签
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType()); // 使用fromJson将json转换为java对象
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>()); // 判空
            for (String tagName : tagsList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request){
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public int updateUser(User user, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 如果当前登录的用户是管理员，它可以修改信息
        // 如果当前登录的用户和被修改信息的用户是同一个人，可以修改信息
        if (!isAdmin(request) && (!Objects.equals(loginUser.getId(), user.getId()))) throw new BusinessException(ErrorCode.NO_AUTH);
        boolean b = this.updateById(user);
        if (!b) return -1;
        return 0;
    }


    @Override
    public Page<User> recommendUsers(int pageSize, int pageNum, HttpServletRequest request) {
        User currentUser = this.currentUser(request);
        String redisKey = String.format("yupao:user:recommend:%s", currentUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> pageInfo = (Page<User>) valueOperations.get(redisKey);
        // 如果缓存里有，从缓存里读数据
        if (pageInfo != null) return pageInfo;
        // 没有才查数据库
        pageInfo = new Page<>(pageSize, pageNum); // 第一个参数表示当前是第几页，第二个参数表示一页内有几条数据
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        Page<User> userPage = this.page(pageInfo, queryWrapper);
        // 查完数据库，写到缓存里
        try {
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis save error: " + e);
        }
        return userPage;
    }
}




