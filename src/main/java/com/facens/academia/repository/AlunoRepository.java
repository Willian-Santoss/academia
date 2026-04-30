package com.facens.academia.repository;

import com.facens.academia.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    // Busca por e-mail
    Optional<Aluno> findByEmail(String email);

    // Verifica se já existe aluno com esse e-mail (para validação)
    boolean existsByEmail(String email);

    // Verifica se já existe aluno com esse CPF
    boolean existsByCpf(String cpf);

    // Lista alunos ativos
    List<Aluno> findByAtivoTrue();

    // Lista alunos por plano
    List<Aluno> findByPlanoId(Long planoId);

    // Lista alunos ativos por plano
    List<Aluno> findByPlanoIdAndAtivoTrue(Long planoId);
}
