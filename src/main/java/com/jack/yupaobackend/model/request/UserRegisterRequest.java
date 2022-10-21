package com.jack.yupaobackend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 *
 * @author jack
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -3817846057855891000L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
