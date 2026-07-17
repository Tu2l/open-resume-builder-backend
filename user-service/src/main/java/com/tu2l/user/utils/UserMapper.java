package com.tu2l.user.utils;

import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.model.request.NewUserRegisterRequest;
import com.tu2l.user.model.request.UpdateUserRequest;
import com.tu2l.user.model.response.UserDTO;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "role", expression = "java(UserEntity.getRole().name())")
    @Mapping(source = "profile.firstName", target = "firstName")
    @Mapping(source = "profile.middleName", target = "middleName")
    @Mapping(source = "profile.lastName", target = "lastName")
    @Mapping(source = "profile.phoneNumber", target = "phoneNumber")
    @Mapping(source = "accountStatus.enabled", target = "enabled")
    UserDTO toUserDTO(UserEntity UserEntity);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.valueOf(userDTO.getRole()))")
    UserEntity toUserEntity(UserDTO userDTO);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.USER.name())")
    UserDTO toUserDTO(NewUserRegisterRequest NewUserRegisterRequest);

    @Mapping(target = "role", expression = "java(com.tu2l.common.model.states.UserRole.USER)")
    UserEntity toUserEntity(NewUserRegisterRequest NewUserRegisterRequest);

    // Profile update only: role and email are not mutable here (role changes go through
    // AuthorizationService.assignRole; email is non-updatable). IGNORE strategy so unset
    // (null) request fields never overwrite existing values.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(source = "firstName", target = "profile.firstName")
    @Mapping(source = "middleName", target = "profile.middleName")
    @Mapping(source = "lastName", target = "profile.lastName")
    @Mapping(source = "phoneNumber", target = "profile.phoneNumber")
    UserEntity updateUserFromDTO(UserDTO userDTO, @MappingTarget UserEntity UserEntity);

    UserDTO toUserDTO(UpdateUserRequest updateUserRequest);
}