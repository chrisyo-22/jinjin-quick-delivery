package com.jinjin.service.impl;

import com.jinjin.constant.MessageConstant;
import com.jinjin.dto.UserLoginDTO;
import com.jinjin.entity.User;
import com.jinjin.exception.LoginFailedException;
import com.jinjin.mapper.UserMapper;
import com.jinjin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null || !StringUtils.hasText(userLoginDTO.getCode())) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        String openid = deriveLocalOpenId(userLoginDTO.getCode());
        User user = userMapper.getByOpenid(openid);
        if (user != null) {
            return user;
        }

        user = User.builder()
                .openid(openid)
                .name("User-" + openid.substring(Math.max(0, openid.length() - 6)))
                .createTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        log.info("Registered local user {}", user.getId());
        return user;
    }

    private String deriveLocalOpenId(String loginSeed) {
        return "local_" + UUID.nameUUIDFromBytes(loginSeed.trim().getBytes(StandardCharsets.UTF_8))
                .toString()
                .replace("-", "");
    }
}
