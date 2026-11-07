package br.com.classbook.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
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

	boolean existsByDisciplinaId(Long disciplinaId);

	/**
	 * Pesquisa avançada (Issues #27 e #28): {@code titulo}, {@code busca}, {@code autorId} e
	 * {@code categoriaId} são opcionais e combináveis (E lógico). {@code autorId}/{@code categoriaId}
	 * nulos não filtram; {@code titulo} e {@code busca} vazios (nunca {@code null}, ver
	 * {@link br.com.classbook.service.LivroService}) também não filtram.
	 *
	 * <p>{@code titulo} procura só no título do livro. {@code busca} é a busca livre: o texto pode estar no
	 * título, no nome do autor, no nome da categoria ou no ano de publicação (o ano é comparado como texto,
	 * então "189" acha 1899). Os dois chegam com {@code %}, {@code _} e {@code \} já escapados, para o
	 * {@code LIKE} procurá-los como texto, e nunca {@code null}, porque o PostgreSQL não consegue tipar
	 * {@code lower(concat(...))} com um parâmetro nulo.
	 *
	 * <p>Autor, categoria e disciplina vêm na mesma consulta ({@code join fetch}), para não gerar uma
	 * consulta extra por livro ao converter a listagem para DTO (ver
	 * docs/MODELAGEM.md#regras-comuns-às-relações). A categoria e a disciplina são opcionais, então o join
	 * é {@code left}: um {@code join} simples esconderia os livros sem categoria ou sem disciplina, mesmo
	 * quando o título bate com a busca.
	 */
	@Query("""
			select l from Livro l
			join fetch l.autor a
			left join fetch l.categoria c
			left join fetch l.disciplina d
			where (:titulo = '' or lower(l.titulo) like lower(concat('%', :titulo, '%')) escape '\\')
			  and (:busca = ''
			       or lower(l.titulo) like lower(concat('%', :busca, '%')) escape '\\'
			       or lower(a.nome) like lower(concat('%', :busca, '%')) escape '\\'
			       or lower(c.nome) like lower(concat('%', :busca, '%')) escape '\\'
			       or cast(l.anoPublicacao as string) like concat('%', :busca, '%') escape '\\')
			  and (:autorId is null or a.id = :autorId)
			  and (:categoriaId is null or c.id = :categoriaId)
			""")
	List<Livro> pesquisar(@Param("titulo") String titulo, @Param("busca") String busca,
			@Param("autorId") Long autorId, @Param("categoriaId") Long categoriaId, Sort sort);

}
