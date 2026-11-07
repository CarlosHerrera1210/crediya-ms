package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.UserRequest;
import co.com.crediya.api.dto.UserResponse;
import co.com.crediya.model.user.User;

public final class UserDtoMapper {

    private UserDtoMapper() {
    }

    public static User toDomain(UserRequest request) {
        return User.builder()
                .names(request.names())
                .lastNames(request.lastNames())
                .birthDate(request.birthDate())
                .address(request.address())
                .phone(request.phone())
                .email(request.email())
                .baseSalary(request.baseSalary())
                .documentNumber(request.documentNumber())
                .roleId(request.roleId())
                .build();
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNames(),
                user.getLastNames(),
                user.getBirthDate(),
                user.getAddress(),
                user.getPhone(),
                user.getEmail(),
                user.getBaseSalary(),
                user.getDocumentNumber(),
                user.getRoleId());
    }
}
