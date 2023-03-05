package med.voll.api.paciente;

import jakarta.persistence.*;
import med.voll.api.endereco.Endereco;

import java.util.Objects;

@Table(name = "pacientes")
@Entity(name = "Paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;

    @Embedded
    private Endereco endereco;

    public Paciente(){}

    public Paciente(String nome, String email, String telefone, String cpf, Endereco endereco) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.endereco = endereco;
    }

    public Paciente(DadosCadastroPaciente dados) {
        this.nome = dados.nome();
        this.email = dados.email();
        this.telefone = dados.telefone();
        this.cpf = dados.cpf();
        this.endereco = new Endereco(dados.endereco());
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paciente paciente)) return false;
        return Objects.equals(getNome(), paciente.getNome()) && Objects.equals(getEmail(), paciente.getEmail()) && Objects.equals(getTelefone(), paciente.getTelefone()) && Objects.equals(getCpf(), paciente.getCpf()) && Objects.equals(getEndereco(), paciente.getEndereco());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNome(), getEmail(), getTelefone(), getCpf(), getEndereco());
    }
}
