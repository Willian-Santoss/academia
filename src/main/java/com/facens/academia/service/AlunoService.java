package com.facens.academia.service;

import com.facens.academia.dto.request.AlunoRequest;
import com.facens.academia.dto.response.AlunoResponse;
import com.facens.academia.entity.Aluno;
import com.facens.academia.entity.Plano;
import com.facens.academia.exception.RecursoNaoEncontradoException;
import com.facens.academia.exception.RegraNegocioException;
import com.facens.academia.repository.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlunoService {

    // Injeção de Dependência via construtor (IoC)
    private final AlunoRepository alunoRepository;
    private final PlanoService planoService;

    // -------------------------------------------------------
    // CADASTRAR ALUNO
    // -------------------------------------------------------
    @Transactional
    public AlunoResponse cadastrar(AlunoRequest request) {

        // Regra de negócio: e-mail não pode ser duplicado
        if (alunoRepository.existsByEmail(request.getEmail())) {
            throw new RegraNegocioException(
                    "Já existe um aluno cadastrado com o e-mail: " + request.getEmail()
            );
        }

        // Regra de negócio: CPF não pode ser duplicado
        if (alunoRepository.existsByCpf(request.getCpf())) {
            throw new RegraNegocioException(
                    "Já existe um aluno cadastrado com o CPF: " + request.getCpf()
            );
        }

        // Regra de negócio: plano deve existir e estar ativo
        Plano plano = planoService.buscarEntidadePorId(request.getPlanoId());

        if (Boolean.FALSE.equals(plano.getAtivo())) {
            throw new RegraNegocioException(
                    "Não é possível matricular aluno em um plano inativo."
            );
        }

        Aluno aluno = Aluno.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .cpf(request.getCpf())
                .telefone(request.getTelefone())
                .dataNascimento(request.getDataNascimento())
                .dataMatricula(LocalDateTime.now())
                .ativo(true)
                .plano(plano)
                .build();

        Aluno salvo = alunoRepository.save(aluno);
        return toResponse(salvo);
    }

    // -------------------------------------------------------
    // CONSULTAR TODOS OS ALUNOS
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AlunoResponse> listarTodos() {
        return alunoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // CONSULTAR ALUNOS ATIVOS
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AlunoResponse> listarAtivos() {
        return alunoRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // CONSULTAR ALUNO POR ID
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public AlunoResponse buscarPorId(Long id) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));
        return toResponse(aluno);
    }

    // -------------------------------------------------------
    // CONSULTAR ALUNO POR E-MAIL
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public AlunoResponse buscarPorEmail(String email) {
        Aluno aluno = alunoRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Aluno não encontrado com o e-mail: " + email
                ));
        return toResponse(aluno);
    }

    // -------------------------------------------------------
    // CONSULTAR ALUNOS POR PLANO
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AlunoResponse> listarPorPlano(Long planoId) {
        // Valida se o plano existe antes de buscar
        planoService.buscarEntidadePorId(planoId);

        return alunoRepository.findByPlanoId(planoId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // ATUALIZAR ALUNO
    // -------------------------------------------------------
    @Transactional
    public AlunoResponse atualizar(Long id, AlunoRequest request) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));

        // Regra de negócio: novo e-mail não pode pertencer a outro aluno
        alunoRepository.findByEmail(request.getEmail())
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new RegraNegocioException(
                                "Já existe outro aluno com o e-mail: " + request.getEmail()
                        );
                    }
                });

        // Valida e troca plano, se necessário
        if (!aluno.getPlano().getId().equals(request.getPlanoId())) {
            Plano novoPlano = planoService.buscarEntidadePorId(request.getPlanoId());
            if (Boolean.FALSE.equals(novoPlano.getAtivo())) {
                throw new RegraNegocioException("Não é possível migrar para um plano inativo.");
            }
            aluno.setPlano(novoPlano);
        }

        aluno.setNome(request.getNome());
        aluno.setEmail(request.getEmail());
        aluno.setTelefone(request.getTelefone());
        aluno.setDataNascimento(request.getDataNascimento());

        return toResponse(alunoRepository.save(aluno));
    }

    // -------------------------------------------------------
    // DESATIVAR ALUNO (exclusão lógica)
    // -------------------------------------------------------
    @Transactional
    public void desativar(Long id) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));

        if (Boolean.FALSE.equals(aluno.getAtivo())) {
            throw new RegraNegocioException("O aluno já está inativo.");
        }

        aluno.setAtivo(false);
        alunoRepository.save(aluno);
    }

    // -------------------------------------------------------
    // Conversão Entidade -> Response DTO
    // -------------------------------------------------------
    private AlunoResponse toResponse(Aluno aluno) {
        return AlunoResponse.builder()
                .id(aluno.getId())
                .nome(aluno.getNome())
                .email(aluno.getEmail())
                .cpf(aluno.getCpf())
                .telefone(aluno.getTelefone())
                .dataNascimento(aluno.getDataNascimento())
                .dataMatricula(aluno.getDataMatricula())
                .ativo(aluno.getAtivo())
                .planoId(aluno.getPlano().getId())
                .planoNome(aluno.getPlano().getNome())
                .build();
    }
}
