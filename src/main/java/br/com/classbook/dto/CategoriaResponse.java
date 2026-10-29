package br.com.classbook.dto;

import br.com.classbook.model.Categoria;
import br.com.classbook.model.TipoCategoria;

/**
 * Saída de todas as rotas de {@code /api/categorias}.
 */
public record CategoriaResponse(Long id, String nome, String descricao, String faixaEtaria, TipoCategoria tipo) {

	public static CategoriaResponse de(Categoria categoria) {
		return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao(),
				categoria.getFaixaEtaria(), categoria.getTipo());
	}

}
