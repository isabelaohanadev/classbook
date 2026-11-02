package br.com.classbook.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.classbook.model.Livro;

public interface LivroRepository extends JpaRepository<Livro, Long> {

	boolean existsByIsbn(String isbn);

	/** Para o {@code PUT}: o ISBN do próprio livro não conta como conflito. */
	boolean existsByIsbnAndIdNot(String isbn, Long id);

	boolean existsByAutorId(Long autorId);

	boolean existsByCategoriaId(Long categoriaId);

	/**
	 * Pesquisa avançada (Issues #27 e #28): {@code titulo}, {@code autorId} e {@code categoriaId} são
	 * opcionais e combináveis (E lógico). {@code autorId}/{@code categoriaId} nulos não filtram;
	 * {@code titulo} vazio (nunca {@code null} — ver {@link br.com.classbook.service.LivroService}) também
	 * não filtra. {@code titulo} nunca é {@code null} aqui porque o PostgreSQL não consegue tipar
	 * {@code lower(concat(...))} com um parâmetro nulo (ver o service). Carrega {@code autor} e
	 * {@code categoria} na mesma consulta, para não gerar uma consulta extra por livro ao converter a
	 * listagem para DTO (ver docs/MODELAGEM.md#regras-comuns-às-relações).
	 */
	@EntityGraph(attributePaths = {"autor", "categoria"})
	@Query("""
			select l from Livro l
			where (:titulo = '' or lower(l.titulo) like lower(concat('%', :titulo, '%')))
			  and (:autorId is null or l.autor.id = :autorId)
			  and (:categoriaId is null or l.categoria.id = :categoriaId)
			""")
	List<Livro> pesquisar(@Param("titulo") String titulo, @Param("autorId") Long autorId,
			@Param("categoriaId") Long categoriaId, Sort sort);

}
