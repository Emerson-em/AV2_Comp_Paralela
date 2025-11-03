package analysis;

import java.util.*;
import java.util.concurrent.*;
import sorting.algorithms.*;
import sorting.parallel.*;

public class PerformanceAnalyzer {
    private List<SortResult> results;

    public PerformanceAnalyzer() {
        this.results = Collections.synchronizedList(new ArrayList<>());
    }

    public void runAnalysis(int[] datasetSizes, int numSamples, int maxThreads, String[] datasetTypes) {
        System.out.println("Iniciando análise de desempenho...");
        System.out.println("Tamanhos de dataset: " + Arrays.toString(datasetSizes));
        System.out.println("Tipos de dataset: " + Arrays.toString(datasetTypes));
        System.out.println("Máximo de threads: " + maxThreads);
        System.out.println("Amostras por configuração: " + numSamples);
        System.out.println("=" .repeat(60));

        int totalConfigs = calculateTotalConfigurations(datasetSizes, datasetTypes, numSamples, maxThreads);
        int currentConfig = 0;

        for (int size : datasetSizes) {
            System.out.println("\nAnalisando tamanho: " + size);

            for (String datasetType : datasetTypes) {
                System.out.println("  Tipo: " + datasetType);

                for (int sample = 0; sample < numSamples; sample++) {
                    int[] dataset = DatasetGenerator.generateDataset(size, datasetType);

                    // Testar algoritmos seriais
                    testAlgorithm("bubble", "serial", dataset, datasetType, 1, ++currentConfig, totalConfigs);
                    testAlgorithm("quick", "serial", dataset, datasetType, 1, ++currentConfig, totalConfigs);
                    testAlgorithm("merge", "serial", dataset, datasetType, 1, ++currentConfig, totalConfigs);
                    testAlgorithm("insertion", "serial", dataset, datasetType, 1, ++currentConfig, totalConfigs);

                    // Testar algoritmos paralelos
                    for (int numThreads : new int[]{2, 4}) {
                        if (numThreads <= maxThreads) {
                            testAlgorithm("bubble", "parallel", dataset, datasetType, numThreads, ++currentConfig, totalConfigs);
                            testAlgorithm("quick", "parallel", dataset, datasetType, numThreads, ++currentConfig, totalConfigs);
                            testAlgorithm("merge", "parallel", dataset, datasetType, numThreads, ++currentConfig, totalConfigs);
                            testAlgorithm("insertion", "parallel", dataset, datasetType, numThreads, ++currentConfig, totalConfigs);
                        }
                    }
                }
            }
        }

        System.out.println("\nAnálise concluída!");
    }

    private void testAlgorithm(String algorithm, String version, int[] dataset,
                               String datasetType, int numThreads, int currentConfig, int totalConfigs) {
        System.out.printf("    [%d/%d] %s %s (%d threads)...%n",
                currentConfig, totalConfigs, algorithm, version, numThreads);

        long startTime = System.nanoTime();
        int[] result = null;
        boolean isSorted = false;

        try {
            result = executeSort(algorithm, version, dataset, numThreads);
            isSorted = DatasetGenerator.isSorted(result);
        } catch (Exception e) {
            System.out.println("      Erro: " + e.getMessage());
            isSorted = false;
        }

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000_000.0;

        SortResult sortResult = new SortResult(
                algorithm, version, dataset.length, datasetType,
                numThreads, executionTime, isSorted, System.currentTimeMillis()
        );

        results.add(sortResult);
    }

    private int[] executeSort(String algorithm, String version, int[] dataset, int numThreads) {
        switch (algorithm) {
            case "bubble":
                return version.equals("serial") ?
                        BubbleSort.sort(dataset) :
                        ParallelBubbleSort.sort(dataset, numThreads);

            case "quick":
                return version.equals("serial") ?
                        QuickSort.sort(dataset) :
                        ParallelQuickSort.sort(dataset, numThreads);

            case "merge":
                return version.equals("serial") ?
                        MergeSort.sort(dataset) :
                        ParallelMergeSort.sort(dataset, numThreads);

            case "insertion":
                return version.equals("serial") ?
                        InsertionSort.sort(dataset) :
                        ParallelInsertionSort.sort(dataset, numThreads);

            default:
                throw new IllegalArgumentException("Algoritmo desconhecido: " + algorithm);
        }
    }

    private int calculateTotalConfigurations(int[] datasetSizes, String[] datasetTypes,
                                             int numSamples, int maxThreads) {
        int algorithmsCount = 4;
        int serialConfigs = datasetSizes.length * datasetTypes.length * numSamples * algorithmsCount;
        int parallelThreads = Math.min(2, maxThreads); // Considera apenas 2 e 4 threads
        int parallelConfigs = datasetSizes.length * datasetTypes.length * numSamples * algorithmsCount * parallelThreads;
        return serialConfigs + parallelConfigs;
    }

    public void saveToCSV(String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println(SortResult.getCSVHeader());
            for (SortResult result : results) {
                writer.println(result.toString());
            }
            System.out.println("Resultados salvos em: " + filename);
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    public List<SortResult> getResults() {
        return new ArrayList<>(results);
    }
}