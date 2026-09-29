package ru.yandex.practicum.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(UsersProperties.class)
public class SecurityConfig {
    private final UsersProperties usersProperties;

    private static final String URL_API = "/api";
    private static final String URL_PRODUCTS = "/products";
    private static final String URL_CATEGORIES = "/categories";
    private static final String URL_ORDERS = "/orders";
    private static final String URL_INVENTORY = "/inventory";
    private static final String URL_BY_EMAIL = "/by-email";
    private static final String ID = "id";
    private static final String ANY_PATH = "/**";
    private static final String USER_ROLE = "USER";
    private static final String ADMIN_ROLE = "ADMIN";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, ANY_PATH).permitAll()
                        .pathMatchers("/swagger-ui/**", "/webjars/**").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, URL_API + URL_PRODUCTS + ANY_PATH).permitAll()
                        .pathMatchers(HttpMethod.GET, URL_API + URL_CATEGORIES + ANY_PATH).permitAll()
                        .pathMatchers(HttpMethod.GET, URL_API + URL_INVENTORY + ANY_PATH).permitAll()
                        .pathMatchers(HttpMethod.POST, URL_API + URL_ORDERS + ANY_PATH).hasRole(USER_ROLE)
                        .pathMatchers(HttpMethod.GET, URL_API + URL_ORDERS + URL_BY_EMAIL).hasRole(USER_ROLE)
                        .pathMatchers(HttpMethod.GET, URL_API + URL_ORDERS + "/{" + ID + "}").hasRole(USER_ROLE)
                        .pathMatchers(HttpMethod.GET, URL_API + URL_ORDERS).hasRole(ADMIN_ROLE)
                        .pathMatchers(HttpMethod.POST,
                                URL_API + URL_PRODUCTS + ANY_PATH,
                                URL_API + URL_INVENTORY + ANY_PATH,
                                URL_API + URL_CATEGORIES + ANY_PATH).hasRole(ADMIN_ROLE)
                        .pathMatchers(HttpMethod.PUT, URL_API + URL_INVENTORY + ANY_PATH).hasRole(ADMIN_ROLE)
                        .pathMatchers(HttpMethod.PATCH, URL_API + URL_PRODUCTS + ANY_PATH).hasRole(ADMIN_ROLE)
                        .anyExchange().denyAll())
                .httpBasic(Customizer.withDefaults())
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService(UsersProperties usersProperties, PasswordEncoder passwordEncoder) {
        List<UserDetails> users = usersProperties.getUsers().stream()
                .map(userData -> createUserDetails(userData, passwordEncoder))
                .toList();

        return new MapReactiveUserDetailsService(users);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private UserDetails createUserDetails(UsersProperties.UserData userData, PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(userData.getUsername())
                .password(passwordEncoder.encode(userData.getPassword()))
                .roles(userData.getRoles().toArray(String[]::new))
                .build();
    }
}
