package Ouvidoria.Senai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Arrays;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        // Create MvcRequestMatcher.Builder with the introspector
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
        
        return http
                // Habilita CSRF apenas para os endpoints de login e desabilita para as APIs
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                            mvcMatcherBuilder.pattern("/login/**"), 
                            mvcMatcherBuilder.pattern("/denuncias/**"),
                            mvcMatcherBuilder.pattern("/elogios/**"),
                            mvcMatcherBuilder.pattern("/reclamacoes/**"),
                            mvcMatcherBuilder.pattern("/sugestoes/**")
                        ) // Ignora CSRF para login e todas as manifestações
                )
                // Configura CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configura gerenciamento de sessão como STATELESS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configura cabeçalhos de segurança
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; img-src 'self'; style-src 'self'; frame-ancestors 'none'")
                        )
                        .frameOptions(frame -> frame.deny())
                        // XSS Protection removido temporariamente
                        .cacheControl(cache -> cache.disable())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acesso total aos endpoints de login e cadastro
                        .requestMatchers(HttpMethod.POST, "/login/autenticar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/teste").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/cadastrar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/refresh-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/recuperar-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/redefinir-senha").permitAll()
                        // Exige que o usuário tenha o cargo de ALUNO ou FUNCIONARIO para criar manifestações
                        .requestMatchers(HttpMethod.POST, "/denuncias/**").hasAnyAuthority("ALUNO", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/elogios/**").hasAnyAuthority("ALUNO", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/reclamacoes/**").hasAnyAuthority("ALUNO", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/sugestoes/**").hasAnyAuthority("ALUNO", "FUNCIONARIO")
                        // Permissões específicas para manutenção
                        .requestMatchers(HttpMethod.PUT, "/reclamacoes/**").hasAnyAuthority("ADMIN", "MANUTENCAO", "ALUNO", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/reclamacoes/**").authenticated()
                        // Permissões para admin
                        .requestMatchers(HttpMethod.GET, "/denuncias/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/elogios/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sugestoes/**").hasAnyAuthority("ADMIN")
                        // Qualquer outra requisição precisa de autenticação
                        .anyRequest().authenticated()
                )
                // Adiciona nosso filtro personalizado antes do filtro padrão do Spring
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    // Using Spring Boot's default mvcHandlerMappingIntrospector bean
}
