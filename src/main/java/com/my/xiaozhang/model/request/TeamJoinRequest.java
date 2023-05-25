package com.my.xiaozhang.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求体
 * @author 22603
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * id
     */
    private Long teamId;


    private String currentId;

    /**
     * 密码
     */
    private String password;
}
