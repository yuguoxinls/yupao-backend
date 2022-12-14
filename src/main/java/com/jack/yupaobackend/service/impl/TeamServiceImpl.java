package com.jack.yupaobackend.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.yupaobackend.common.ErrorCode;
import com.jack.yupaobackend.exception.BusinessException;
import com.jack.yupaobackend.mapper.TeamMapper;
import com.jack.yupaobackend.model.domain.Team;
import com.jack.yupaobackend.model.domain.User;
import com.jack.yupaobackend.model.domain.UserTeam;
import com.jack.yupaobackend.model.dto.TeamQuery;
import com.jack.yupaobackend.model.enums.TeamStatusEnum;
import com.jack.yupaobackend.model.request.TeamAddRequest;
import com.jack.yupaobackend.model.request.TeamJoinRequest;
import com.jack.yupaobackend.model.request.TeamUpdateRequest;
import com.jack.yupaobackend.model.vo.TeamUserVo;
import com.jack.yupaobackend.model.vo.UserVo;
import com.jack.yupaobackend.service.TeamService;
import com.jack.yupaobackend.service.UserService;
import com.jack.yupaobackend.service.UserTeamService;
import jodd.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = teamAddRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(enumByValue)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不满足要求");
            }
        }
        //   6. 超时时间 > 当前时间
        // TODO: 2022/10/21 这里的两个时间没搞懂
        Date expireTime = teamAddRequest.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间不满足要求");
        }
        //   7. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getUserId, userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍表 **4和5要保证同时对表操作，也就是说成功都成功，失败都失败，因此要用到事务**
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //5. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //1. 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>(); // TODO: 2022/10/23 查询条件拼接有误
        Long teamId = teamQuery.getId();
        queryWrapper.eq(teamId != null && teamId > 0, Team::getId, teamId);

        String name = teamQuery.getName();
        queryWrapper.like(StringUtils.isNotBlank(name), Team::getName, name);

        String description = teamQuery.getDescription();
        queryWrapper.like(StringUtils.isNotBlank(description), Team::getDescription, description);

        // 可以通过某个**关键词**同时对名称和描述查询
        String searchText = teamQuery.getSearchText();
        queryWrapper.and(StringUtils.isNotBlank(searchText), qw -> qw.like(Team::getName, searchText).or().like(Team::getDescription, searchText));

        Integer maxNum = teamQuery.getMaxNum();
        queryWrapper.eq(maxNum != null && maxNum >= 0, Team::getMaxNum, maxNum);

        Long userId = teamQuery.getUserId();
        queryWrapper.eq(userId != null && userId > 0, Team::getUserId, userId);

        Integer status = teamQuery.getStatus();
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        // 根据状态查询前要做判断
        if (enumByValue == null) {
            enumByValue = TeamStatusEnum.PUBLIC; // 如果未设置状态参数，则默认查询公开的
        }
        boolean isAdmin = userService.isAdmin(request);
        if (!isAdmin && !TeamStatusEnum.PUBLIC.equals(enumByValue)) { // 如果当前用户不是管理员，且要查询的房间是非公开的，则提示无权限
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        queryWrapper.eq(Team::getStatus, enumByValue.getValue());
        //2. 不展示已过期的队伍（根据过期时间筛选） expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.isNotNull(Team::getExpireTime).or().gt(Team::getExpireTime, new Date()));

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) return new ArrayList<>();
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            userId = team.getUserId();
            if (userId == null) continue;
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            // 脱敏用户信息
            if (user != null){
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        //1. 判断请求参数是否为空
        if (teamUpdateRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        //2. 查询队伍是否存在
        Long teamId = teamUpdateRequest.getId();
        Team oldTeam = this.getById(teamId);
        if (oldTeam == null) throw new BusinessException(ErrorCode.NULL_ERROR, "要更新的队伍不存在");
        //3. 只有管理员或者队伍的创建者可以修改
        User currentUser = userService.currentUser(request);
        boolean isAdmin = userService.isAdmin(currentUser);
        if (!isAdmin && !Objects.equals(currentUser.getId(), oldTeam.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4. 如果用户传入的新值和老值一致，就不用 update 了（可自行实现，降低数据库使用次数）
        /*TeamUpdateRequest oldTeamRequest = new TeamUpdateRequest();
        BeanUtils.copyProperties(oldTeam, oldTeamRequest);
        if (oldTeamRequest == teamUpdateRequest) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入了未更新的参数");
        }*/
        //5. **如果队伍状态改为加密，必须要有密码**
        Integer status = teamUpdateRequest.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (!TeamStatusEnum.PUBLIC.equals(statusEnum)) {
            String password = teamUpdateRequest.getPassword();
            if (StringUtils.isBlank(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "非公开房间必须设置密码");
            }
        }
        //6. 更新成功
        Team newTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, newTeam);
        return this.updateById(newTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        //1. 用户最多加入 5 个队伍
        User currentUser = userService.currentUser(request);
        Long currentUserId = currentUser.getId();
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(currentUserId != null && currentUserId > 0, Team::getUserId, currentUserId);
        long hasJoinTeam = this.count(queryWrapper);
        if (hasJoinTeam >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多加入 5 个队伍");
        }
        //2. 队伍必须存在，只能加入未满、未过期的队伍
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍不存在");
        }

        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        long teamHasJoinUser = userTeamService.count(lambdaQueryWrapper);
        if (teamHasJoinUser >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍人数已满");
        }

        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍已过期");
        }
        //3. 不能加入自己的队伍，不能重复加入已加入的队伍（幂等性）
        Long teamUserId = team.getUserId();
        if (Objects.equals(teamUserId, currentUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入自己创建的队伍，你已在队伍中");
        }

        lambdaQueryWrapper.eq(UserTeam::getUserId, currentUserId);
        long count = userTeamService.count(lambdaQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复加入已加入的队伍，你已在队伍中");
        }
        //4. 禁止加入私有的队伍
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        //5. 如果加入的队伍是加密的，必须密码匹配才可以
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            String joinPassword = teamJoinRequest.getPassword();
            if (StringUtils.isBlank(joinPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "需要提供密码");
            }
            String password = team.getPassword();
            if (!password.equals(joinPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        //6. 新增队伍 - 用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(currentUserId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    /**
     * 用户退出队伍
     * @param id
     * @param request
     * @return
     */
    @Override
    @Transactional
    public boolean quitTeam(Long id, HttpServletRequest request) {
        //2. 校验队伍是否存在
        Team team = this.getById(id);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //3. 校验我是否已加入队伍
        User currentUser = userService.currentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户未登录");
        }
        Long userId = currentUser.getId();
        Long teamId = team.getId();
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null && userId > 0, UserTeam::getUserId, userId);
        queryWrapper.eq(teamId != null && teamId > 0, UserTeam::getTeamId, teamId);
        long count = userTeamService.count(queryWrapper);
        if (count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "你不在此队伍中，无法退出");
        }
        //4. 如果队伍
        //   1. 只剩一人，队伍解散
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(teamId != null && teamId > 0, UserTeam::getTeamId, teamId);
        long userTeamCount = userTeamService.count(queryWrapper);
        if (userTeamCount <= 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        if (userTeamCount == 1){
            boolean removeById = this.removeById(teamId);
            UserTeam userTeam = userTeamService.getOne(queryWrapper);
            boolean remove = userTeamService.removeById(userTeam);
            if (!removeById || !remove) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }else {
            //   2. 还有其他人
            //      1. 如果是队长退出队伍，权限转移给第二早加入的用户 —— 先来后到
            if (Objects.equals(team.getUserId(), userId)){
                queryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextUserTeamUserId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserTeamUserId);
                this.updateById(updateTeam);
            }
            // 移除关系
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTeam::getUserId, userId);
            queryWrapper.eq(teamId != null && teamId > 0, UserTeam::getTeamId, teamId);
            boolean result = userTeamService.remove(queryWrapper);
            if (!result){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return true;
    }

    /**
     * 队长解散队伍
     * @param id
     * @param request
     * @return
     */
    @Override
    @Transactional
    public boolean deleteTeam(Long id, HttpServletRequest request) {
        //2. 校验队伍是否存在
        Team team = this.getById(id);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //3. 校验你是不是队伍的队长
        User currentUser = userService.currentUser(request);
        if (!Objects.equals(team.getUserId(), currentUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH, "你不是队长，无法解散队伍");
        }
        //4. 移除所有加入队伍的关联信息
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        Long teamId = team.getId();
        queryWrapper.eq(teamId != null && teamId > 0, UserTeam::getTeamId, teamId);
        boolean removeResult = userTeamService.remove(queryWrapper);
        if (!removeResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //5. 删除队伍
        return this.removeById(teamId);
    }
}




