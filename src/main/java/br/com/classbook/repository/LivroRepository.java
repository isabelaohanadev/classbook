package br.com.classbook.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.classbook.model.Livro;

public interface LivroRepository extends JpaRepository<Livro, Long> {

	boolean existsByIsbn(String isbn);

	boolean existsByAutorId(Long autorId);

	/**
	 * Sobrescrito para carregar {@code autor} (e, a partir da Issue #26, a categoria) na mesma
	 * consulta: evita uma consulta extra por livro ao converter a listagem para DTO (ver
	 * docs/MODELAGEM.md#regras-comuns-às-relações).
	 */
	@Override
	@EntityGraph(attributePaths = "autor")
	List<Livro> findAll(Sort sort);

}
