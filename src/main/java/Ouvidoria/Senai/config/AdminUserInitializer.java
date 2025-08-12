package Ouvidoria.Senai.config;

import Ouvidoria.Senai.entities.CargoUsuario;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.repositories.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@senai.com}")
    private String adminEmail;
    
    @Value("${admin.password:}")
    private String adminPassword;
    
    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existe algum usuário ADMIN
        if (loginRepository.findByCargoUsuario(CargoUsuario.ADMIN).isEmpty()) {
            // Verifica se a senha do admin foi configurada
            if (adminPassword == null || adminPassword.trim().isEmpty()) {
                System.out.println("AVISO DE SEGURANÇA: Senha do administrador não configurada. Usando senha aleatória temporária.");
                // Gera uma senha aleatória se não foi configurada
                adminPassword = UUID.randomUUID().toString().substring(0, 12);
                System.out.println("Senha temporária do administrador: " + adminPassword);
                System.out.println("IMPORTANTE: Altere esta senha imediatamente após o primeiro login!");
            }
            
            Login adminUser = new Login();
            adminUser.setEmailEducacional(adminEmail);
            adminUser.setSenha(passwordEncoder.encode(adminPassword));
            adminUser.setCargoUsuario(CargoUsuario.ADMIN);

            loginRepository.save(adminUser);
            System.out.println("Usuário ADMIN padrão criado com email: " + adminEmail);
        }
    }
}