package training.aidd.library.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(basic -> {})
            .authorizeHttpRequests(auth -> auth
                // 書誌削除・現物削除は CHIEF 以上
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("CHIEF", "DIRECTOR")
                // 職員管理は DIRECTOR のみ
                .requestMatchers("/api/staff/**").hasRole("DIRECTOR")
                // 貸出ルール変更は CHIEF 以上
                .requestMatchers(HttpMethod.PUT, "/api/loan-rules/**").hasAnyRole("CHIEF", "DIRECTOR")
                // その他は全職員（認証済み）
                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
