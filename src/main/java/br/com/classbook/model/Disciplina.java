package br.com.classbook.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Disciplina escolar, mapeada para a tabela {@code disciplina} (ver docs/MODELAGEM.md). O código é
 * único, garantido no banco por {@code uk_disciplina_codigo}; o nome não é, porque a mesma disciplina
 * existe em séries diferentes. Sem relacionamento bidirecional: não guarda a lista de materiais.
 */
@Entity
public class Disciplina {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nome;

	private String codigo;

	private String descricao;

	@Enumerated(EnumType.STRING)
	private SerieEscolar serieEscolar;

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public SerieEscolar getSerieEscolar() {
		return serieEscolar;
	}

	public void setSerieEscolar(SerieEscolar serieEscolar) {
		this.serieEscolar = serieEscolar;
	}

}
