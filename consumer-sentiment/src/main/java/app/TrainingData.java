package app;

/** Dataset de treino (pt-BR) embutido no container — IA 1: análise de sentimento. */
public final class TrainingData {
    private TrainingData() {}

    public static final String[] LABELS = {"POSITIVA", "NEGATIVA", "NEUTRA"};

    public static final String[][] SAMPLES = {
        { // POSITIVA
            "Adorei o produto, excelente qualidade!",
            "Superou minhas expectativas, recomendo muito",
            "Chegou rápido e funciona perfeitamente, muito satisfeito",
            "Melhor compra que fiz, vale cada centavo",
            "Qualidade incrível e entrega rápida, nota dez",
            "Muito bom, bonito e resistente, recomendo a todos",
            "Atendimento maravilhoso e produto perfeito, super feliz",
            "Custo-benefício excelente, fiquei encantado",
            "Ótimo produto, amei, compraria de novo",
            "Fantástico, funciona muito bem e é lindo",
            "Gostei bastante, produto de primeira, muito bom",
            "Perfeito, chegou antes do prazo e estou muito contente",
            "Excelente, recomendo, material ótimo e acabamento impecável",
            "Satisfeito com a compra, produto maravilhoso"
        },
        { // NEGATIVA
            "Péssimo, chegou quebrado e não funciona",
            "Muito ruim, qualidade horrível, me arrependi da compra",
            "Não recomendo, produto fraco e de baixa qualidade",
            "Decepcionante, parou de funcionar em dois dias",
            "Pior compra da vida, atrasou e veio com defeito",
            "Atendimento terrível, não gostei nada, quero reembolso",
            "Material frágil, veio riscado e com defeito, não gostei",
            "Propaganda enganosa, produto ruim e caro demais",
            "Horrível, veio com defeito e a loja não resolveu",
            "Odiei, qualidade péssima, dinheiro jogado fora",
            "Não funciona, produto ruim, estou muito insatisfeito",
            "Entrega atrasada, caixa amassada e produto quebrado, decepção",
            "Lixo, parou de funcionar, não compro mais, péssimo",
            "Muito caro para a qualidade ruim, arrependido"
        },
        { // NEUTRA
            "Produto razoável, cumpre o que promete, nada de especial",
            "É ok, nem bom nem ruim, atende o básico",
            "Chegou no prazo e é igual à descrição, sem surpresas",
            "Mediano, dá para o uso do dia a dia",
            "Normal, esperava um pouco mais mas serve",
            "Produto comum, preço justo, sem grandes diferenças",
            "Entrega dentro do prazo, produto como anunciado",
            "Funciona, sem destaque, nem gostei nem desgostei",
            "Produto regular, cumpre a função, nada demais",
            "Tudo certo com a entrega, produto de acordo com a descrição",
            "Razoável pelo preço, nem muito bom nem muito ruim",
            "Mais ou menos, serve para o básico, sem novidades",
            "Produto normal, igual aos outros, preço médio",
            "Recebi o produto no prazo, está conforme o anúncio, comum"
        }
    };
}
