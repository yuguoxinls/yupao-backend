package com.jack.yupaobackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.yupaobackend.mapper.UserTeamMapper;
import com.jack.yupaobackend.model.domain.UserTeam;
import com.jack.yupaobackend.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author yuguoxin
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2022-10-21 20:13:26
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




