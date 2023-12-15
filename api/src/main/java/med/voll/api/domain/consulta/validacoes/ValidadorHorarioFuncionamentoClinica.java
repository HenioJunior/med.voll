package med.voll.api.domain.consulta.validacoes;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;

import java.time.DayOfWeek;

public class ValidadorHorarioFuncionamentoClinica {

    public void validar(DadosAgendamentoConsulta dados){
        var dataConsulta = dados.data();
        var domingo = dataConsulta.getDayOfWeek().equals(DayOfWeek.SUNDAY);
        var antesdaAberturaDaClinica = dataConsulta.getHour() < 7;
        var depoisEnceramentoClinica = dataConsulta.getHour() > 18;

        if (domingo || antesdaAberturaDaClinica || depoisEnceramentoClinica) {
            throw new ValidacaoException("Consulta fora do funcionamento da clínica");
        }
    }
}
