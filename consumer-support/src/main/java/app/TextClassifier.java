package app;

import smile.classification.LogisticRegression;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IA embarcada: classificador de texto supervisionado.
 *  - Pré-processamento: minúsculas, remoção de acentos/pontuação, tokenização e bigramas de negação ("nao_gostei").
 *  - Representação: Bag-of-Words (frequência de termos) normalizada (L2).
 *  - Modelo: Regressão Logística multinomial da biblioteca Smile (smile.classification.LogisticRegression),
 *    treinada na inicialização do container com o dataset embutido em TrainingData.
 */
public class TextClassifier {

    public record Result(String label, double confidence) {}

    private final String[] labels;
    private final Map<String, Integer> vocab = new HashMap<>();
    private final LogisticRegression model;

    public TextClassifier(String[] labels, String[][] samplesPerClass) {
        this.labels = labels;
        List<List<String>> docs = new ArrayList<>();
        List<Integer> ys = new ArrayList<>();
        for (int c = 0; c < samplesPerClass.length; c++) {
            for (String s : samplesPerClass[c]) {
                List<String> feats = features(s);
                docs.add(feats);
                ys.add(c);
                for (String f : feats) vocab.putIfAbsent(f, vocab.size());
            }
        }
        double[][] x = new double[docs.size()][];
        int[] y = new int[docs.size()];
        for (int i = 0; i < docs.size(); i++) {
            x[i] = vectorize(docs.get(i));
            y[i] = ys.get(i);
        }
        // lambda=0.1 (regularização), tol=1e-5, maxIter=500
        this.model = LogisticRegression.fit(x, y, 0.1, 1e-5, 500);
        System.out.println("[IA] Smile LogisticRegression treinada: " + docs.size()
                + " exemplos, vocabulário=" + vocab.size() + ", classes=" + labels.length);
    }

    static String normalize(String s) {
        String n = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replaceAll("[^a-z0-9 ]", " ");
    }

    static List<String> features(String text) {
        String[] t = normalize(text).trim().split("\\s+");
        List<String> out = new ArrayList<>();
        for (int i = 0; i < t.length; i++) {
            if (t[i].length() < 2) continue;
            out.add(t[i]);
            if (t[i].equals("nao") && i + 1 < t.length) out.add("nao_" + t[i + 1]);
        }
        return out;
    }

    private double[] vectorize(List<String> feats) {
        double[] v = new double[vocab.size()];
        for (String f : feats) {
            Integer idx = vocab.get(f);
            if (idx != null) v[idx] += 1.0;
        }
        double norm = 0;
        for (double d : v) norm += d * d;
        norm = Math.sqrt(norm);
        if (norm > 0) for (int i = 0; i < v.length; i++) v[i] /= norm;
        return v;
    }

    public Result classify(String text) {
        double[] x = vectorize(features(text));
        double[] posteriori = new double[labels.length];
        int k = model.predict(x, posteriori);
        return new Result(labels[k], posteriori[k]);
    }
}
