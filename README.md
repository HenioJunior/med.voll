### Notas

### Implementação do SpringSecurity

No Spring Security 5.7.0-M2, descontinuaram o WebSecurityConfigurerAdapter, pois o objetivo é incentivar os usuários a migrarem para uma configuração de segurança baseada em componentes.

fonte: https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter

Abaixo está um checklist para auxilar na implementação... 

### Criação da classe SecurityConfiguration

1. Utilizar as anotações de classe: `@EnableWebSecurity` e `@Configuration`

2. Alterando o processo de autenticação do Spring para STATELESS:
    - O objeto SecurityFilterChain do Spring é usado para configurar o processo de autenticação e de autorização.
    - Nos parênteses do método receberemos a classe HttpSecurity, do Spring, e o chamaremos de http.
```java
   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())//Serve para desabilitarmos proteção contra-ataques do tipo CSRF (Cross-Site Request Forgery)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
```

### Implementar a autenticação


1. Criação da classe `AutenticacaoService` que Implementa a interface `UserDetailsService`; 

    - Criação da interface UsuarioRepository e implementar a assinatura do método `findByLogin`;
   
    - Implementar o método `loadUserByUsername` para buscar as informações do usuário no banco de dados;
        ```java
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByLogin(username);
        }
        ```  

Temos o cliente da nossa API, no caso um aplicativo mobile, e neste aplicativo consta o formulário de login.</br>
Ao efetuar o login clicando no botão "Entrar" do aplicativo, uma requisição é enviada para a nossa API, levando </br>
no corpo da requisição um JSON com o usuário e senha digitado na tela de login.
</br>
A API recebe a requisição e valida no banco de dados. Caso o usuário esteja cadastrado, é gerado o token como resposta.</br>

Precisamos ter um controller para receber essas requisições, responsável por autenticar o usuário no sistema.</br>

2. Implementação do `AutenticacaoController`;

Dentro da classe precisamos construir um método chamado `efetuarLogin` para receber essa requisição.

```java
      public ResponseEntity efetuarLogin() {

        }
```

O método efetuarLogin(), recebe um DTO com os dados que serão enviados pelo aplicativo front-end: DadosAutenticacao dados.</br>

Lembrando que esse parâmetro precisa ser anotado com `@RequestBody`, já que virá no corpo da requisição. E, também, o @Valid para validarmos os campos com o bean validation.</br>
Agora, precisamos consultar o banco de dados e disparar o processo de autenticação.</br>
O processo de autenticação está na classe `AutenticacaoService`. Precisamos chamar o método loadUserByUsername, já que é ele que usa o repository para efetuar o select no banco de dados.</br>
No controller, precisamos usar a classe `AuthenticationManager` do Spring, responsável por disparar o processo de autenticação.</br>
Vamos declarar o atributo na classe `AutenticacaoController` e chamaremos de manager. Acima, incluiremos a anotação `@Autowired`, para solicitar ao Spring a injeção desse parâmetro. Não somos nós que vamos instanciar esse objeto, e sim o Spring.</br>

```java
@Autowired
private AuthenticationManager manager;
```

### dentro de efetuarLogin()
Criação da variavel `token` que é do tipo `UsernamePasswordAuthenticationToken()` que recebe como parâmetro o login e a senha da DTO DadosAutenticacao. </br>

O objeto `manager` chama o método .authenticate(), que recebe como parâmetro o `token`.</br>

No fim, precisamos retornar um `.ok().build()``. Isso para recebermos um código 200 OK quando a requisição for efetuada com sucesso.

```java
@PostMapping
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.login(), dados.Senha());
        var authentication = manager.authenticate(token);
        return ResponseEntity.ok().build();
    }
```

### Em SecurityConfigurations

A classe `AuthenticationManager`` é do Spring. Porém, ele não injetado de forma automática.</br>
Por ser uma configuração de segurança, faremos essa alteração na classe `SecurityConfigurations`.

Criação do método público, cujo retorno é o objeto `AuthenticationManager` e o nome será authenticationManager.</br>

No parêntese deste método, receberemos um objeto do tipo `AuthenticationConfiguration` chamado configuration.</br>

No retorno, teremos `configuration.getAuthenticationManager()`.

```java
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
```

Criaremos mais um método usando a anotação @Bean. Será público e devolve um objeto do tipo PasswordEncoder, sendo a classe que representa o algoritmo de hashing da senha.

```java
 @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
```

Precisamos implementar uma interface chamada `UserDetails` para o Spring Security na classe Usuario.</br>
Por ser uma interface, precisamos implementar os métodos.</br>
No primeiro método criado, precisamos devolver um objeto do tipo Collection chamado getAuthorities. Caso tenhamos um controle de permissão no projeto, por exemplo, perfis de acesso, é necessário criar uma classe que represente esses perfis.</br>
```java
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
```

### Gerando token JWT

Criação de uma nova classe(TokenService) , para que possamos isolar o token, uma boa prática em programação.</br>

Ela fará a geração, a validação e o que mais estiver relacionado aos tokens. No arquivo "TokenService.java", passaremos a anotação @Service, já que a classe representará um serviço.</br>

Criação do método `gerarToken`. Dentro dela, usaremos a biblioteca JWT.</br>

Vamos gerar a validade chamando o método .withExpiresAt(). passando como parâmetro dataExpiracao().

```java
@Service
public class TokenService {

    public String gerarToken(Usuario usuario) { 
        try {
            var algoritmo = Algorithm.HMAC256("12345678");
            return JWT.create()
                .withIssuer("API Voll.med")
                .withSubject(usuario.getLogin())
                .withExpiresAt(dataExpiracao())
                .sign(algoritmo);
        } catch (JWTCreationException exception){
            throw new RuntimeException("erro ao gerrar token jwt", exception);
        }        
    }

    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
```

#### Em AutenticacaoController

Injetaremos o `TokenService tokenService` para gerar o token;</br>
O token será retornado no corpo da mensagem `Response.Entity.ok(tokenService.gerarToken((usuario) authentication.getPrincipal()))`;

```java
@Autowired
private TokenService tokenService;

@PostMapping
public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
    var token = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
    var authentication :Authentication = manager.authenticate(token);

    return ResponseEntity.ok(tokenService.gerarToken((Usuario) authentication.getPrincipal()));
}
```

Além do Issuer, Subject e data de expiração, podemos incluir outras informações no token JWT, de acordo com as necessidades da aplicação. Por exemplo, podemos incluir o id do usuário no token, para isso basta utilizar o método withClaim:</br>

```java
return JWT.create()
    .withIssuer("API Voll.med")
    .withSubject(usuario.getLogin())

    .withClaim("id", usuario.getId())

    .withExpiresAt(dataExpiracao())
    .sign(algoritmo);
```

O método withClaim recebe dois parâmetros, sendo o primeiro uma String que identifica o nome do claim (propriedade armazenada no token), e o segundo a informação que se deseja armazenar.</br>

