package com.facens.academia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlunoResponse {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
    private LocalDateTime dataMatricula;
    private Boolean ativo;

    // Dados do plano (sem expor a entidade diretamente)
    private Long planoId;
    private String planoNome;
}
