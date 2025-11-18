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
                            mvcMatcherBuilder.pattern("/sugestoes/**"),
                            mvcMatcherBuilder.pattern("/manifestacoes/**"),
                            mvcMatcherBuilder.pattern("/redefinir-senha**"),
                            mvcMatcherBuilder.pattern("/api/password/**")
                        ) // Ignora CSRF para login, manifestações e página de redefinição de senha
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
                        // Permite requisições OPTIONS (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Permite acesso total aos endpoints de login e cadastro
                        .requestMatchers(HttpMethod.POST, "/login/autenticar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/teste").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/cadastrar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/refresh-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/recuperar-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/password/forgot").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/password/reset").permitAll()
                        .requestMatchers(HttpMethod.GET, "/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/redefinir-senha").permitAll()
                        // Exige que o usuário tenha o cargo de ALUNO ou FUNCIONARIO para criar manifestações
                        .requestMatchers(HttpMethod.POST, "/denuncias/**").hasAnyAuthority("ALUNO", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/elogios/**").hasAnyAuthority("ALUNO", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/reclamacoes/**").hasAnyAuthority("ALUNO", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/sugestoes/**").hasAnyAuthority("ALUNO", "FUNCIONARIO")
                        // Permissões específicas para manutenção
                        .requestMatchers(HttpMethod.PUT, "/reclamacoes/**").hasAnyAuthority("ADMIN", "MANUTENCAO", "ALUNO", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/reclamacoes/**").authenticated()
                        // PUT adicionais
                        .requestMatchers(HttpMethod.PUT, "/denuncias/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/elogios/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/sugestoes/**").hasAnyAuthority("ADMIN")
                        // PATCH (ex.: /reclamacoes/{id}/status)
                        .requestMatchers(HttpMethod.PATCH, "/reclamacoes/**").hasAnyAuthority("ADMIN", "MANUTENCAO")
                        // DELETE por tipo (services farão validação fina por área)
                        .requestMatchers(HttpMethod.DELETE, "/denuncias/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/elogios/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/sugestoes/**").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/reclamacoes/**").hasAnyAuthority("ADMIN")
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
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", 
            "http://localhost:8080",
            "http://localhost:8081",
            "https://ouvidoria-senai-e9brd8b7gbg2a3f6.brazilsouth-01.azurewebsites.net",
            "https://ouvidoria-senai.vercel.app",
            "https://ouvidoria-senai-782uk18e0-luis-cantieris-projects.vercel.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    // Using Spring Boot's default mvcHandlerMappingIntrospector bean
}
