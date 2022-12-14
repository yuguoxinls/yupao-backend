package com.jack.yupaobackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jack.yupaobackend.model.domain.Team;
import com.jack.yupaobackend.model.dto.TeamQuery;
import com.jack.yupaobackend.model.request.TeamAddRequest;
import com.jack.yupaobackend.model.request.TeamJoinRequest;
import com.jack.yupaobackend.model.request.TeamUpdateRequest;
import com.jack.yupaobackend.model.vo.TeamUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author yuguoxin
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2022-10-21 15:54:35
*/
public interface TeamService extends IService<Team> {

    long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request);

    List<TeamUserVo> listTeams(TeamQuery teamQuery, HttpServletRequest request);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);

    boolean quitTeam(Long id, HttpServletRequest request);

    boolean deleteTeam(Long id, HttpServletRequest request);
}
