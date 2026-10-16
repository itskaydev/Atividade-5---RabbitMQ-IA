package app;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;

public class Main {

    static final String TAG = "CONSUMIDOR-2 SUPORTE";
    static final String QUEUE = Topology.Q_SUPPORT;
    static final String ERROR_MARKER = "[[FORCAR_ERRO]]";
    static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws Exception {
        long delayMs = Long.parseLong(Topology.env("PROCESSING_DELAY_MS", "1500"));

        System.out.println("[" + TAG + "] Treinando IA...");
        TextClassifier classifier = new TextClassifier(TrainingData.LABELS, TrainingData.SAMPLES);

        Connection conn = Topology.connect(TAG);
        Channel ch = conn.createChannel();
        Topology.declare(ch);
        ch.basicQos(1);   // uma mensagem por vez (sem ACK, não recebe a próxima)

        System.out.println("[" + TAG + "] Consumindo '" + QUEUE + "' | atraso por mensagem: " + delayMs + " ms | ACK manual");

        DeliverCallback onMessage = (consumerTag, delivery) -> {
            long tag = delivery.getEnvelope().getDeliveryTag();
            try {
                Message m = MAPPER.readValue(delivery.getBody(), Message.class);   // JSON inválido => exceção
                if (m.id() == null || m.content() == null || m.content().isBlank()
                        || m.content().contains(ERROR_MARKER)) {
                    throw new IllegalStateException("Mensagem inválida / falha de processamento forçada");
                }

                TextClassifier.Result r = classifier.classify(m.content());
                Thread.sleep(delayMs);   // processamento lento => fila cresce na interface do RabbitMQ

                System.out.printf("[%s] id=%s | gerada em %s | \"%s\" => %s (%.0f%%)%n",
                        TAG, m.id().substring(0, 8), m.timestamp(), m.content(), r.label(), r.confidence() * 100);

                ch.basicAck(tag, false);                    // ACK manual
            } catch (Exception e) {
                System.out.println("[" + TAG + "] ERRO ao processar (" + e.getClass().getSimpleName() + ": "
                        + e.getMessage() + ") => enviando para a DLQ");
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                try {
                    ch.basicNack(tag, false, false);        // requeue=false => dead-letter (DLQ)
                } catch (IOException io) {
                    System.out.println("[" + TAG + "] Falha ao enviar NACK: " + io.getMessage());
                }
            }
        };

        ch.basicConsume(QUEUE, false, onMessage, consumerTag -> {});
    }
}
