package com.facens.academia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valorMensal;
    private Integer duracaoMeses;
    private Boolean ativo;
    private Integer totalAlunos;
}
