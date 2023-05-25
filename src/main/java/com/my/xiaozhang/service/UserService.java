package com.my.xiaozhang.service;

import com.my.xiaozhang.common.BaseResponse;
import com.my.xiaozhang.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.my.xiaozhang.model.vo.TagVo;
import com.my.xiaozhang.model.vo.UserForgetRequest;
import com.my.xiaozhang.model.vo.UserSendMessage;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount,String userEmail,String code,String userPassword, String checkPassword, String planetCode);

    /**
     * 发送验证码
     * @param eamil
     * @return
     */
    BaseResponse<Boolean> sendMessage(UserSendMessage eamil);

    /**
     * 修改密码
     * @param userForgetRequest
     * @return
     */
    BaseResponse<Boolean> updatePassword(UserForgetRequest userForgetRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    Boolean userLogout(String id,HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser) throws IOException;

    TagVo getTags(String id, HttpServletRequest request);
}
