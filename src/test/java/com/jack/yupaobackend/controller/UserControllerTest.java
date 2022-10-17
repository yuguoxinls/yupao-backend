package com.jack.yupaobackend.controller;

import com.jack.yupaobackend.domain.User;
import com.jack.yupaobackend.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {
    @Resource
    private UserService userService;

    @Test
    void searchByTags() {
        List<String> tagsList = new ArrayList<>();
        tagsList.add("python");
        tagsList.add("java");
        List<User> users = userService.searchByTags(tagsList);
        Assert.assertNotNull(users);
    }
}