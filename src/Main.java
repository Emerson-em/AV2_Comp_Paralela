import analysis.PerformanceAnalyzer;
import analysis.SortResult;
import visualization.ChartGenerator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== AV2 - COMPUTAÇÃO PARALELA ===");
        System.out.println("Análise de Desempenho: Algoritmos de Ordenação Serial vs Paralelo\n");

        try {
            PerformanceAnalyzer analyzer = new PerformanceAnalyzer();

            // Configurações para análise de demonstração
            int[] datasetSizes = {100, 500, 1000};
            String[] datasetTypes = {"random", "sorted"};
            int numSamples = 3;
            int maxThreads = 4;

            // Executar análise
            analyzer.runAnalysis(datasetSizes, numSamples, maxThreads, datasetTypes);

            // Salvar resultados
            analyzer.saveToCSV("demo_analysis.csv");

            // Gerar análises gráficas
            List<SortResult> results = analyzer.getResults();

            ChartGenerator.generateSerialVsParallelComparison(results);
            ChartGenerator.generateSpeedupAnalysis(results);
            ChartGenerator.generateThreadScalingAnalysis(results);
            ChartGenerator.generateDatasetTypeAnalysis(results);

            System.out.println("\n🎉 ANÁLISE CONCLUÍDA COM SUCESSO!");
            System.out.println("📊 Resultados salvos em: demo_analysis.csv");
            System.out.println("📈 Análises geradas no console");

        } catch (Exception e) {
            System.out.println("❌ Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}