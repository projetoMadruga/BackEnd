package Ouvidoria.Senai.dtos;

import Ouvidoria.Senai.entities.Elogio;

public class ElogioDTO {

	private Long id;
	private String descricaoDetalhada;
	private String caminhoAnexo;
	private String emailUsuario; // Para o frontend saber quem criou
	private String local;
	private String dataHora;

	public ElogioDTO() {
	}

	public ElogioDTO(Long id, String descricaoDetalhada, String caminhoAnexo, String emailUsuario, String local, String dataHora) {
		this.id = id;
		this.descricaoDetalhada = descricaoDetalhada;
		this.caminhoAnexo = caminhoAnexo;
		this.emailUsuario = emailUsuario;
		this.local = local;
		this.dataHora = dataHora;
	}

	// O construtor que faltava: mapeia a ENTIDADE para o DTO.
	public ElogioDTO(Elogio entity) {
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
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
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
	public String getDescricaoDetalhada() { return descricaoDetalhada; }
	public void setDescricaoDetalhada(String descricaoDetalhada) { this.descricaoDetalhada = descricaoDetalhada; }
	public String getCaminhoAnexo() { return caminhoAnexo; }
	public void setCaminhoAnexo(String caminhoAnexo) { this.caminhoAnexo = caminhoAnexo; }
	public String getEmailUsuario() { return emailUsuario; }
	public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }
}