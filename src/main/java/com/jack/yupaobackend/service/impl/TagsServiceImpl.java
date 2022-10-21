package com.jack.yupaobackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.yupaobackend.model.domain.Tags;
import com.jack.yupaobackend.mapper.TagsMapper;
import com.jack.yupaobackend.service.TagsService;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【tags(用户标签表)】的数据库操作Service实现
* @createDate 2022-10-17 15:55:51
*/
@Service
public class TagsServiceImpl extends ServiceImpl<TagsMapper, Tags>
    implements TagsService {

}




