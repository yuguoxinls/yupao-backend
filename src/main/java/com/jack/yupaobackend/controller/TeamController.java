package com.jack.yupaobackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jack.yupaobackend.common.BaseResponse;
import com.jack.yupaobackend.common.ErrorCode;
import com.jack.yupaobackend.common.ResultUtils;
import com.jack.yupaobackend.exception.BusinessException;
import com.jack.yupaobackend.model.domain.Team;
import com.jack.yupaobackend.model.dto.TeamQuery;
import com.jack.yupaobackend.service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody Team team){
        if (team == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean result = teamService.save(team);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存失败！");
        return ResultUtils.success(team.getId());
    }

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(long id){
        if (id <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean result = teamService.removeById(id);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败！");
        return ResultUtils.success(true);
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team){
        if (team == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean result = teamService.updateById(team);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败！");
        return ResultUtils.success(true);
    }

    /**
     * 根据id查询单条数据
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
     * 查询多条数据
     * @param teamQuery
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeams(TeamQuery teamQuery){
        if (teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>(team);
        List<Team> teamList = teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询
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

}
