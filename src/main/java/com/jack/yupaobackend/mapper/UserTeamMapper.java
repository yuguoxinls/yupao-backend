package com.jack.yupaobackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.yupaobackend.model.domain.UserTeam;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yuguoxin
* @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
* @createDate 2022-10-21 20:13:26
* @Entity generator.domain.UserTeam
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




