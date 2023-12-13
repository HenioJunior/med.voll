package med.voll.api.domain.consulta;

import java.time.LocalDateTime;

public record DadosAgendamentoConsulta(Long idMedico, Long idPaciente, LocalDateTime data){

}
