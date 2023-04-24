package com.projeto.codecc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.projeto.codecc.model.Tema;

public interface TemaRepository extends JpaRepository<Tema, Long> {

	/*List<Tema> findAllByDescricaoContainingIgnoreCase(@Param("descricao") String tema);*/
	
	List<Tema> findAllByTemaContainingIgnoreCase(@Param("tema") String tema);
}

