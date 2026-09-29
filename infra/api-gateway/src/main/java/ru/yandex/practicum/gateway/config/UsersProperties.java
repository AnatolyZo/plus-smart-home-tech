package ru.yandex.practicum.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class UsersProperties {
    private List<UserData> users = new ArrayList<>();

    @Getter
    @Setter
    public static class UserData {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();
    }
}
