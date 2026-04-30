package com.facens.academia.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoRequest {

    @NotBlank(message = "O nome do plano é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @NotNull(message = "O valor mensal é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor mensal deve ser maior que zero")
    private BigDecimal valorMensal;

    @NotNull(message = "A duração em meses é obrigatória")
    @Min(value = 1, message = "A duração mínima é de 1 mês")
    @Max(value = 24, message = "A duração máxima é de 24 meses")
    private Integer duracaoMeses;
}
