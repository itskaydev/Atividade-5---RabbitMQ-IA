package app;

/** Mensagem trocada: id único, data/hora de geração, tipo e conteúdo. */
public record Message(String id, String timestamp, String type, String content) {}
