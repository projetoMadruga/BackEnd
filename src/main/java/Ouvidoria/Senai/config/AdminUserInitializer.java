package Ouvidoria.Senai.config;

import Ouvidoria.Senai.entities.CargoUsuario;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.repositories.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import Ouvidoria.Senai.entities.AccessLevel;
import Ouvidoria.Senai.entities.Area;
import Ouvidoria.Senai.entities.UserAreaPermission;
import Ouvidoria.Senai.repositories.UserAreaPermissionRepository;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAreaPermissionRepository userAreaPermissionRepository;

    @Value("${admin.default-password:}")
    private String defaultAdminPassword;
    
    @Override
    public void run(String... args) throws Exception {
        // Cria/atualiza 3 usuários ADMIN específicos com permissões por área
        seedAdmin(
                "chile@docente.senai.br",
                mapOf(
                        Area.ADS_REDES, AccessLevel.EDIT,
                        Area.MANUFATURA_DIGITAL, AccessLevel.EDIT,
                        Area.MECANICA, AccessLevel.VIEW,
                        Area.FACULDADE_SENAI, AccessLevel.VIEW
                )
        );

        seedAdmin(
                "vieira@docente.senai.br",
                mapOf(
                        Area.FACULDADE_SENAI, AccessLevel.EDIT,
                        Area.MECANICA, AccessLevel.VIEW,
                        Area.MANUFATURA_DIGITAL, AccessLevel.VIEW
                )
        );

        seedAdmin(
                "pino@docente.senai.br",
                mapOf(
                        Area.MECANICA, AccessLevel.EDIT,
                        Area.MANUFATURA_DIGITAL, AccessLevel.EDIT,
                        Area.FACULDADE_SENAI, AccessLevel.VIEW
                )
        );
    }

    private void seedAdmin(String email, Map<Area, AccessLevel> permissions) {
        Login user = loginRepository.findByEmailEducacional(email);
        if (user == null) {
            String rawPass = (defaultAdminPassword != null && !defaultAdminPassword.trim().isEmpty())
                    ? defaultAdminPassword
                    : UUID.randomUUID().toString().substring(0, 12);

            if (defaultAdminPassword == null || defaultAdminPassword.trim().isEmpty()) {
                System.out.println("AVISO: Criando usuário ADMIN '" + email + "' com senha temporária: " + rawPass);
            }

            user = new Login();
            user.setEmailEducacional(email);
            user.setSenha(passwordEncoder.encode(rawPass));
            user.setCargoUsuario(CargoUsuario.ADMIN);
            user = loginRepository.save(user);
            System.out.println("Usuário ADMIN criado: " + email);
        } else {
            if (user.getCargoUsuario() != CargoUsuario.ADMIN) {
                user.setCargoUsuario(CargoUsuario.ADMIN);
                loginRepository.save(user);
                System.out.println("Usuário existente promovido a ADMIN: " + email);
            }
        }

        // Upsert permissões por área
        for (Map.Entry<Area, AccessLevel> entry : permissions.entrySet()) {
            Area area = entry.getKey();
            AccessLevel level = entry.getValue();
            UserAreaPermission perm = userAreaPermissionRepository
                    .findByUserAndArea(user, area)
                    .orElse(new UserAreaPermission(user, area, level));
            perm.setAccessLevel(level);
            userAreaPermissionRepository.save(perm);
        }
    }

    private Map<Area, AccessLevel> mapOf(Object... kv) {
        Map<Area, AccessLevel> map = new EnumMap<>(Area.class);
        for (int i = 0; i < kv.length; i += 2) {
            map.put((Area) kv[i], (AccessLevel) kv[i + 1]);
        }
        return map;
    }
}