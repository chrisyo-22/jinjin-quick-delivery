package com.jinjin.service;

import com.jinjin.dto.UserLoginDTO;
import com.jinjin.entity.User;

public interface UserService {

    /**
     * 用户登录。
     *
     * @param userLoginDTO login payload
     * @return persisted user
     */
    User login(UserLoginDTO userLoginDTO);
}
