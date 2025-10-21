package Ouvidoria.Senai.config;

import Ouvidoria.Senai.entities.CargoUsuario;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.repositories.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Lista de administradores padrão
        List<Login> administradores = Arrays.asList(
            criarAdmin("chile@docente.senai.br", "Senai@Chile115"),
            criarAdmin("vieira@docente.senai.br", "Senai@Vieira115"),
            criarAdmin("pino@docente.senai.br", "Senai@Pino115")
        );

        // Verifica e cria/atualiza cada administrador
        for (Login admin : administradores) {
            // Codifica a senha antes de verificar/salvar
            String senhaCodificada = passwordEncoder.encode(admin.getSenha());
            
            // Verifica se o usuário já existe
            Login usuarioExistente = loginRepository.findByEmailEducacional(admin.getEmailEducacional());
            
            if (usuarioExistente != null) {
                // Atualiza a senha se for diferente
                if (!passwordEncoder.matches(admin.getSenha(), usuarioExistente.getSenha())) {
                    usuarioExistente.setSenha(senhaCodificada);
                    loginRepository.save(usuarioExistente);
                }
            } else {
                // Cria um novo usuário
                admin.setSenha(senhaCodificada);
                loginRepository.save(admin);
            }
        }
    }

    private Login criarAdmin(String email, String senha) {
        Login login = new Login();
        login.setEmailEducacional(email);
        login.setSenha(senha); // Será codificada no repositório
        login.setCargoUsuario(CargoUsuario.ADMIN);
        return login;
    }
}
