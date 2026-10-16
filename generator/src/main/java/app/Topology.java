package app;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Topologia do RabbitMQ (declarada de forma idempotente por todos os serviços,
 * então a ordem de subida dos containers não importa).
 *
 *  messages.topic (TOPIC) --review.product---> q.reviews --(nack/erro)--> messages.dlx --> dlq.reviews
 *                         --support.request--> q.support --(nack/erro)--> messages.dlx --> dlq.support
 */
public final class Topology {
    public static final String EXCHANGE = "messages.topic";
    public static final String DLX = "messages.dlx";

    public static final String RK_REVIEW = "review.product";
    public static final String RK_SUPPORT = "support.request";

    public static final String Q_REVIEWS = "q.reviews";
    public static final String Q_SUPPORT = "q.support";
    public static final String DLQ_REVIEWS = "dlq.reviews";
    public static final String DLQ_SUPPORT = "dlq.support";

    private Topology() {}

    public static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    public static Connection connect(String name) throws Exception {
        ConnectionFactory f = new ConnectionFactory();
        f.setHost(env("RABBITMQ_HOST", "localhost"));
        f.setPort(Integer.parseInt(env("RABBITMQ_PORT", "5672")));
        f.setUsername(env("RABBITMQ_USER", "admin"));
        f.setPassword(env("RABBITMQ_PASS", "admin123"));
        f.setAutomaticRecoveryEnabled(true);
        while (true) {
            try {
                Connection c = f.newConnection(name);
                System.out.println("[" + name + "] Conectado ao RabbitMQ em " + f.getHost());
                return c;
            } catch (Exception e) {
                System.out.println("[" + name + "] RabbitMQ indisponível (" + e.getMessage() + "). Tentando de novo em 3s...");
                Thread.sleep(3000);
            }
        }
    }

    public static void declare(Channel ch) throws IOException {
        ch.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC, true);
        ch.exchangeDeclare(DLX, BuiltinExchangeType.DIRECT, true);

        // Dead Letter Queues
        ch.queueDeclare(DLQ_REVIEWS, true, false, false, null);
        ch.queueBind(DLQ_REVIEWS, DLX, "dlq.reviews");
        ch.queueDeclare(DLQ_SUPPORT, true, false, false, null);
        ch.queueBind(DLQ_SUPPORT, DLX, "dlq.support");

        // Filas dos consumidores (com DLX configurada)
        ch.queueDeclare(Q_REVIEWS, true, false, false,
                Map.<String, Object>of("x-dead-letter-exchange", DLX,
                                       "x-dead-letter-routing-key", "dlq.reviews"));
        ch.queueBind(Q_REVIEWS, EXCHANGE, RK_REVIEW);

        ch.queueDeclare(Q_SUPPORT, true, false, false,
                Map.<String, Object>of("x-dead-letter-exchange", DLX,
                                       "x-dead-letter-routing-key", "dlq.support"));
        ch.queueBind(Q_SUPPORT, EXCHANGE, RK_SUPPORT);
    }
}
