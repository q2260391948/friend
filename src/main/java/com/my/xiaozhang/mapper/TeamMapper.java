package com.my.xiaozhang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.my.xiaozhang.model.domain.Team;
import com.my.xiaozhang.model.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 22603
 */
public interface TeamMapper extends BaseMapper<Team> {
    List<User> SelectUsers(@Param("teamId") long teamId);
}




