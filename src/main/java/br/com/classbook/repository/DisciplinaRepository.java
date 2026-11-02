package br.com.classbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.classbook.model.Disciplina;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

	boolean existsByCodigo(String codigo);

	/** Para o {@code PUT}: o código da própria disciplina não conta como conflito. */
	boolean existsByCodigoAndIdNot(String codigo, Long id);

}
