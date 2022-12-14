package com.jack.yupaobackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jack.yupaobackend.common.BaseResponse;
import com.jack.yupaobackend.common.ErrorCode;
import com.jack.yupaobackend.common.ResultUtils;
import com.jack.yupaobackend.exception.BusinessException;
import com.jack.yupaobackend.model.domain.Team;
import com.jack.yupaobackend.model.dto.TeamQuery;
import com.jack.yupaobackend.model.request.TeamAddRequest;
import com.jack.yupaobackend.model.request.TeamJoinRequest;
import com.jack.yupaobackend.model.request.TeamUpdateRequest;
import com.jack.yupaobackend.model.vo.TeamUserVo;
import com.jack.yupaobackend.service.TeamService;
import com.jack.yupaobackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/team")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class TeamController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        long teamId = teamService.addTeam(teamAddRequest, request);
        return ResultUtils.success(teamId);
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if (teamUpdateRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean result = teamService.updateTeam(teamUpdateRequest, request);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "???????????????");
        return ResultUtils.success(true);
    }

    /**
     * ??????id??????????????????
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if (id <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Team team = teamService.getById(id);
        if (team == null) throw new BusinessException(ErrorCode.NULL_ERROR);
        return ResultUtils.success(team);
    }

    /**
     * ??????????????????
     * @param teamQuery
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        List<TeamUserVo> teamUserVoList = teamService.listTeams(teamQuery, request);
        return ResultUtils.success(teamUserVoList);
    }

    /**
     * ????????????
     * @param teamQuery
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if (teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    /**
     * ??????????????????
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if (teamJoinRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean result = teamService.joinTeam(teamJoinRequest, request);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "????????????");
        return ResultUtils.success(true);
    }

    /**
     * ??????????????????
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody Long id, HttpServletRequest request){
        if (id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.quitTeam(id, request);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "????????????");
        return ResultUtils.success(true);
    }

    /**
     * ??????????????????
     * @param id
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Long id, HttpServletRequest request){
        if (id == null || id <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean result = teamService.deleteTeam(id, request);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "?????????????????????");
        return ResultUtils.success(true);
    }

}
