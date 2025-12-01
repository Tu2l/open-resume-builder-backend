package com.tu2l.user.utils;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.model.request.RegisterRequest;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "role", expression = "java(userEntity.getRole().name())")
    UserDTO toUserDTO(UserEntity userEntity);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.valueOf(userDTO.getRole()))")
    UserEntity toUserEntity(UserDTO userDTO);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.USER.name())")
    UserDTO toUserDTO(RegisterRequest registerRequest);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.USER)")
    UserEntity toUserEntity(RegisterRequest registerRequest);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.valueOf(userDTO.getRole()))")
    UserEntity updateUserFromDTO(UserDTO userDTO, @MappingTarget UserEntity userEntity);

    UserDTO toUserDTO(UpdateUserRequest updateUserRequest);
}