package com.my.xiaozhang.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author:22603
 * @Date:2023/3/18 14:58
 */
@Data
public class TagVo implements Serializable {

    /**
     * 用户标签
     */
    private List<String> oldTags;

    /**
     * 智能推荐标签
     */
    private List<String> RecommendTags;

}
