package Ouvidoria.Senai.config;

import Ouvidoria.Senai.entities.CargoUsuario;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.repositories.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import Ouvidoria.Senai.entities.NivelAcesso;
import Ouvidoria.Senai.entities.Area;
import Ouvidoria.Senai.entities.PermissaoAreaUsuario;
import Ouvidoria.Senai.repositories.PermissaoAreaUsuarioRepository;

import java.util.EnumMap;
import java.util.Map;

@Component
public class InicializadorAdmin implements CommandLineRunner {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissaoAreaUsuarioRepository permissaoAreaUsuarioRepository;

    @Value("${admin.senha-padrao:}")
    private String senhaPadraoAdmin;
    
    @Override
    public void run(String... args) throws Exception {
        // Cria/atualiza 3 usuários ADMIN específicos com permissões por área
        criarAdmin(
                "chile@docente.senai.br",
                mapear(
                        Area.ADS_REDES, NivelAcesso.EDITAR,
                        Area.MANUFATURA_DIGITAL, NivelAcesso.EDITAR,
                        Area.MECANICA, NivelAcesso.VISUALIZAR,
                        Area.FACULDADE_SENAI, NivelAcesso.VISUALIZAR
                )
        );

        criarAdmin(
                "vieira@docente.senai.br",
                mapear(
                        Area.FACULDADE_SENAI, NivelAcesso.EDITAR,
                        Area.MECANICA, NivelAcesso.VISUALIZAR,
                        Area.MANUFATURA_DIGITAL, NivelAcesso.VISUALIZAR
                )
        );

        criarAdmin(
                "pino@docente.senai.br",
                mapear(
                        Area.MECANICA, NivelAcesso.EDITAR,
                        Area.MANUFATURA_DIGITAL, NivelAcesso.EDITAR,
                        Area.FACULDADE_SENAI, NivelAcesso.VISUALIZAR
                )
        );
    }

    private void criarAdmin(String email, Map<Area, NivelAcesso> permissoes) {
        Login usuario = loginRepository.findFirstByEmailEducacional(email);
        if (usuario == null) {
            String senha = (senhaPadraoAdmin != null && !senhaPadraoAdmin.trim().isEmpty())
                    ? senhaPadraoAdmin
                    : "Senai@" + email.split("@")[0] + "115";
            
            usuario = new Login();
            usuario.setEmailEducacional(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setCargoUsuario(CargoUsuario.ADMIN);
            loginRepository.save(usuario);
        } else if (usuario.getCargoUsuario() != CargoUsuario.ADMIN) {
            usuario.setCargoUsuario(CargoUsuario.ADMIN);
            loginRepository.save(usuario);
        }

        // Atualiza permissões por área
        for (Map.Entry<Area, NivelAcesso> entrada : permissoes.entrySet()) {
            Area area = entrada.getKey();
            NivelAcesso nivel = entrada.getValue();
            PermissaoAreaUsuario permissao = permissaoAreaUsuarioRepository
                    .findByUsuarioAndArea(usuario, area)
                    .orElse(new PermissaoAreaUsuario(usuario, area, nivel));
            permissao.setNivelAcesso(nivel);
            permissaoAreaUsuarioRepository.save(permissao);
        }
    }

    private Map<Area, NivelAcesso> mapear(Object... chaveValor) {
        Map<Area, NivelAcesso> mapa = new EnumMap<>(Area.class);
        for (int i = 0; i < chaveValor.length; i += 2) {
            mapa.put((Area) chaveValor[i], (NivelAcesso) chaveValor[i + 1]);
        }
        return mapa;
    }
}
