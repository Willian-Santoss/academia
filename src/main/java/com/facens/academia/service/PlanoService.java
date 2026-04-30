package com.facens.academia.service;

import com.facens.academia.dto.request.PlanoRequest;
import com.facens.academia.dto.response.PlanoResponse;
import com.facens.academia.entity.Plano;
import com.facens.academia.exception.RecursoNaoEncontradoException;
import com.facens.academia.exception.RegraNegocioException;
import com.facens.academia.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanoService {

    // Injeção de Dependência via construtor (gerada pelo @RequiredArgsConstructor do Lombok)
    private final PlanoRepository planoRepository;

    // -------------------------------------------------------
    // CADASTRAR PLANO
    // -------------------------------------------------------
    @Transactional
    public PlanoResponse cadastrar(PlanoRequest request) {

        // Regra de negócio: nome do plano não pode ser duplicado
        if (planoRepository.existsByNome(request.getNome())) {
            throw new RegraNegocioException(
                    "Já existe um plano cadastrado com o nome: " + request.getNome()
            );
        }

        // Regra de negócio: valor mensal deve ser maior que zero
        if (request.getValorMensal().signum() <= 0) {
            throw new RegraNegocioException("O valor mensal deve ser maior que zero.");
        }

        Plano plano = Plano.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .valorMensal(request.getValorMensal())
                .duracaoMeses(request.getDuracaoMeses())
                .ativo(true)
                .build();

        Plano salvo = planoRepository.save(plano);
        return toResponse(salvo);
    }

    // -------------------------------------------------------
    // CONSULTAR TODOS OS PLANOS
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<PlanoResponse> listarTodos() {
        return planoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // CONSULTAR PLANOS ATIVOS
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<PlanoResponse> listarAtivos() {
        return planoRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // CONSULTAR PLANO POR ID
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public PlanoResponse buscarPorId(Long id) {
        Plano plano = planoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano", id));
        return toResponse(plano);
    }

    // -------------------------------------------------------
    // ATUALIZAR PLANO
    // -------------------------------------------------------
    @Transactional
    public PlanoResponse atualizar(Long id, PlanoRequest request) {
        Plano plano = planoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano", id));

        // Regra de negócio: novo nome não pode pertencer a outro plano
        planoRepository.findByNomeIgnoreCase(request.getNome())
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new RegraNegocioException(
                                "Já existe outro plano com o nome: " + request.getNome()
                        );
                    }
                });

        plano.setNome(request.getNome());
        plano.setDescricao(request.getDescricao());
        plano.setValorMensal(request.getValorMensal());
        plano.setDuracaoMeses(request.getDuracaoMeses());

        return toResponse(planoRepository.save(plano));
    }

    // -------------------------------------------------------
    // DESATIVAR PLANO (exclusão lógica)
    // -------------------------------------------------------
    @Transactional
    public void desativar(Long id) {
        Plano plano = planoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano", id));

        // Regra de negócio: não pode desativar plano com alunos ativos
        boolean possuiAlunosAtivos = plano.getAlunos().stream()
                .anyMatch(aluno -> Boolean.TRUE.equals(aluno.getAtivo()));

        if (possuiAlunosAtivos) {
            throw new RegraNegocioException(
                    "Não é possível desativar o plano pois existem alunos ativos vinculados a ele."
            );
        }

        plano.setAtivo(false);
        planoRepository.save(plano);
    }

    // -------------------------------------------------------
    // Método auxiliar: busca entidade (usado pelo AlunoService)
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public Plano buscarEntidadePorId(Long id) {
        return planoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano", id));
    }

    // -------------------------------------------------------
    // Conversão Entidade -> Response DTO
    // -------------------------------------------------------
    private PlanoResponse toResponse(Plano plano) {
        return PlanoResponse.builder()
                .id(plano.getId())
                .nome(plano.getNome())
                .descricao(plano.getDescricao())
                .valorMensal(plano.getValorMensal())
                .duracaoMeses(plano.getDuracaoMeses())
                .ativo(plano.getAtivo())
                .totalAlunos(plano.getAlunos() != null ? plano.getAlunos().size() : 0)
                .build();
    }
}
