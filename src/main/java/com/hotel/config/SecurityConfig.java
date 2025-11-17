package com.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.hotel.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Autowired
    private CustomOAuth2UserService oauth2UserService;


    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/register/**", "/").permitAll()
                                .requestMatchers("/sona/**", "/dashtreme/**").permitAll()
                                .requestMatchers("/admin/dashboard").hasRole("ADMIN")
                                .requestMatchers("/admin/users/**").hasRole("ADMIN")
                                .requestMatchers("/admin/room-types/**").hasRole("ADMIN")
                                .requestMatchers("/admin/rooms/**").hasRole("ADMIN")
                                .requestMatchers("/admin/bookings/**").hasRole("ADMIN")
                                .requestMatchers("/rooms", "/room-details/**").permitAll()
                                .requestMatchers("/booking/**").authenticated()
                                .requestMatchers("/admin/services/**").hasRole("ADMIN")
                                .requestMatchers("/admin/payments/**").hasRole("ADMIN")
                                .requestMatchers("/admin/contacts/**").hasRole("ADMIN")
                                .requestMatchers("/contact", "/contact/send").permitAll()
                                .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                                .requestMatchers("/write-review/**", "/submit-review").authenticated()
                                .requestMatchers("/admin/calendar/**").hasRole("ADMIN")
                                .requestMatchers("/api/calendar/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/", false)
                                .permitAll()
                ).oauth2Login(
                        oauth2 -> oauth2
                                .loginPage("/login") // Dùng chung trang login
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(oauth2UserService) // Dùng service ta vừa tạo
                                )
                                .defaultSuccessUrl("/", false)
                ).logout(
                        logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                );
        return http.build();
    }
}