package br.com.classbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.classbook.model.Autor;

public interface AutorRepository extends JpaRepository<Autor, Long> {

}
