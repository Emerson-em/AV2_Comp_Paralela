import time
import random
import concurrent.futures
import multiprocessing
import pandas as pd
import numpy as np
from typing import List, Callable, Dict, Any
import os

# =============================================================================
# 1. ALGORITMOS SERIAIS
# =============================================================================

def bubble_sort(arr: List[int]) -> List[int]:
    """Implementação do Bubble Sort"""
    n = len(arr)
    arr_copy = arr.copy()
    for i in range(n):
        for j in range(0, n - i - 1):
            if arr_copy[j] > arr_copy[j + 1]:
                arr_copy[j], arr_copy[j + 1] = arr_copy[j + 1], arr_copy[j]
    return arr_copy

def quick_sort(arr: List[int]) -> List[int]:
    """Implementação do Quick Sort"""
    if len(arr) <= 1:
        return arr.copy()
    
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    
    return quick_sort(left) + middle + quick_sort(right)

def merge_sort(arr: List[int]) -> List[int]:
    """Implementação do Merge Sort"""
    if len(arr) <= 1:
        return arr.copy()
    
    mid = len(arr) // 2
    left = merge_sort(arr[:mid])
    right = merge_sort(arr[mid:])
    
    return merge(left, right)

def merge(left: List[int], right: List[int]) -> List[int]:
    """Função auxiliar para o Merge Sort"""
    result = []
    i = j = 0
    
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1
    
    result.extend(left[i:])
    result.extend(right[j:])
    return result

def insertion_sort(arr: List[int]) -> List[int]:
    """Implementação do Insertion Sort"""
    arr_copy = arr.copy()
    for i in range(1, len(arr_copy)):
        key = arr_copy[i]
        j = i - 1
        while j >= 0 and arr_copy[j] > key:
            arr_copy[j + 1] = arr_copy[j]
            j -= 1
        arr_copy[j + 1] = key
    return arr_copy

# =============================================================================
# 2. VERSÕES PARALELAS CORRIGIDAS
# =============================================================================

def parallel_bubble_sort_chunk(chunk: List[int]) -> List[int]:
    """Função auxiliar para Bubble Sort paralelo"""
    return bubble_sort(chunk)

def parallel_bubble_sort(arr: List[int], num_threads: int = 4) -> List[int]:
    """Versão paralela do Bubble Sort usando divisão de trabalho"""
    if len(arr) <= 1000:  # Threshold para usar versão serial
        return bubble_sort(arr)
    
    chunk_size = len(arr) // num_threads
    chunks = [arr[i:i + chunk_size] for i in range(0, len(arr), chunk_size)]
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=num_threads) as executor:
        sorted_chunks = list(executor.map(parallel_bubble_sort_chunk, chunks))
    
    # Combina os chunks ordenados
    result = []
    for chunk in sorted_chunks:
        result = merge(result, chunk)
    
    return result

def parallel_quick_sort(arr: List[int], num_threads: int = 4) -> List[int]:
    """Versão paralela do Quick Sort usando processos"""
    if len(arr) <= 1000 or num_threads <= 1:
        return quick_sort(arr)
    
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    
    # Usa ThreadPoolExecutor em vez de ProcessPoolExecutor para evitar problemas de serialização
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
        left_future = executor.submit(parallel_quick_sort, left, num_threads // 2)
        right_result = parallel_quick_sort(right, num_threads // 2)
        left_result = left_future.result()
    
    return left_result + middle + right_result

def parallel_merge_sort(arr: List[int], num_threads: int = 4) -> List[int]:
    """Versão paralela do Merge Sort"""
    if len(arr) <= 1000 or num_threads <= 1:
        return merge_sort(arr)
    
    mid = len(arr) // 2
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
        left_future = executor.submit(parallel_merge_sort, arr[:mid], num_threads // 2)
        right_future = executor.submit(parallel_merge_sort, arr[mid:], num_threads // 2)
        
        left = left_future.result()
        right = right_future.result()
    
    return merge(left, right)

def parallel_insertion_sort_chunk(chunk: List[int]) -> List[int]:
    """Função auxiliar para Insertion Sort paralelo"""
    return insertion_sort(chunk)

def parallel_insertion_sort(arr: List[int], num_threads: int = 4) -> List[int]:
    """Versão paralela do Insertion Sort usando divisão de trabalho"""
    if len(arr) <= 1000:  # Threshold para usar versão serial
        return insertion_sort(arr)
    
    chunk_size = len(arr) // num_threads
    chunks = [arr[i:i + chunk_size] for i in range(0, len(arr), chunk_size)]
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=num_threads) as executor:
        sorted_chunks = list(executor.map(parallel_insertion_sort_chunk, chunks))
    
    # Combina os chunks ordenados
    result = []
    for chunk in sorted_chunks:
        result = merge(result, chunk)
    
    return result

# =============================================================================
# 3. SISTEMA DE ANÁLISE CORRIGIDO
# =============================================================================

class PerformanceAnalyzer:
    def __init__(self):
        self.results = []
    
    def calculate_total_configurations(self, dataset_sizes, dataset_types, num_samples, max_threads):
        """Calcula o número total de configurações para a barra de progresso"""
        algorithms_count = 4  # bubble, quick, merge, insertion
        serial_configs = len(dataset_sizes) * len(dataset_types) * num_samples * algorithms_count
        parallel_configs = len(dataset_sizes) * len(dataset_types) * num_samples * algorithms_count * (len([2, 4]))
        return serial_configs + parallel_configs
        
    def generate_dataset(self, size: int, dataset_type: str) -> List[int]:
        """Gera diferentes tipos de conjuntos de dados"""
        random.seed(42)  # Para resultados reproduzíveis
        
        if dataset_type == "random":
            return [random.randint(1, size * 10) for _ in range(size)]
        elif dataset_type == "sorted":
            return list(range(size))
        elif dataset_type == "reverse_sorted":
            return list(range(size, 0, -1))
        elif dataset_type == "partially_sorted":
            arr = list(range(size))
            # Inverte algumas seções para criar dados parcialmente ordenados
            for i in range(0, size, size // 10):
                if i + 10 < size:
                    arr[i:i+10] = reversed(arr[i:i+10])
            return arr
        elif dataset_type == "duplicates":
            return [random.randint(1, size // 10) for _ in range(size)]
        else:
            raise ValueError(f"Tipo de dataset desconhecido: {dataset_type}")
    
    def measure_performance(self, sort_function: Callable, arr: List[int], 
                          algorithm: str, version: str, num_threads: int = 1, 
                          dataset_type: str = "random") -> Dict[str, Any]:
        """Mede o tempo de execução de uma função de ordenação"""
        start_time = time.time()
        try:
            result = sort_function(arr)
            end_time = time.time()
            
            # Verifica se o resultado está ordenado
            is_sorted = all(result[i] <= result[i + 1] for i in range(len(result) - 1))
            
            return {
                'algorithm': algorithm,
                'version': version,
                'dataset_size': len(arr),
                'dataset_type': dataset_type,
                'num_threads': num_threads,
                'execution_time': end_time - start_time,
                'is_sorted': is_sorted,
                'timestamp': time.time()
            }
        except Exception as e:
            print(f"Erro durante a execução do {algorithm} {version}: {e}")
            return {
                'algorithm': algorithm,
                'version': version,
                'dataset_size': len(arr),
                'dataset_type': dataset_type,
                'num_threads': num_threads,
                'execution_time': float('inf'),
                'is_sorted': False,
                'timestamp': time.time(),
                'error': str(e)
            }
    
    def run_analysis(self, dataset_sizes: List[int], num_samples: int = 5, 
                    max_threads: int = 4, dataset_types: List[str] = None):
        """Executa análise completa de desempenho"""
        if dataset_types is None:
            dataset_types = ["random", "sorted", "reverse_sorted"]
        
        # Mapeamento de algoritmos para suas funções
        algorithms = {
            'bubble': {
                'serial': bubble_sort,
                'parallel': parallel_bubble_sort
            },
            'quick': {
                'serial': quick_sort, 
                'parallel': parallel_quick_sort
            },
            'merge': {
                'serial': merge_sort,
                'parallel': parallel_merge_sort
            },
            'insertion': {
                'serial': insertion_sort,
                'parallel': parallel_insertion_sort
            }
        }
        
        # CALCULA o total_config ANTES de começar
        total_config = self.calculate_total_configurations(
            dataset_sizes, dataset_types, num_samples, max_threads
        )
        current_config = 0
        
        print("Iniciando análise de desempenho...")
        print(f"Tamanhos de dataset: {dataset_sizes}")
        print(f"Tipos de dataset: {dataset_types}")
        print(f"Máximo de threads: {max_threads}")
        print(f"Amostras por configuração: {num_samples}")
        print(f"Total de configurações: {total_config}")
        print("=" * 60)
        
        for size in dataset_sizes:
            print(f"\nAnalisando tamanho: {size}")
            
            for dataset_type in dataset_types:
                print(f"  Tipo: {dataset_type}")
                
                for sample in range(num_samples):
                    # Gera dataset para esta amostra
                    dataset = self.generate_dataset(size, dataset_type)
                    
                    for algo_name, versions in algorithms.items():
                        # Execução serial
                        current_config += 1
                        print(f"    [{current_config}/{total_config}] {algo_name} serial...")
                        
                        serial_result = self.measure_performance(
                            versions['serial'], dataset, algo_name, 'serial', 1, dataset_type
                        )
                        self.results.append(serial_result)
                        
                        # Execuções paralelas com diferentes números de threads
                        for num_threads in [2, 4]:
                            if num_threads <= max_threads:
                                current_config += 1
                                print(f"    [{current_config}/{total_config}] {algo_name} paralelo ({num_threads} threads)...")
                                
                                try:
                                    parallel_result = self.measure_performance(
                                        lambda arr: versions['parallel'](arr, num_threads),
                                        dataset, algo_name, 'parallel', num_threads, dataset_type
                                    )
                                    self.results.append(parallel_result)
                                except Exception as e:
                                    print(f"      Erro: {e}")
                                    # Registra o erro
                                    self.results.append({
                                        'algorithm': algo_name,
                                        'version': 'parallel', 
                                        'dataset_size': size,
                                        'dataset_type': dataset_type,
                                        'num_threads': num_threads,
                                        'execution_time': float('inf'),
                                        'is_sorted': False,
                                        'timestamp': time.time(),
                                        'error': str(e)
                                    })
        
        print("\nAnálise concluída!")
    
    def save_to_csv(self, filename: str = "sorting_analysis_results.csv"):
        """Salva os resultados em arquivo CSV"""
        if not self.results:
            print("Nenhum resultado para salvar.")
            return
        
        # Cria diretório se não existir
        os.makedirs(os.path.dirname(filename) if os.path.dirname(filename) else '.', exist_ok=True)
        
        df = pd.DataFrame(self.results)
        df.to_csv(filename, index=False)
        print(f"Resultados salvos em {filename}")
        return df
    
    def get_summary_statistics(self) -> pd.DataFrame:
        """Retorna estatísticas resumidas dos resultados"""
        if not self.results:
            return pd.DataFrame()
        
        df = pd.DataFrame(self.results)
        
        # Remove resultados com erro para estatísticas
        valid_results = df[df['execution_time'] != float('inf')]
        
        if len(valid_results) == 0:
            print("Nenhum resultado válido para estatísticas.")
            return pd.DataFrame()
            
        summary = valid_results.groupby(['algorithm', 'version', 'dataset_size', 'dataset_type', 'num_threads']).agg({
            'execution_time': ['mean', 'std', 'min', 'max'],
            'is_sorted': 'all'
        }).round(6)
        
        summary.columns = ['time_mean', 'time_std', 'time_min', 'time_max', 'all_sorted']
        summary = summary.reset_index()
        
        return summary

# =============================================================================
# 4. EXECUÇÃO SIMPLIFICADA PARA TESTE
# =============================================================================

def run_demo_analysis():
    """Executa uma análise de demonstração rápida"""
    analyzer = PerformanceAnalyzer()
    
    # Configurações menores para teste rápido
    dataset_sizes = [100, 500, 1000]
    dataset_types = ["random", "sorted"]
    
    print("=== EXECUÇÃO DE DEMONSTRAÇÃO ===")
    print("Esta é uma versão simplificada para teste.")
    print("Use configurações maiores para análise completa.\n")
    
    analyzer.run_analysis(
        dataset_sizes=dataset_sizes,
        num_samples=3,  # Menos amostras para teste rápido
        max_threads=4,
        dataset_types=dataset_types
    )
    
    # Salva resultados
    df_results = analyzer.save_to_csv("demo_analysis.csv")
    
    # Estatísticas resumidas
    summary = analyzer.get_summary_statistics()
    if not summary.empty:
        summary.to_csv("demo_summary.csv", index=False)
        print("\nEstatísticas resumidas:")
        print(summary.head())
    else:
        print("\nNenhuma estatística disponível devido a erros.")
    
    return analyzer

def main():
    """Função principal"""
    try:
        analyzer = run_demo_analysis()
        
        # Mostra alguns resultados
        if analyzer.results:
            print(f"\nTotal de execuções: {len(analyzer.results)}")
            
            # Filtra resultados válidos
            valid_results = [r for r in analyzer.results if r.get('execution_time', float('inf')) != float('inf')]
            error_results = [r for r in analyzer.results if r.get('execution_time', float('inf')) == float('inf')]
            
            print(f"Execuções válidas: {len(valid_results)}")
            print(f"Execuções com erro: {len(error_results)}")
            
            if error_results:
                print("\nErros encontrados:")
                for error in error_results[:5]:  # Mostra apenas os primeiros 5 erros
                    print(f"  {error.get('algorithm')} {error.get('version')}: {error.get('error', 'Erro desconhecido')}")
    
    except Exception as e:
        print(f"Erro durante a execução: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()