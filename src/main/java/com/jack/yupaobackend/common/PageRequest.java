package com.jack.yupaobackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求类
 *
 * @author yuguoxin
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1974649411462843630L;
    /**
     * 页面大小，一页有几条数据
     */
    protected int pageSize = 10;
    /**
     * 当前是第几页
     */
    protected int pageNum = 1;
}
