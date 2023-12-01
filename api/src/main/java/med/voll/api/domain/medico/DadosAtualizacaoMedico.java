package med.voll.api.domain.medico;

import jakarta.validation.constraints.NotBlank;
import med.voll.api.domain.endereco.DadosEndereco;

public record DadosAtualizacaoMedico(

        Long id,
        @NotBlank
        String nome,
        String telefone,
        DadosEndereco endereco) {
}
