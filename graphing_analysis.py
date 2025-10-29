import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
from typing import List, Dict, Any
import os

class PerformanceVisualizer:
    def __init__(self, results_file: str = "demo_analysis.csv"):
        self.results_file = results_file
        self.df = None
        self.load_data()
    
    def load_data(self):
        """Carrega os dados do arquivo CSV"""
        if os.path.exists(self.results_file):
            self.df = pd.read_csv(self.results_file)
            print(f"Dados carregados: {len(self.df)} registros")
        else:
            print(f"Arquivo {self.results_file} não encontrado")
            # Cria dados de exemplo para teste
            self.create_sample_data()
    
    def create_sample_data(self):
        """Cria dados de exemplo se o arquivo não existir"""
        print("Criando dados de exemplo para demonstração...")
        sample_data = []
        algorithms = ['bubble', 'quick', 'merge', 'insertion']
        sizes = [100, 500, 1000]
        
        for algo in algorithms:
            for size in sizes:
                # Dados de exemplo
                sample_data.append({
                    'algorithm': algo,
                    'version': 'serial',
                    'dataset_size': size,
                    'dataset_type': 'random',
                    'num_threads': 1,
                    'execution_time': size * 0.001 * (1 if algo == 'quick' else 2 if algo == 'merge' else 3 if algo == 'insertion' else 4)
                })
                sample_data.append({
                    'algorithm': algo,
                    'version': 'parallel',
                    'dataset_size': size,
                    'dataset_type': 'random',
                    'num_threads': 4,
                    'execution_time': size * 0.001 * (0.6 if algo == 'quick' else 1.2 if algo == 'merge' else 1.8 if algo == 'insertion' else 2.4)
                })
        
        self.df = pd.DataFrame(sample_data)
        self.df.to_csv(self.results_file, index=False)
        print("Dados de exemplo criados e salvos!")
    
    def create_comparison_plots(self):
        """Cria gráficos comparativos entre versões serial e paralela"""
        if self.df is None or self.df.empty:
            print("Nenhum dado para visualizar")
            return
        
        # Configurações de plot
        plt.style.use('default')
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        axes = axes.flatten()
        
        algorithms = self.df['algorithm'].unique()
        
        for i, algo in enumerate(algorithms):
            if i >= len(axes):
                break
                
            ax = axes[i]
            algo_data = self.df[self.df['algorithm'] == algo]
            
            # Média por tamanho de dataset e versão
            summary = algo_data.groupby(['dataset_size', 'version'])['execution_time'].mean().unstack()
            
            if 'serial' in summary.columns:
                serial_times = summary['serial']
                ax.plot(serial_times.index, serial_times.values, marker='o', linewidth=2, label='Serial', color='red')
            
            if 'parallel' in summary.columns:
                parallel_times = summary['parallel']
                ax.plot(parallel_times.index, parallel_times.values, marker='s', linewidth=2, label='Paralelo', color='blue')
            
            ax.set_title(f'{algo.upper()} Sort - Serial vs Paralelo', fontsize=14, fontweight='bold')
            ax.set_xlabel('Tamanho do Dataset', fontsize=12)
            ax.set_ylabel('Tempo de Execução (s)', fontsize=12)
            ax.legend()
            ax.grid(True, alpha=0.3)
            ax.tick_params(axis='both', which='major', labelsize=10)
        
        plt.tight_layout()
        plt.savefig('serial_vs_parallel_comparison.png', dpi=300, bbox_inches='tight')
        plt.show()  # 🔥 IMPORTANTE: Mostra o gráfico
        print("✅ Gráfico de comparação salvo e exibido!")
    
    def create_thread_scaling_plot(self):
        """Cria gráfico de escalonamento por número de threads"""
        if self.df is None or self.df.empty:
            return
        
        parallel_data = self.df[self.df['version'] == 'parallel']
        
        if parallel_data.empty:
            print("Nenhum dado paralelo para analisar")
            return
        
        plt.figure(figsize=(12, 8))
        
        colors = ['blue', 'red', 'green', 'orange']
        algorithms = parallel_data['algorithm'].unique()
        
        for i, algo in enumerate(algorithms):
            algo_data = parallel_data[parallel_data['algorithm'] == algo]
            
            # Agrupa por número de threads
            thread_scaling = algo_data.groupby('num_threads')['execution_time'].mean()
            
            if not thread_scaling.empty:
                plt.plot(thread_scaling.index, thread_scaling.values, 
                        marker='o', linewidth=2, label=algo, color=colors[i % len(colors)])
        
        plt.xlabel('Número de Threads', fontsize=12)
        plt.ylabel('Tempo de Execução (s)', fontsize=12)
        plt.title('Escalonamento por Número de Threads', fontsize=14, fontweight='bold')
        plt.legend()
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        plt.savefig('thread_scaling_analysis.png', dpi=300, bbox_inches='tight')
        plt.show()  # 🔥 IMPORTANTE: Mostra o gráfico
        print("✅ Gráfico de escalonamento salvo e exibido!")
    
    def create_dataset_type_analysis(self):
        """Analisa desempenho por tipo de dataset"""
        if self.df is None or self.df.empty:
            return
        
        plt.figure(figsize=(14, 10))
        
        # Média por algoritmo e tipo de dataset
        dataset_analysis = self.df.groupby(['algorithm', 'dataset_type'])['execution_time'].mean().unstack()
        
        if not dataset_analysis.empty:
            dataset_analysis.plot(kind='bar', figsize=(14, 8))
            plt.title('Desempenho por Tipo de Dataset', fontsize=14, fontweight='bold')
            plt.xlabel('Algoritmo', fontsize=12)
            plt.ylabel('Tempo Médio de Execução (s)', fontsize=12)
            plt.xticks(rotation=45)
            plt.legend(title='Tipo de Dataset', bbox_to_anchor=(1.05, 1), loc='upper left')
            plt.grid(True, alpha=0.3)
            plt.tight_layout()
            plt.savefig('dataset_type_analysis.png', dpi=300, bbox_inches='tight')
            plt.show()  # 🔥 IMPORTANTE: Mostra o gráfico
            print("✅ Gráfico de tipos de dataset salvo e exibido!")
    
    def create_speedup_analysis(self):
        """Analisa o speedup das versões paralelas"""
        if self.df is None or self.df.empty:
            return
        
        # Calcula speedup (tempo_serial / tempo_paralelo)
        serial_times = self.df[self.df['version'] == 'serial'].groupby(
            ['algorithm', 'dataset_size'])['execution_time'].mean()
        
        parallel_times = self.df[self.df['version'] == 'parallel'].groupby(
            ['algorithm', 'dataset_size'])['execution_time'].mean()
        
        speedup_data = []
        for (algo, size), para_time in parallel_times.items():
            if (algo, size) in serial_times.index:
                serial_time = serial_times[(algo, size)]
                speedup = serial_time / para_time
                speedup_data.append({
                    'algorithm': algo,
                    'dataset_size': size,
                    'speedup': speedup
                })
        
        if not speedup_data:
            print("Não há dados suficientes para calcular speedup")
            return None
        
        speedup_df = pd.DataFrame(speedup_data)
        
        # Gráfico de speedup
        plt.figure(figsize=(12, 8))
        
        colors = ['blue', 'red', 'green', 'orange']
        algorithms = speedup_df['algorithm'].unique()
        
        for i, algo in enumerate(algorithms):
            algo_data = speedup_df[speedup_df['algorithm'] == algo]
            plt.plot(algo_data['dataset_size'], algo_data['speedup'], 
                    marker='o', linewidth=2, label=algo, color=colors[i % len(colors)])
        
        plt.axhline(y=1, color='red', linestyle='--', alpha=0.7, label='Limite Serial')
        plt.xlabel('Tamanho do Dataset', fontsize=12)
        plt.ylabel('Speedup (Serial/Paralelo)', fontsize=12)
        plt.title('Análise de Speedup - Versões Paralelas', fontsize=14, fontweight='bold')
        plt.legend()
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        plt.savefig('speedup_analysis.png', dpi=300, bbox_inches='tight')
        plt.show()  # 🔥 IMPORTANTE: Mostra o gráfico
        print("✅ Gráfico de speedup salvo e exibido!")
        
        return speedup_df

def generate_all_analysis():
    """Gera todas as análises gráficas"""
    visualizer = PerformanceVisualizer()
    
    print("Gerando gráficos de análise...")
    
    # Gráfico 1: Comparação Serial vs Paralelo
    print("\n1. Gerando gráfico de comparação serial vs paralelo...")
    visualizer.create_comparison_plots()
    
    # Gráfico 2: Escalonamento por threads
    print("\n2. Gerando gráfico de escalonamento por threads...")
    visualizer.create_thread_scaling_plot()
    
    # Gráfico 3: Análise por tipo de dataset
    print("\n3. Gerando gráfico de análise por tipo de dataset...")
    visualizer.create_dataset_type_analysis()
    
    # Gráfico 4: Análise de speedup
    print("\n4. Gerando gráfico de análise de speedup...")
    speedup_df = visualizer.create_speedup_analysis()
    
    # Salva dados de speedup
    if speedup_df is not None:
        speedup_df.to_csv('speedup_analysis.csv', index=False)
        print("✅ Dados de speedup salvos em CSV")
    
    print("\n🎉 Todos os gráficos foram gerados!")
    print("📊 Gráficos salvos como arquivos PNG na pasta atual")

# Função para teste rápido
def quick_test():
    """Teste rápido para verificar se os gráficos funcionam"""
    print("=== TESTE RÁPIDO DE GRÁFICOS ===")
    
    # Cria visualizador com dados de exemplo
    visualizer = PerformanceVisualizer("test_data.csv")
    
    # Gera apenas um gráfico para teste
    print("Gerando gráfico de comparação...")
    visualizer.create_comparison_plots()
    
    print("Se você viu um gráfico, está funcionando!")

if __name__ == "__main__":
    # Para teste rápido, descomente a linha abaixo:
    # quick_test()
    
    # Para análise completa:
    generate_all_analysis()