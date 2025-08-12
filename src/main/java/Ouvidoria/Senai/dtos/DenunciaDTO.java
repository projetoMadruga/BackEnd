package Ouvidoria.Senai.dtos;

import Ouvidoria.Senai.entities.Denuncia;

public class DenunciaDTO {

	private Long id;
	private String local;
	private String dataHora;
	private String descricaoDetalhada;
	private String caminhoAnexo;

	// Campo opcional para o frontend saber quem criou a denúncia
	private String emailUsuario;

	public DenunciaDTO() {
	}

	public DenunciaDTO(Long id, String local, String dataHora, String descricaoDetalhada, String caminhoAnexo, String emailUsuario) {
		this.id = id;
		this.local = local;
		this.dataHora = dataHora;
		this.descricaoDetalhada = descricaoDetalhada;
		this.caminhoAnexo = caminhoAnexo;
		this.emailUsuario = emailUsuario;
	}

	// O construtor que mapeia a ENTIDADE para o DTO.
	// Isto corrige o erro "'DenunciaDTO(...)' cannot be applied to '(Ouvidoria.Senai.entities.Denuncia)'"
	public DenunciaDTO(Denuncia entity) {
		this.id = entity.getId();
		this.descricaoDetalhada = entity.getDescricaoDetalhada();
		this.caminhoAnexo = entity.getCaminhoAnexo();
		this.local = entity.getLocal();
		this.dataHora = entity.getDataHora();
		if (entity.getUsuario() != null) {
			this.emailUsuario = entity.getUsuario().getEmailEducacional();
		}
	}



	// Getters e Setters para TODOS os campos

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getDataHora() {
		return dataHora;
	}

	public void setDataHora(String dataHora) {
		this.dataHora = dataHora;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getDescricaoDetalhada() { return descricaoDetalhada; }
	public void setDescricaoDetalhada(String descricaoDetalhada) { this.descricaoDetalhada = descricaoDetalhada; }
	public String getCaminhoAnexo() { return caminhoAnexo; }
	public void setCaminhoAnexo(String caminhoAnexo) { this.caminhoAnexo = caminhoAnexo; }
	public String getEmailUsuario() { return emailUsuario; }
	public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }
}