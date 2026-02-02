package com.tontin.platform.mapper.user;

import org.mapstruct.Mapper;

import com.tontin.platform.domain.User;
import com.tontin.platform.dto.auth.User.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user); 
}
