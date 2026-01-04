package com.tu2l.user.utils;

import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.model.request.NewUserRegisterRequest;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "role", expression = "java(UserEntityV2.getRole().name())")
    UserDTO toUserDTO(UserEntity UserEntity);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.valueOf(userDTO.getRole()))")
    UserEntity toUserEntity(UserDTO userDTO);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.USER.name())")
    UserDTO toUserDTO(NewUserRegisterRequest NewUserRegisterRequest);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.USER)")
    UserEntity toUserEntity(NewUserRegisterRequest NewUserRegisterRequest);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.valueOf(userDTO.getRole()))")
    UserEntity updateUserFromDTO(UserDTO userDTO, @MappingTarget UserEntity UserEntity);

    UserDTO toUserDTO(UpdateUserRequest updateUserRequest);
}