package com.facens.academia.repository;

import com.facens.academia.entity.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, Long> {

    // Busca planos ativos
    List<Plano> findByAtivoTrue();

    // Verifica se já existe plano com esse nome
    boolean existsByNome(String nome);

    // Busca plano por nome (ignorando maiúsculas/minúsculas)
    Optional<Plano> findByNomeIgnoreCase(String nome);

    // Busca planos com alunos matriculados
    @Query("SELECT p FROM Plano p WHERE SIZE(p.alunos) > 0")
    List<Plano> findPlanosComAlunos();
}
