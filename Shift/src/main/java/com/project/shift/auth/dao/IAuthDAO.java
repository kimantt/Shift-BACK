package com.project.shift.auth.dao;

import com.project.shift.user.entity.UserEntity;

public interface IAuthDAO {

    UserEntity getUser(UserEntity userEntity);

    UserEntity getUserById(Long userId);

    void saveRefreshToken(UserEntity userEntity, String refreshToken);

    String getRefreshToken(Long userId);

    void updateRefreshToken(Long userId);
}
