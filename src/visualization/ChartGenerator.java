package visualization;

import analysis.SortResult;
import java.util.*;
import java.util.stream.Collectors;

public class ChartGenerator {

    public static void generateSerialVsParallelComparison(List<SortResult> results) {
        System.out.println("\n=== GRÁFICO: Serial vs Paralelo ===");

        Map<String, Map<String, Map<Integer, Double>>> algorithmData = new HashMap<>();

        for (SortResult result : results) {
            String algo = result.getAlgorithm();
            String version = result.getVersion();
            int size = result.getDatasetSize();
            double time = result.getExecutionTime();

            algorithmData.putIfAbsent(algo, new HashMap<>());
            algorithmData.get(algo).putIfAbsent(version, new HashMap<>());

            // Média por tamanho e versão
            Map<Integer, Double> versionData = algorithmData.get(algo).get(version);
            versionData.put(size, versionData.getOrDefault(size, 0.0) + time);
        }

        // Calcular médias
        for (String algo : algorithmData.keySet()) {
            System.out.println("\n" + algo.toUpperCase() + " Sort:");
            for (String version : algorithmData.get(algo).keySet()) {
                Map<Integer, Double> versionData = algorithmData.get(algo).get(version);
                System.out.println("  " + version + ":");
                for (int size : versionData.keySet()) {
                    // Contar quantas amostras temos para este tamanho
                    long count = results.stream()
                            .filter(r -> r.getAlgorithm().equals(algo) &&
                                    r.getVersion().equals(version) &&
                                    r.getDatasetSize() == size)
                            .count();
                    double avgTime = versionData.get(size) / count;
                    System.out.printf("    Tamanho %d: %.6f s%n", size, avgTime);
                }
            }
        }
    }

    public static void generateSpeedupAnalysis(List<SortResult> results) {
        System.out.println("\n=== ANÁLISE DE SPEEDUP ===");

        Map<String, Map<Integer, Double>> serialTimes = new HashMap<>();
        Map<String, Map<Integer, Double>> parallelTimes = new HashMap<>();

        // Coletar tempos seriais
        for (SortResult result : results) {
            if (result.getVersion().equals("serial")) {
                String algo = result.getAlgorithm();
                int size = result.getDatasetSize();
                double time = result.getExecutionTime();

                serialTimes.putIfAbsent(algo, new HashMap<>());
                Map<Integer, Double> algoData = serialTimes.get(algo);
                algoData.put(size, algoData.getOrDefault(size, 0.0) + time);
            }
        }

        // Coletar tempos paralelos (média entre 2 e 4 threads)
        for (SortResult result : results) {
            if (result.getVersion().equals("parallel")) {
                String algo = result.getAlgorithm();
                int size = result.getDatasetSize();
                double time = result.getExecutionTime();

                parallelTimes.putIfAbsent(algo, new HashMap<>());
                Map<Integer, List<Double>> algoData = new HashMap<>();
                for (SortResult r : results) {
                    if (r.getVersion().equals("parallel") && r.getAlgorithm().equals(algo)) {
                        algoData.putIfAbsent(r.getDatasetSize(), new ArrayList<>());
                        algoData.get(r.getDatasetSize()).add(r.getExecutionTime());
                    }
                }

                // Calcular média para cada tamanho
                for (int s : algoData.keySet()) {
                    List<Double> times = algoData.get(s);
                    double avg = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    parallelTimes.get(algo).put(s, avg);
                }
            }
        }

        // Calcular speedup
        System.out.println("\nSpeedup (Serial/Paralelo):");
        for (String algo : serialTimes.keySet()) {
            if (parallelTimes.containsKey(algo)) {
                System.out.println("\n" + algo.toUpperCase() + ":");
                for (int size : serialTimes.get(algo).keySet()) {
                    if (parallelTimes.get(algo).containsKey(size)) {
                        double serialTime = serialTimes.get(algo).get(size);
                        // Contar amostras seriais para calcular média correta
                        long serialCount = results.stream()
                                .filter(r -> r.getAlgorithm().equals(algo) &&
                                        r.getVersion().equals("serial") &&
                                        r.getDatasetSize() == size)
                                .count();
                        serialTime /= serialCount;

                        double parallelTime = parallelTimes.get(algo).get(size);
                        double speedup = serialTime / parallelTime;

                        System.out.printf("  Tamanho %d: %.3f (Serial: %.6fs, Paralelo: %.6fs)%n",
                                size, speedup, serialTime, parallelTime);
                    }
                }
            }
        }
    }

    public static void generateThreadScalingAnalysis(List<SortResult> results) {
        System.out.println("\n=== ESCALONAMENTO POR THREADS ===");

        Map<String, Map<Integer, Double>> threadScaling = new HashMap<>();

        for (SortResult result : results) {
            if (result.getVersion().equals("parallel")) {
                String algo = result.getAlgorithm();
                int threads = result.getNumThreads();
                double time = result.getExecutionTime();

                threadScaling.putIfAbsent(algo, new HashMap<>());
                Map<Integer, List<Double>> algoData = new HashMap<>();

                for (SortResult r : results) {
                    if (r.getVersion().equals("parallel") && r.getAlgorithm().equals(algo)) {
                        algoData.putIfAbsent(r.getNumThreads(), new ArrayList<>());
                        algoData.get(r.getNumThreads()).add(r.getExecutionTime());
                    }
                }

                // Calcular média por número de threads
                for (int t : algoData.keySet()) {
                    List<Double> times = algoData.get(t);
                    double avg = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    threadScaling.get(algo).put(t, avg);
                }
            }
        }

        for (String algo : threadScaling.keySet()) {
            System.out.println("\n" + algo.toUpperCase() + ":");
            Map<Integer, Double> algoData = threadScaling.get(algo);
            for (int threads : new int[]{2, 4}) {
                if (algoData.containsKey(threads)) {
                    System.out.printf("  %d threads: %.6f s%n", threads, algoData.get(threads));
                }
            }
        }
    }

    public static void generateDatasetTypeAnalysis(List<SortResult> results) {
        System.out.println("\n=== DESEMPENHO POR TIPO DE DATASET ===");

        Map<String, Map<String, Double>> datasetAnalysis = new HashMap<>();

        for (SortResult result : results) {
            String algo = result.getAlgorithm();
            String datasetType = result.getDatasetType();
            double time = result.getExecutionTime();

            datasetAnalysis.putIfAbsent(algo, new HashMap<>());
            datasetAnalysis.get(algo).put(datasetType,
                    datasetAnalysis.get(algo).getOrDefault(datasetType, 0.0) + time);
        }

        // Calcular médias
        for (String algo : datasetAnalysis.keySet()) {
            System.out.println("\n" + algo.toUpperCase() + ":");
            Map<String, Double> algoData = datasetAnalysis.get(algo);

            for (String datasetType : algoData.keySet()) {
                // Contar amostras para este tipo
                long count = results.stream()
                        .filter(r -> r.getAlgorithm().equals(algo) &&
                                r.getDatasetType().equals(datasetType))
                        .count();
                double avgTime = algoData.get(datasetType) / count;
                System.out.printf("  %s: %.6f s%n", datasetType, avgTime);
            }
        }
    }
}