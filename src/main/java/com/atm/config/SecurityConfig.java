package com.atm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // You’re handling sessions manually; CSRF off keeps the custom forms simple.
            .csrf(csrf -> csrf.disable())

            // Allow your app routes and static resources.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/atm/**", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().permitAll()
            )

            // No Spring Security default login page
            .formLogin(form -> form.disable())

            // Proper logout handling
            .logout(logout -> logout
                .logoutUrl("/atm/logout")                     // form action
                .logoutSuccessUrl("/atm/login?logout")        // redirect with ?logout flag
                .invalidateHttpSession(true)                  // clear session
                .deleteCookies("JSESSIONID")                  // clear cookie
                .permitAll()
            );

        return http.build();
    }
}
