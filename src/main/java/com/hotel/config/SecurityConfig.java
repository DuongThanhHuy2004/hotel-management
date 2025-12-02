package com.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) ->
                        authorize
                                // --- PUBLIC (Ai cũng vào được) ---
                                .requestMatchers("/register/**", "/").permitAll()
                                .requestMatchers("/sona/**", "/dashtreme/**").permitAll()
                                .requestMatchers("/rooms", "/room-details/**").permitAll()
                                .requestMatchers("/contact", "/contact/send").permitAll()
                                .requestMatchers("/login/**").permitAll()

                                // --- ADMIN (Chỉ quản trị viên) ---
                                .requestMatchers("/admin/dashboard").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/admin/users/**").hasRole("ADMIN")
                                .requestMatchers("/admin/room-types/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/admin/rooms/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/admin/bookings/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/admin/services/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/admin/payments/**").hasRole("ADMIN")
                                .requestMatchers("/admin/contacts/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/admin/calendar/**").hasAnyRole("ADMIN", "STAFF")
                                // API cho Dashboard & Calendar
                                .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/api/calendar/**").hasAnyRole("ADMIN", "STAFF")

                                // --- AUTHENTICATED (Phải đăng nhập: User hoặc Admin) ---
                                .requestMatchers("/booking/**").authenticated()
                                .requestMatchers("/write-review/**", "/submit-review").authenticated()

                                .requestMatchers("/profile/**").authenticated()
                                .requestMatchers("/my-bookings").authenticated()

                                // Tất cả các request khác đều phải đăng nhập
                                .anyRequest().authenticated()
                )
                .formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/", false)
                                .permitAll()
                )
                .oauth2Login(
                        oauth2 -> oauth2
                                .loginPage("/login")
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(oauth2UserService)
                                )
                                .defaultSuccessUrl("/", false)
                )
                .logout(
                        logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                );
        return http.build();
    }
}