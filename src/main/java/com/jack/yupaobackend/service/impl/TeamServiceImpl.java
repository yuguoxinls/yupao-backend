package com.jack.yupaobackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.yupaobackend.common.ErrorCode;
import com.jack.yupaobackend.exception.BusinessException;
import com.jack.yupaobackend.mapper.TeamMapper;
import com.jack.yupaobackend.model.domain.Team;
import com.jack.yupaobackend.model.domain.User;
import com.jack.yupaobackend.model.domain.UserTeam;
import com.jack.yupaobackend.model.enums.TeamStatusEnum;
import com.jack.yupaobackend.model.request.TeamAddRequest;
import com.jack.yupaobackend.service.TeamService;
import com.jack.yupaobackend.service.UserService;
import com.jack.yupaobackend.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

/**
* @author yuguoxin
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2022-10-21 15:54:35
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {
    @Autowired
    private UserService userService;
    @Autowired
    private UserTeamService userTeamService;

    @Override
    @Transactional
    public long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request) {
        //1. 请求参数是否为空？
        if (teamAddRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //2. 是否登录，未登录不允许创建
        User loginUser = userService.currentUser(request);
        if (loginUser == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录");
        final Long userId = loginUser.getId();
        //3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        Integer maxNum = teamAddRequest.getMaxNum();
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = teamAddRequest.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名字不满足要求");
        }
        //   3. 描述 <= 512
        String description = teamAddRequest.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
        }
        //   4. status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(teamAddRequest.getStatus()).orElse(0);
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (enumByValue == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = teamAddRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(enumByValue)){
            if (StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不满足要求");
            }
        }
        //   6. 超时时间 > 当前时间
        // TODO: 2022/10/21 这里的两个时间没搞懂
        Date expireTime = teamAddRequest.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间不满足要求");
        }
        //   7. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getUserId, userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍表 **4和5要保证同时对表操作，也就是说成功都成功，失败都失败，因此要用到事务**
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //5. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }
}




