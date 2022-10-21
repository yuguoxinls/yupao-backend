package com.jack.yupaobackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jack.yupaobackend.model.domain.Team;
import com.jack.yupaobackend.model.request.TeamAddRequest;

import javax.servlet.http.HttpServletRequest;

/**
* @author yuguoxin
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2022-10-21 15:54:35
*/
public interface TeamService extends IService<Team> {

    long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request);
}
