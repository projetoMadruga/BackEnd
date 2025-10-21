package Ouvidoria.Senai.config;

import Ouvidoria.Senai.repositories.LoginRepository;
import Ouvidoria.Senai.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private LoginRepository loginRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // Pula o filtro para requisições OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("SecurityFilter: Pulando requisição OPTIONS para " + requestURI);
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Pula o filtro para endpoints públicos de login
        if (requestURI.startsWith("/login/")) {
            System.out.println("SecurityFilter: Pulando endpoint público: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        var token = this.recoverToken(request);
        if (token != null) {
            var email = tokenService.validarToken(token);
            // Verifica se o email não está vazio (token válido)
            if (email != null && !email.isEmpty()) {
                UserDetails usuario = loginRepository.findFirstByEmailEducacional(email);

                if (usuario != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        // O token vem depois de "Bearer "
        return authHeader.replace("Bearer ", "");
    }
}