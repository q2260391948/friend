package com.my.xiaozhang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.xiaozhang.model.domain.User;
import com.my.xiaozhang.model.request.TeamUpdateRequest;
import com.my.xiaozhang.service.UserTeamService;
import com.my.xiaozhang.model.domain.UserTeam;
import com.my.xiaozhang.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
        implements UserTeamService {

}




