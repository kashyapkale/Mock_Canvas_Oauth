package org.oauth.fake_oauth_canvas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final GenericLogoutHandler genericLogoutHandler;

    public SecurityConfig(GenericLogoutHandler genericLogoutHandler) {
        this.genericLogoutHandler = genericLogoutHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/login/oauth2/**", "/oauth2/**", "/api/v1/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/dashboard", true)
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(genericLogoutHandler)
                        .logoutSuccessUrl("/").permitAll()
                );
        return http.build();
    }
}
