package app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    static final String POISON_MARKER = "[[FORCAR_ERRO]]";

    static final List<String> PRODUTOS = List.of("notebook", "fone de ouvido", "smartphone", "cafeteira",
            "teclado mecânico", "monitor", "cadeira gamer", "mochila", "caixa de som", "mouse sem fio");

    static final List<String> POSITIVAS = List.of(
            "Adorei, produto excelente e de ótima qualidade!",
            "Superou minhas expectativas, recomendo muito.",
            "Chegou rápido e funciona perfeitamente, muito satisfeito.",
            "Melhor compra que fiz este ano, vale cada centavo.",
            "Qualidade incrível, entrega rápida, nota dez!",
            "Muito bom, bonito e resistente. Recomendo a todos.",
            "Atendimento maravilhoso e produto perfeito, super feliz.",
            "Custo-benefício excelente, fiquei encantado.");

    static final List<String> NEGATIVAS = List.of(
            "Péssimo, chegou quebrado e não funciona.",
            "Muito ruim, qualidade horrível, me arrependi da compra.",
            "Não recomendo, produto fraco e de baixa qualidade.",
            "Decepcionante, parou de funcionar em dois dias.",
            "Pior compra da vida, atrasou e veio com defeito.",
            "Atendimento terrível, não gostei nada, quero reembolso.",
            "Material frágil, veio riscado e com defeito. Não gostei.",
            "Propaganda enganosa, produto ruim e caro demais.");

    static final List<String> NEUTRAS = List.of(
            "Produto razoável, cumpre o que promete, nada de especial.",
            "É ok, nem bom nem ruim, atende o básico.",
            "Chegou no prazo e é igual à descrição, sem surpresas.",
            "Mediano, dá para o uso do dia a dia.",
            "Normal, esperava um pouco mais mas serve.",
            "Produto comum, preço justo, sem grandes diferenças.",
            "Entrega dentro do prazo, produto como anunciado.",
            "Funciona, sem destaque, nem gostei nem desgostei.");

    static final List<String> SUPORTE_ACESSO = List.of(
            "Não consigo fazer login, aparece senha incorreta.",
            "Minha conta foi bloqueada após várias tentativas de acesso.",
            "Esqueci a senha e o e-mail de recuperação não chega.",
            "O código de autenticação em dois fatores não funciona.",
            "Sem permissão para acessar o painel, usuário não autorizado.",
            "Token de acesso expirado, não consigo entrar no sistema.",
            "Erro de credenciais inválidas ao tentar autenticar.");

    static final List<String> SUPORTE_DESEMPENHO = List.of(
            "O sistema está extremamente lento, as telas demoram para carregar.",
            "Aplicação travando e consumindo muita memória e CPU.",
            "Os relatórios levam minutos para gerar, muita lentidão.",
            "O app congela quando abro arquivos grandes.",
            "Consultas demoradas, o desempenho piorou depois da atualização.",
            "Alto uso de CPU deixa tudo travado e lento.",
            "Páginas carregam devagar e o sistema engasga o tempo todo.");

    static final List<String> SUPORTE_CONEXAO = List.of(
            "Sem conexão com a internet, o aplicativo não consegue sincronizar.",
            "O servidor está fora do ar, erro de timeout na rede.",
            "O wifi cai toda hora e a VPN desconecta.",
            "Falha de conexão com o banco de dados, host inacessível.",
            "Erro de DNS, não consigo conectar ao servidor.",
            "A rede oscila e a conexão é interrompida constantemente.",
            "Não consegue conectar, a conexão foi recusada pelo servidor.");

    static <T> T pick(List<T> l) {
        return l.get(ThreadLocalRandom.current().nextInt(l.size()));
    }

    static String reviewText() {
        int r = ThreadLocalRandom.current().nextInt(3);
        List<String> pool = r == 0 ? POSITIVAS : (r == 1 ? NEGATIVAS : NEUTRAS);
        return "Comprei " + pick(PRODUTOS) + ". " + pick(pool);
    }

    static String supportText() {
        int r = ThreadLocalRandom.current().nextInt(3);
        List<String> pool = r == 0 ? SUPORTE_ACESSO : (r == 1 ? SUPORTE_DESEMPENHO : SUPORTE_CONEXAO);
        return "Cliente relata: " + pick(pool);
    }

    public static void main(String[] args) throws Exception {
        int perSecond = Math.max(1, Integer.parseInt(Topology.env("MESSAGES_PER_SECOND", "6")));
        int poisonEvery = Integer.parseInt(Topology.env("POISON_EVERY", "100"));
        ObjectMapper mapper = new ObjectMapper();

        Connection conn = Topology.connect("generator");
        Channel ch = conn.createChannel();
        Topology.declare(ch);   // garante filas antes de publicar (nenhuma mensagem se perde)

        System.out.println("[GERADOR] Gerando " + perSecond + " msg/s | envenenada a cada " + poisonEvery + " msgs (0=desligado)");

        long intervalNanos = 1_000_000_000L / perSecond;
        long next = System.nanoTime();
        long count = 0;

        while (true) {
            count++;
            boolean review = ThreadLocalRandom.current().nextBoolean();
            String type = review ? "review" : "support";
            String routingKey = review ? Topology.RK_REVIEW : Topology.RK_SUPPORT;
            String content = review ? reviewText() : supportText();
            boolean poison = poisonEvery > 0 && count % poisonEvery == 0;
            if (poison) content = POISON_MARKER + " " + content;

            Message m = new Message(UUID.randomUUID().toString(), Instant.now().toString(), type, content);
            AMQP.BasicProperties props = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
                    .contentType("application/json").type(type).messageId(m.id()).build();

            ch.basicPublish(Topology.EXCHANGE, routingKey, props, mapper.writeValueAsBytes(m));
            System.out.printf("[GERADOR] #%d -> %s | %s | %s%n", count, routingKey, m.id().substring(0, 8),
                    poison ? "(MENSAGEM COM ERRO) " + content : content);

            next += intervalNanos;
            long sleep = next - System.nanoTime();
            if (sleep > 0) Thread.sleep(sleep / 1_000_000L, (int) (sleep % 1_000_000L));
        }
    }
}
