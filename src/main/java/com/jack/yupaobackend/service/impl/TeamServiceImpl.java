package com.jack.yupaobackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.yupaobackend.mapper.TeamMapper;
import com.jack.yupaobackend.model.domain.Team;
import com.jack.yupaobackend.service.TeamService;
import org.springframework.stereotype.Service;

/**
* @author yuguoxin
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2022-10-21 15:54:35
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

}




