package com.jack.yupaobackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.yupaobackend.common.ErrorCode;
import com.jack.yupaobackend.domain.User;
import com.jack.yupaobackend.exception.BusinessException;
import com.jack.yupaobackend.mapper.UserMapper;
import com.jack.yupaobackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}




