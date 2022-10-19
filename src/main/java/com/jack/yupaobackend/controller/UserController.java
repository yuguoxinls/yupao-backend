package com.jack.yupaobackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jack.yupaobackend.common.BaseResponse;
import com.jack.yupaobackend.common.ErrorCode;
import com.jack.yupaobackend.common.ResultUtils;
import com.jack.yupaobackend.domain.User;
import com.jack.yupaobackend.domain.request.UserLoginRequest;
import com.jack.yupaobackend.domain.request.UserRegisterRequest;
import com.jack.yupaobackend.exception.BusinessException;
import com.jack.yupaobackend.service.UserService;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.jack.yupaobackend.constant.UserConstant.ADMIN_ROLE;
import static com.jack.yupaobackend.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null){
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)){
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null){
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)){
            return null;
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request){
        boolean flag = userService.isAdmin(request);
        if (!flag){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)){
            queryWrapper.like(User::getUsername, username);
        }
        List<User> list = userService.list(queryWrapper);
        List<User> result = list.stream().map(user -> {
            user.setUserPassword(null);
            return user;
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    /**
     * 首页信息分页查询
     * @param pageSize 8
     * @param pageNum 1
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(@RequestParam int pageSize, @RequestParam int pageNum, HttpServletRequest request){
        Page<User> userPage = userService.recommendUsers(pageNum, pageSize);

        return ResultUtils.success(userPage);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody int id, HttpServletRequest request){
        boolean flag = userService.isAdmin(request);
        if (!flag){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @GetMapping("/current")
    public BaseResponse<User> currentUser(HttpServletRequest request){
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null){
            return null;
        }
        //TODO 校验用户是否合法
        Long id = currentUser.getId();
        User user = userService.getById(id);
        String planetCode = user.getPlanetCode();
        if (StringUtils.isBlank(planetCode)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);

    }

    @GetMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }



    /**
     * 根据标签列表查询拥有该标签的所有用户
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags") // TODO: 2022/10/18 根据标签搜索用户，前端会卡住
    public BaseResponse<List<User>> searchByTags(@RequestParam List<String> tagNameList, HttpServletRequest request){
        boolean flag = userService.isAdmin(request);
        if (!flag) throw new BusinessException(ErrorCode.NO_AUTH);
        List<User> users = userService.searchByTags(tagNameList);
        if (users == null) return ResultUtils.error(ErrorCode.NULL_ERROR);
        return ResultUtils.success(users);
    }

    /**
     * 更新用户信息
     * @param user 前端传过来的更改后的用户信息
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        if (user == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        int r = userService.updateUser(user, request);
        return ResultUtils.success(r);
    }



}
