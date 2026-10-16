package app;

/** Dataset de treino (pt-BR) embutido no container — IA 2: classificação de solicitações de suporte. */
public final class TrainingData {
    private TrainingData() {}

    public static final String[] LABELS = {
        "PROBLEMA DE ACESSO", "PROBLEMA DE DESEMPENHO", "PROBLEMA DE CONEXÃO"
    };

    public static final String[][] SAMPLES = {
        { // ACESSO
            "Não consigo fazer login, aparece senha incorreta",
            "Minha conta foi bloqueada após várias tentativas de acesso",
            "Esqueci a senha e o e-mail de recuperação não chega",
            "O código de autenticação em dois fatores não funciona",
            "Sem permissão para acessar o painel, usuário não autorizado",
            "Token de acesso expirado, não consigo entrar no sistema",
            "Erro de credenciais inválidas ao tentar autenticar",
            "Usuário ou senha inválidos, não consigo acessar minha conta",
            "Acesso negado ao módulo, preciso de permissão de administrador",
            "Conta suspensa, não consigo logar com meu usuário",
            "O link de redefinição de senha expirou"
        },
        { // DESEMPENHO
            "O sistema está extremamente lento, as telas demoram para carregar",
            "Aplicação travando e consumindo muita memória e CPU",
            "Os relatórios levam minutos para gerar, muita lentidão",
            "O app congela quando abro arquivos grandes",
            "Consultas demoradas, o desempenho piorou depois da atualização",
            "Alto uso de CPU deixa tudo travado e lento",
            "Páginas carregam devagar e o sistema engasga o tempo todo",
            "Lentidão absurda, o programa demora muito para responder",
            "O sistema trava e fica pesado quando há muitos usuários",
            "Consumo excessivo de memória, a aplicação fica travando",
            "Tempo de resposta muito alto, tudo demora para abrir"
        },
        { // CONEXÃO
            "Sem conexão com a internet, o aplicativo não consegue sincronizar",
            "O servidor está fora do ar, erro de timeout na rede",
            "O wifi cai toda hora e a VPN desconecta",
            "Falha de conexão com o banco de dados, host inacessível",
            "Erro de DNS, não consigo conectar ao servidor",
            "A rede oscila e a conexão é interrompida constantemente",
            "Não consegue conectar, a conexão foi recusada pelo servidor",
            "Perda de pacotes e queda de conexão com a rede da empresa",
            "Não há conexão com o servidor, a internet caiu",
            "Conexão instável, a VPN não conecta e o roteador reinicia",
            "Erro de rede ao tentar conectar na API, servidor não responde"
        }
    };
}
