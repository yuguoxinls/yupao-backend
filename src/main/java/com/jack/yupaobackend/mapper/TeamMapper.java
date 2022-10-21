package com.jack.yupaobackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.yupaobackend.model.domain.Team;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yuguoxin
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2022-10-21 15:54:34
* @Entity generator.domain.Team
*/
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

}




