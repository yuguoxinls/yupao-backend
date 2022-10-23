package com.jack.yupaobackend.model.dto;

import com.jack.yupaobackend.common.PageRequest;
import lombok.Data;

import java.io.Serial;


@Data
public class TeamQuery extends PageRequest {

    @Serial
    private static final long serialVersionUID = -9131018546674251945L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
}
