package br.com.classbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.classbook.model.Disciplina;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

}
