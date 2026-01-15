package com.hth.udecareer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.hth.udecareer.security.JwtAuthenticationEntryPoint;
import com.hth.udecareer.security.JwtRequestFilter;
import com.hth.udecareer.security.PhpPasswordEncoder;

import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService jwtUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final DynamicCorsFilter dynamicCorsFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PhpPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http,
            PasswordEncoder passwordEncoder)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(daoAuthenticationProvider())
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf().disable()
                .cors().configurationSource(corsConfigurationSource).and()
                .authenticationProvider(daoAuthenticationProvider())
                // Add DynamicCorsFilter first to handle CORS with pattern matching (*.vercel.app)
                .addFilterBefore(dynamicCorsFilter, SecurityContextPersistenceFilter.class)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeRequests();
        http.authorizeRequests()
                .antMatchers("/ws/**").permitAll()
                .antMatchers("/notifications.html").permitAll()
                .antMatchers("/send-notification.html").permitAll()
                .antMatchers("/webhook/**").permitAll()
                .antMatchers("/hooks/**").permitAll()
                .antMatchers("/css/**", "/js/**", "/img/**", "/lib/**", "/favicon.ico").permitAll()
                .antMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html",
                        "/webjars/**",
                        "/actuator/**")
                .permitAll()
                // Public authentication endpoints (no auth required)
                .antMatchers("/authenticate", "/signup", "/register", "/verification-code/**",
                        "/api/user/reset-pass", "/api/version/check-update","/metadata/**", "/api/comments/root", "/api/comments/*/replies")
                .permitAll()
                .antMatchers("/api/post/**").permitAll()
                .antMatchers("/auth/google/**").permitAll()
                // Auth API endpoints (includes /api/auth/authenticate alias)
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/payment/vnpay-ipn").permitAll()
                .antMatchers("/api/payment/vnpay-status").permitAll()
                .antMatchers("/api/notifications/test-send").permitAll() // Test notification endpoint
                .antMatchers("/api/courses-pagination", "/**/courses-pagination").permitAll()
                .antMatchers("/api/support/channels", "/**/support/channels").permitAll()
                .antMatchers("/api/quiz/category/public").permitAll()
                .antMatchers("/uploads/**").permitAll()
                .antMatchers("/api/cross-sale/apps").permitAll()
                .antMatchers("/api/home/banner").permitAll()
                .antMatchers("/api/spaces", "/api/spaces/{spacesId}/by-id", "/api/spaces/{slug}/by-slug").permitAll()
                .antMatchers("/api/feeds").permitAll()
                .antMatchers("/api/feeds/**").permitAll()
                .antMatchers("/api/commentsFeed/**").permitAll()
                .antMatchers("/api/gamification/leaderboard").permitAll()
                .antMatchers("/api/gamification/leaderboard/**").permitAll()
                .antMatchers("/api/leaderboard/**").permitAll()
                .antMatchers("/api/streak/leaderboard").permitAll()
                .antMatchers("/api/badges").permitAll()
                .antMatchers("/api/badges/type/**").permitAll()
                .antMatchers("/api/badges/by-slug/**").permitAll()
                .antMatchers("/api/badges/*/earners").permitAll()
                .antMatchers("/api/badges/user/**").permitAll()
                .antMatchers("/api/topics").permitAll()
                .antMatchers("/api/topics/**").permitAll()
                // EIL Certification Skills API - public for web and app
                .antMatchers("/api/certifications").permitAll()
                .antMatchers("/api/certifications/**").permitAll()
                .antMatchers("/certifications").permitAll()
                .antMatchers("/certifications/**").permitAll()
                .antMatchers("/go/**").permitAll() // Allow affiliate redirect links without authentication
                .anyRequest().authenticated()
                .and()
                .formLogin().disable()
                .logout().disable()
                .httpBasic().disable()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(jwtUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
