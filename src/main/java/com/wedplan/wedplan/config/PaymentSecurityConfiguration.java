package com.wedplan.wedplan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.UUID;

@Configuration
public class PaymentSecurityConfiguration {
    @Bean
    SecurityFilterChain paymentSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/bookings", "/reservations/**", "/checkout/**",
                                "/payhere/notify", "/admin/login", "/style/**", "/script/**",
                                "/images/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/admin/**", "/payments/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payhere/notify"))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/bookings", true)
                        .failureUrl("/admin/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/?loggedOut")
                        .permitAll())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService adminUserDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${wedplan.admin.username:}") String configuredUsername,
            @Value("${wedplan.admin.password:}") String configuredPassword) {
        UserDetails admin;
        if (configuredUsername.isBlank() || configuredPassword.isBlank()) {
            admin = User.withUsername("admin-not-configured")
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .roles("ADMIN")
                    .disabled(true)
                    .build();
        } else {
            admin = User.withUsername(configuredUsername.trim())
                    .password(passwordEncoder.encode(configuredPassword))
                    .roles("ADMIN")
                    .build();
        }
        return new InMemoryUserDetailsManager(admin);
    }
}
