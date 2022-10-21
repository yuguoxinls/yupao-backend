package com.jack.yupaobackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.yupaobackend.model.domain.Tags;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Administrator
* @description 针对表【tags(用户标签表)】的数据库操作Mapper
* @createDate 2022-10-17 15:55:51
* @Entity generator.domain.tags
*/
@Mapper
public interface TagsMapper extends BaseMapper<Tags> {

}




