package com.jack.yupaobackend.controller;

import com.jack.yupaobackend.model.domain.User;
import com.jack.yupaobackend.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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