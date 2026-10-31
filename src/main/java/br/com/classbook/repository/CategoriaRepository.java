package br.com.classbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.classbook.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	boolean existsByNomeIgnoreCase(String nome);

	/** Para o {@code PUT}: o nome da própria categoria não conta como conflito. */
	boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

}
