package com.jack.yupaobackend.once;

import com.jack.yupaobackend.domain.User;
import com.jack.yupaobackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class InsertUsersTest {
    @Resource
    private UserService userService;


    @Test
    void insertUser() {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 500000; i++) {
            User user = new User();
            user.setUsername("test");
            user.setUserAccount("test");
            user.setAvatarUrl("https://th.bing.com/th/id/OIP.JPaFw0vH2f6Qy44aUfZ4jgAAAA?pid=ImgDet&rs=1");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("10");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList);
    }
}