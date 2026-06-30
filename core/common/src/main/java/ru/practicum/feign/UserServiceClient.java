package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.UserDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/admin/users")
    List<UserDto> getUsers(@RequestParam("ids") List<Long> ids);

    default boolean userExists(Long userId) {
        try {
            List<UserDto> users = getUsers(List.of(userId));
            return users != null && !users.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}