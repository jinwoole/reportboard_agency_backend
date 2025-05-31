package agency.reportboard.backend.user.config;

import agency.reportboard.backend.common.dto.ApiResponse;
import agency.reportboard.backend.user.security.WebAuthnAuthenticationProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                .requestMatchers("/", "/index.html", "/static/**").permitAll()
                .requestMatchers("/api/auth/me").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    ObjectMapper mapper = new ObjectMapper();
                    ApiResponse<Object> errorResponse = ApiResponse.error("Not authenticated");
                    String jsonResponse = mapper.writeValueAsString(errorResponse);
                    response.getWriter().write(jsonResponse);
                    response.getWriter().flush();
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    ObjectMapper mapper = new ObjectMapper();
                    ApiResponse<Object> errorResponse = ApiResponse.error("Access denied");
                    String jsonResponse = mapper.writeValueAsString(errorResponse);
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
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
