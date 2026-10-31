package br.com.classbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.classbook.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

}
