package br.com.classbook.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.classbook.model.MaterialDigital;

public interface MaterialDigitalRepository extends JpaRepository<MaterialDigital, Long> {

	boolean existsByDisciplinaId(Long disciplinaId);

	/**
	 * Sobrescrito para carregar a {@code disciplina} na mesma consulta: evita uma consulta extra por
	 * material ao converter a listagem para DTO (ver docs/MODELAGEM.md#materialdigital--disciplina).
	 */
	@Override
	@EntityGraph(attributePaths = "disciplina")
	List<MaterialDigital> findAll(Sort sort);

}
