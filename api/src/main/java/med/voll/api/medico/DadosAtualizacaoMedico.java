package med.voll.api.medico;

import jakarta.validation.constraints.NotBlank;
import med.voll.api.endereco.DadosEndereco;

public record DadosAtualizacaoMedico(

        Long id,
        @NotBlank
        String nome,
        String telefone,
        DadosEndereco endereco) {
}
