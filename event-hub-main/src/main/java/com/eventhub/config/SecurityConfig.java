package com.eventhub.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig
{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/login", "/register", "/error").permitAll()
                        .requestMatchers("/dashboard", "/event-form", "/venue-form", "/venues", "/venues/**").hasRole("ORGANIZER")
                        .requestMatchers("/events/*/edit").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/events").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.PUT, "/events/*", "/events/*/visibility").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/events/*").hasRole("ORGANIZER")
                        .requestMatchers("/my-bookings", "/events/*/book", "/bookings/*/cancel").hasRole("ATTENDEE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/")
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}
