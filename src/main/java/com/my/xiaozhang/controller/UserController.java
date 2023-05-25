package com.my.xiaozhang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.xiaozhang.common.BaseResponse;
import com.my.xiaozhang.common.ErrorCode;
import com.my.xiaozhang.common.ResultUtils;
import com.my.xiaozhang.exception.BusinessException;
import com.my.xiaozhang.model.domain.User;
import com.my.xiaozhang.model.request.GetNewUserInfoRequest;
import com.my.xiaozhang.model.request.UserLoginRequest;
import com.my.xiaozhang.model.request.UserRegisterRequest;
import com.my.xiaozhang.model.vo.TagVo;
import com.my.xiaozhang.model.vo.UserForgetRequest;
import com.my.xiaozhang.model.vo.UserSendMessage;
import com.my.xiaozhang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.my.xiaozhang.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author 22603
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000/","http://124.221.169.181/"})
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @PostMapping("/sendMessage")
    public BaseResponse<Boolean> sendMessage(@RequestBody UserSendMessage userSendMessage) {
        log.info("userSendMessage:"+userSendMessage.toString());
        return userService.sendMessage(userSendMessage);
    }

    @PutMapping("/forget")
    public BaseResponse<Boolean> forget(@RequestBody UserForgetRequest userForgetRequest) {
        return userService.updatePassword(userForgetRequest);
    }


    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String userEmail = userRegisterRequest.getUserEmail();
        String code = userRegisterRequest.getCode();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = String.valueOf(System.currentTimeMillis());
        log.info(System.currentTimeMillis()+"时间");
        if (StringUtils.isAnyBlank(userAccount,userEmail,code, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount,userEmail,code, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(String id,HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = userService.userLogout(id, request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(@RequestParam String id, HttpServletRequest request) {
        log.info("id:"+id);
        String redisKey = String.format(USER_LOGIN_STATE+id);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        User currentUser = (User) valueOperations.get(redisKey);
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
//        long userId = currentUser.getId();
//        // TODO 校验用户是否合法
//        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(currentUser);
        return ResultUtils.success(safetyUser);
    }

    /**
     * @param username
     * @param request
     * @return
     */

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        log.info(tagNameList.toString());
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

   @GetMapping("/getNewUserInfo")
    public BaseResponse<User> getNewUserInfo(String id) {
        log.info("id:"+id);
        if (CollectionUtils.isEmpty(Collections.singleton(id))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        return ResultUtils.success(user);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(String currentId,long pageSize, long pageNum, HttpServletRequest request) {
//        获取当前登录用户信息
        String userKey = String.format(USER_LOGIN_STATE+currentId);
        log.info(userKey);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        User loginUser = (User)valueOperations.get(userKey);
//        设计字符串id作为该用户标识
        log.info(loginUser.getId()+"userid");
        String redisKey = String.format("my:user:recommend:%s", loginUser.getId());
        // 如果有缓存，直接读缓存
        Page<User> userPage=null;

        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("id",loginUser.getId());
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        log.info("pageNum:"+pageNum);
        log.info("pageSize:"+pageSize);
        log.info("userPage:"+userPage);
        return ResultUtils.success(userPage);
    }


    /***
     * 修改
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, String currentId, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        log.info(user.toString());
        log.info("id:"+currentId);
        String redisKey = String.format(USER_LOGIN_STATE+user.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        User currentUser = (User) valueOperations.get(redisKey);
//        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, currentUser);
        return ResultUtils.success(result);
    }

    /**
     * @param id
     * @param request
     * @return
     */

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num,String currentId,  HttpServletRequest request) throws IOException {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        log.info("id:"+currentId);
        String redisKey = String.format(USER_LOGIN_STATE+currentId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        User currentUser = (User) valueOperations.get(redisKey);
        String redisMatchKey = String.format("my:user:match:%s", currentUser.getId());
        // 如果有缓存，直接读缓存
        List<User> matchUsers=null;
        if (redisKey!=null){
            matchUsers = (List<User>) valueOperations.get(redisMatchKey);
            if (matchUsers != null) {
                return ResultUtils.success(matchUsers);
            }
        }
        List<User> users = userService.matchUsers(num, currentUser);
        try {
            valueOperations.set(redisMatchKey, users, 3, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }

        return ResultUtils.success(users);
    }


    @GetMapping("/get/tags")
    public BaseResponse<TagVo> getTags(String currentId, HttpServletRequest request) {
        TagVo tagVo = userService.getTags(currentId,request);
        log.info(tagVo.toString());
        return ResultUtils.success(tagVo);
    }
}
