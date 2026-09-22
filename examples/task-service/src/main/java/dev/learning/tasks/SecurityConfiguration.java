package dev.learning.tasks;

import java.util.Map;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService users(PasswordEncoder encoder,
            @Value("${learning.security.writer-password}") String writerPassword,
            @Value("${learning.security.reader-password}") String readerPassword) {
        // 只用于本地教学。真实系统接入身份提供方，并设计每个资源的所有权检查。
        return new InMemoryUserDetailsManager(
                User.withUsername("writer").password(encoder.encode(writerPassword)).roles("READER", "WRITER", "OPS").build(),
                User.withUsername("reader").password(encoder.encode(readerPassword)).roles("READER").build());
    }

    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        // 放行容器的错误转发，避免 403 再被 /error 的授权规则改写为 401。
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("OPS")
                        .requestMatchers(HttpMethod.GET, "/api/csrf").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/tasks", "/api/tasks/**").hasRole("READER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks").hasRole("WRITER")
                        .requestMatchers(HttpMethod.PATCH, "/api/tasks/*/status").hasRole("WRITER")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                // Basic 凭证也可能被浏览器自动携带，不能以“这是REST”为由关闭 CSRF。
                .csrf(Customizer.withDefaults())
                .build();
    }

    @RestController
    static class CsrfController {
        @GetMapping("/api/csrf")
        Map<String, String> csrf(CsrfToken token) {
            return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
        }
    }
}
