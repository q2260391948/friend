package com.my.xiaozhang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.my.xiaozhang.common.BaseResponse;
import com.my.xiaozhang.common.ErrorCode;
import com.my.xiaozhang.common.ResultUtils;
import com.my.xiaozhang.constant.UserConstant;
import com.my.xiaozhang.exception.BusinessException;
import com.my.xiaozhang.mapper.UserMapper;
import com.my.xiaozhang.model.domain.User;
import com.my.xiaozhang.model.enums.ListTypeEnum;
import com.my.xiaozhang.model.vo.TagVo;
import com.my.xiaozhang.model.vo.UserForgetRequest;
import com.my.xiaozhang.model.vo.UserSendMessage;
import com.my.xiaozhang.service.UserService;
import com.my.xiaozhang.utils.AllUtils;
import com.my.xiaozhang.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.my.xiaozhang.constant.UserConstant.USER_LOGIN_STATE;
import static com.my.xiaozhang.model.enums.ListTypeEnum.ENGLISH;
import static com.my.xiaozhang.model.enums.ListTypeEnum.MIXEECAE;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    //把yml配置的邮箱号赋值到from
    @Value("${spring.mail.username}")
    private String from;
    //发送邮件需要的对象
    @Resource
    private JavaMailSender javaMailSender;


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "my";

    @Override
    public long userRegister(String userAccount, String userEmail, String code, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userEmail, code, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短,应不少于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，应不少于8位");
        }
        if (code.length() != 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入6位验证码");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和确认密码不一致");
        }
        // 获取缓存验证码
        String redisKey = String.format("my:user:sendMessage:%s", userEmail);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        log.info(redisKey);
        UserSendMessage sendMessage = (UserSendMessage) valueOperations.get(redisKey);
        if (!Optional.ofNullable(sendMessage).isPresent()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取验证码失败!");
        }
        //比对验证码
        String sendMessageCode = sendMessage.getCode();
        log.info(sendMessageCode);
        if (!code.equals(sendMessageCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不匹配!");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setEmail(userEmail);
        user.setPlanetCode(planetCode);
        String defaultUrl = "https://img1.baidu.com/it/u=1637179393,2329776654&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=542";
        user.setAvatarUrl(defaultUrl);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public BaseResponse<Boolean> sendMessage(UserSendMessage toEmail) {
        String email = toEmail.getUserEmail();
        if (StringUtils.isEmpty(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "email为空");
        }
        String subject = "伙伴匹配系统";
        String code = "";
        //StringUtils.isNotEmpty字符串非空判断
        if (StringUtils.isNotEmpty(email)) {
            //发送一个四位数的验证码,把验证码变成String类型
            code = ValidateCodeUtils.generateValidateCode(6).toString();
            String text = "【伙伴匹配系统】您好，您的验证码为：" + code + "，请在5分钟内使用";
            log.info("验证码为：" + code);
            //发送短信
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(text);
            //发送邮件
            javaMailSender.send(message);
            UserSendMessage userSendMessage = new UserSendMessage();
            userSendMessage.setUserEmail(email);
            userSendMessage.setCode(code);
            // 作为唯一标识
            String redisKey = String.format("my:user:sendMessage:%s", email);
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            // 写缓存
            try {
                valueOperations.set(redisKey, userSendMessage, 300000, TimeUnit.MILLISECONDS);
                UserSendMessage sendMessage = (UserSendMessage) valueOperations.get(redisKey);
                log.info(sendMessage.toString());
                return ResultUtils.success(true);
            } catch (Exception e) {
                log.error("redis set key error", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "缓存失败!");
            }
        }
        return ResultUtils.success(true);
    }

    @Override
    public BaseResponse<Boolean> updatePassword(UserForgetRequest userForgetRequest) {
        String email = userForgetRequest.getUserEmail();
        String userPassword = userForgetRequest.getUserPassword();
        String code = userForgetRequest.getCode();
        String userAccount = userForgetRequest.getUserAccount();
        // 1. 校验
        if ((!Optional.ofNullable(email).isPresent()) || (!Optional.ofNullable(userPassword).isPresent())
                || (!Optional.ofNullable(code).isPresent()) || (!Optional.ofNullable(userAccount).isPresent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，应不少于8位!");
        }
        String redisKey = String.format("my:user:sendMessage:%s", email);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        log.info(redisKey);
        UserSendMessage sendMessage = (UserSendMessage) valueOperations.get(redisKey);
        if (!Optional.ofNullable(sendMessage).isPresent()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取验证码失败!");
        }
        String sendMessageCode = sendMessage.getCode();
        log.info(sendMessageCode);
        if (!code.equals(sendMessageCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不匹配!");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("userAccount", userAccount);
        User user = userMapper.selectOne(queryWrapper);
        if (!Optional.ofNullable(user).isPresent()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户不存在!");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        user.setUserPassword(encryptPassword);
        int role = userMapper.updateById(user);
        if (role > 0) {
            return ResultUtils.success(true);
        } else {
            return ResultUtils.success(false);
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        String redisKey = String.format(USER_LOGIN_STATE + safetyUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(redisKey, safetyUser, 30, TimeUnit.MINUTES);
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setProfile(originUser.getProfile());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public Boolean userLogout(String id, HttpServletRequest request) {
        // 移除登录态
        String redisKey = String.format(USER_LOGIN_STATE + id);
        log.info(redisKey);
        Boolean delete = redisTemplate.delete(redisKey);
        return delete;
    }

    /**
     * 根据标签搜索用户（内存过滤）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果用户没有传任何要更新的值，就直接报错，不用执行 update 语句
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不存在修改数据");
        }
        log.info(user.toString());
        if (user.getPhone() != null && user.getPhone().length() != 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "电话长度过长或过短");
        }
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        log.info("loginUser:" + loginUser);
        log.info("userId:" + userId);
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) throws IOException {
        loginUser = this.getById(loginUser.getId());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        loginUser = this.getById(loginUser.getId());
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        log.info("tagList:" + tagList.toString());
        // 用户列表的下标 => 相似度
        List<Pair<User, Double>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            double distance = sorce(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Double>> topUserPairList = list.stream()
                .sorted((o1, o2) -> Double.compare(o2.getValue(), o1.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    public double sorce(List<String> list1, List<String> list2) throws IOException {
        List<String> resultList1 = list1.stream().map(String::toLowerCase).collect(Collectors.toList());
        List<String> resultList2 = list2.stream().map(String::toLowerCase).collect(Collectors.toList());
        int strType = AllUtils.getStrType(resultList1);
        int type = AllUtils.getStrType(resultList2);
        ListTypeEnum enumByValue = ListTypeEnum.getEnumByValue(strType);
        ListTypeEnum enumByValue1 = ListTypeEnum.getEnumByValue(type);
        if (enumByValue == MIXEECAE) {
            resultList1 = AllUtils.tokenize(resultList1);
        }
        if (enumByValue1 == MIXEECAE) {
            resultList2 = AllUtils.tokenize(resultList2);
        }
        double ikSorce = 0;
        if (enumByValue != ENGLISH && enumByValue1 != ENGLISH) {
            List<String> resultList3 = list1.stream().map(String::toLowerCase).collect(Collectors.toList());
            List<String> resultList4 = list2.stream().map(String::toLowerCase).collect(Collectors.toList());
            List<String> quotedList1 = resultList3.stream()
                    .map(str -> "\"" + str + "\"")
                    .collect(Collectors.toList());
            List<String> quotedList2 = resultList4.stream()
                    .map(str -> "\"" + str + "\"")
                    .collect(Collectors.toList());
            String tags1 = AllUtils.collectChineseChars(quotedList1);
            List<String> Ls = AllUtils.analyzeText(tags1);
            String tags2 = AllUtils.collectChineseChars(quotedList2);
            List<String> Ls2 = AllUtils.analyzeText(tags2);
            ikSorce = AllUtils.calculateJaccardSimilarity(Ls, Ls2);
        }
        int EditDistanceSorce = AllUtils.calculateEditDistance(resultList1, resultList2);
        double maxEditDistance = Math.max(resultList1.size(), resultList2.size());
        double EditDistance = 1 - EditDistanceSorce / maxEditDistance;
        double JaccardSorce = AllUtils.calculateJaccardSimilarity(resultList1, resultList2);
        double similaritySorce = AllUtils.cosineSimilarity(resultList1, resultList2);
        /**
         * 编辑距离 权重为0.5
         * Jaccard相似度算法（ik分词后使用Jaccard相似度算法） 权重为0.3
         *  余弦相似度 权重为0.2
         *
         */
        double totalSorce = EditDistance * 0.5 + JaccardSorce * 0.3 + similaritySorce * 0.2 + ikSorce * 0.3;
        return totalSorce;
    }

    @Override
    public TagVo getTags(String id, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TagVo tagVo = new TagVo();
        log.info("id:" + id);
        String redisKey = String.format(USER_LOGIN_STATE + id);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        User currentUser = (User) valueOperations.get(redisKey);
        User userById = this.getById(currentUser.getId());
        String OldTags = userById.getTags();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        List<User> users = this.list(queryWrapper);
        Map<String, Integer> map = new HashMap<>();
        Gson gson = new Gson();
        log.info(OldTags);
        List<String> oldTags = gson.fromJson(OldTags, new TypeToken<List<String>>() {
        }.getType());
        log.info(oldTags.toString() + "");
        for (User user : users) {
            String Tags = user.getTags();
            List<String> list = Arrays.asList(Tags);
            if (list == null) {
                continue;
            }
            List<String> tagList = gson.fromJson(Tags, new TypeToken<List<String>>() {
            }.getType());
            if (tagList != null) {
                for (String tag : tagList) {
                    if (map.get(tag) == null) {
                        map.put(tag, 1);
                    } else {
                        map.put(tag, map.get(tag) + 1);
                    }
                }
            }
        }
        Map<Integer, List<String>> Map = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer key1, Integer key2) {
                //降序排序
                return key2.compareTo(key1);
            }
        });
        map.forEach((value, key) -> {
            if (Map.size() == 0) {
                Map.put(key, Arrays.asList(value));
            } else if (Map.get(key) == null) {
                Map.put(key, Arrays.asList(value));
            } else {
                List<String> list = new ArrayList(Map.get(key));
                list.add(value);
                Map.put(key, list);
            }
        });
        Set<String> set = new HashSet<>();
        for (Map.Entry<Integer, List<String>> entry : Map.entrySet()) {

            log.info("set.size():"+set.size());
            List<String> value = entry.getValue();
            if (oldTags == null) {
                for (String tag : value) {
                    set.add(tag);
                    if (set.size() >= 20) {
                        break;
                    }
                }
            }
            for (String tag : value) {
                if (!oldTags.contains(tag)) {
                    set.add(tag);
                    if (set.size() >= 20) {
                        break;
                    }
                }
            }
        }

        List<String> RecommendTags = new ArrayList<String>(set);
        log.info("RecommendTags:"+RecommendTags);
        tagVo.setOldTags(oldTags);
        tagVo.setRecommendTags(RecommendTags);
        return tagVo;
    }

    /**
     * 根据标签搜索用户（SQL 查询版）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like '%Python%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

}




