package com.my.xiaozhang.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户登录请求体
 */
@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;


    private String currentId;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

    /**
     * 队伍头像
     */
    private String avatarUrl;

}
