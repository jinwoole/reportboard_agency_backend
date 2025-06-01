package agency.reportboard.backend.user.config;

import agency.reportboard.backend.user.security.WebAuthnAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final WebAuthnAuthenticationProvider webAuthnAuthenticationProvider;
    
    public SecurityConfig(WebAuthnAuthenticationProvider webAuthnAuthenticationProvider) {
        this.webAuthnAuthenticationProvider = webAuthnAuthenticationProvider;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/register-options", "/api/auth/register", "/api/auth/login-options", "/api/auth/login", "/api/auth/logout").permitAll()
                .requestMatchers("/", "/index.html", "/dashboard.html").permitAll()
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("*.css", "*.js", "*.png", "*.jpg", "*.ico").permitAll()
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers("/api/memo/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    String jsonResponse = "{\"error\":\"Not authenticated\"}";
                    response.getWriter().write(jsonResponse);
                    response.getWriter().flush();
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    String jsonResponse = "{\"error\":\"Access denied\"}";
                    response.getWriter().write(jsonResponse);
                    response.getWriter().flush();
                })
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .authenticationProvider(webAuthnAuthenticationProvider);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // SvelteKit 개발 서버와 프로덕션 도메인 허용
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",  // SvelteKit 개발 서버
            "http://localhost:3000",  // SvelteKit 프로덕션 (Node.js)
            "http://localhost:4173"   // SvelteKit 프리뷰
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 세션 쿠키 전송을 위해 필요
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
