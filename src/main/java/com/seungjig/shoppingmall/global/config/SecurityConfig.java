package com.seungjig.shoppingmall.global.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/productImage/**").permitAll()
                        .requestMatchers("/", "/users/signup", "/index.html", "/users/signin", "/favicon.ico", "/users/logout", "/products/list").permitAll()
                        .requestMatchers("/products/create", "/products/admin/list", "/products/update", "/products/update/**", "/products/delete/**").hasRole("ADMIN")
                        .requestMatchers("/products/**").permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/users/signin")
                        .loginProcessingUrl("/users/signin")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestedWith = request.getHeader("X-Requested-With");

                            if ("XMLHttpRequest".equals(requestedWith)) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"message\": \"로그인이 필요한 서비스입니다.\"}");
                            } else {
                                response.sendRedirect("/users/signin");
                            }
                        })

                );

        return http.build();
    }
}
