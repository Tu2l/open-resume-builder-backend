package com.tu2l.user.utils;

import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.entity.UserAccountStatus;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.entity.UserProfile;
import com.tu2l.user.model.response.UserDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the MapStruct-generated {@link UserMapperImpl} directly to lock down the
 * profile-flattening and partial-update behaviour (#1–#3).
 */
class UserMapperTest {

    private final UserMapper mapper = new UserMapperImpl();

    @Test
    void toUserDTO_flattensProfileAndAccountStatus() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .role(UserRole.USER)
                .profile(UserProfile.builder()
                        .firstName("John")
                        .middleName("M")
                        .lastName("Doe")
                        .phoneNumber("+1-555")
                        .build())
                .accountStatus(UserAccountStatus.builder().enabled(true).build())
                .build();

        UserDTO dto = mapper.toUserDTO(user);

        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getMiddleName()).isEqualTo("M");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getPhoneNumber()).isEqualTo("+1-555");
        assertThat(dto.getEnabled()).isTrue();
        assertThat(dto.getRole()).isEqualTo("USER");
    }

    @Test
    void updateUserFromDTO_writesProfileFieldsAndIgnoresNullsRoleAndEmail() {
        UserEntity target = UserEntity.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .role(UserRole.ADMIN)
                .profile(UserProfile.builder()
                        .firstName("Old")
                        .lastName("Name")
                        .build())
                .build();

        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setFirstName("New");      // should update
        dto.setLastName(null);         // null -> must NOT overwrite existing "Name"
        dto.setEmail("hacker@x.com");  // email is non-updatable -> ignored
        // role left null -> must not be touched (and must not NPE)

        mapper.updateUserFromDTO(dto, target);

        assertThat(target.getProfile().getFirstName()).isEqualTo("New");
        assertThat(target.getProfile().getLastName()).isEqualTo("Name");
        assertThat(target.getEmail()).isEqualTo("john@example.com");
        assertThat(target.getRole()).isEqualTo(UserRole.ADMIN);
    }
}
