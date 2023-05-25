package com.my.xiaozhang.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求体
 * @author 22603
 */
@Data
public class GetNewUserInfoRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;


    private String currentId;

}
