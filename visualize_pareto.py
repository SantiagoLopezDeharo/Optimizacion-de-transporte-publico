#!/usr/bin/env python3
"""
Script para visualizar el frente de Pareto y analizar métricas de desempeño
del algoritmo genético con diferentes combinaciones de pesos.
"""

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
from mpl_toolkits.mplot3d import Axes3D
import glob
import os
import io

# Configurar estilo
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (12, 8)

def load_latest_pareto_results():
    """Carga el archivo más reciente de resultados de Pareto"""
    files = glob.glob('paradas/pareto_results_*.csv')
    if not files:
        raise FileNotFoundError("No se encontraron archivos pareto_results_*.csv")
    
    latest_file = max(files, key=os.path.getctime)
    print(f"Cargando datos desde: {latest_file}")

    # Some historical files were generated with literal "\\n" instead of real newlines.
    # If so, fix in-memory so pandas can parse the CSV correctly.
    with open(latest_file, 'r', encoding='utf-8-sig', newline='') as f:
        raw = f.read()

    if '\n' not in raw and '\\n' in raw:
        raw = raw.replace('\\n', '\n')
        df = pd.read_csv(io.StringIO(raw))
    else:
        df = pd.read_csv(io.StringIO(raw))

    # Normalize column names
    df.columns = [c.strip() for c in df.columns]

    required_cols = {'w_coverage', 'w_stops', 'w_cost', 'coverage', 'num_stops', 'cost', 'fitness'}
    missing = required_cols - set(df.columns)
    if missing:
        raise ValueError(
            f"El archivo {latest_file} no tiene las columnas esperadas. Faltan: {sorted(missing)}. "
            f"Columnas detectadas: {list(df.columns)}. "
            "Si el archivo fue generado con \\\\n literales, vuelve a correr el generador Java actualizado."
        )

    if len(df) == 0:
        raise ValueError(
            f"El archivo {latest_file} no contiene filas de datos (0 soluciones). "
            "Eso suele pasar si el algoritmo no guardó resultados o si el CSV está mal formado."
        )

    return df, latest_file

def plot_pareto_3d(df, output_file='pareto_3d.png'):
    """Genera gráfico 3D del frente de Pareto"""
    fig = plt.figure(figsize=(14, 10))
    ax = fig.add_subplot(111, projection='3d')
    
    # Crear gradiente de color basado en fitness
    colors = df['fitness']
    scatter = ax.scatter(df['coverage'], df['num_stops'], df['cost'], 
                        c=colors, cmap='viridis', s=100, alpha=0.6, edgecolors='black')
    
    ax.set_xlabel('Coverage (Cobertura)', fontsize=12, fontweight='bold')
    ax.set_ylabel('Number of Stops (Paradas)', fontsize=12, fontweight='bold')
    ax.set_zlabel('Cost (Costo)', fontsize=12, fontweight='bold')
    ax.set_title('Frente de Pareto - Espacio de Objetivos 3D', fontsize=14, fontweight='bold')
    
    # Agregar barra de color
    cbar = plt.colorbar(scatter, ax=ax, pad=0.1, shrink=0.8)
    cbar.set_label('Fitness Agregado', rotation=270, labelpad=20, fontweight='bold')
    
    plt.tight_layout()
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Gráfico 3D guardado en: {output_file}")
    plt.close()

def plot_pareto_2d_projections(df, output_file='pareto_2d_projections.png'):
    """Genera proyecciones 2D del frente de Pareto"""
    fig, axes = plt.subplots(2, 2, figsize=(16, 12))
    
    # Coverage vs Stops
    ax1 = axes[0, 0]
    scatter1 = ax1.scatter(df['coverage'], df['num_stops'], 
                          c=df['fitness'], cmap='viridis', s=100, alpha=0.6, edgecolors='black')
    ax1.set_xlabel('Coverage (Cobertura)', fontweight='bold')
    ax1.set_ylabel('Number of Stops (Paradas)', fontweight='bold')
    ax1.set_title('Coverage vs Number of Stops', fontweight='bold')
    plt.colorbar(scatter1, ax=ax1, label='Fitness')
    
    # Coverage vs Cost
    ax2 = axes[0, 1]
    scatter2 = ax2.scatter(df['coverage'], df['cost'], 
                          c=df['fitness'], cmap='viridis', s=100, alpha=0.6, edgecolors='black')
    ax2.set_xlabel('Coverage (Cobertura)', fontweight='bold')
    ax2.set_ylabel('Cost (Costo)', fontweight='bold')
    ax2.set_title('Coverage vs Cost', fontweight='bold')
    plt.colorbar(scatter2, ax=ax2, label='Fitness')
    
    # Stops vs Cost
    ax3 = axes[1, 0]
    scatter3 = ax3.scatter(df['num_stops'], df['cost'], 
                          c=df['fitness'], cmap='viridis', s=100, alpha=0.6, edgecolors='black')
    ax3.set_xlabel('Number of Stops (Paradas)', fontweight='bold')
    ax3.set_ylabel('Cost (Costo)', fontweight='bold')
    ax3.set_title('Number of Stops vs Cost', fontweight='bold')
    plt.colorbar(scatter3, ax=ax3, label='Fitness')
    
    # Fitness vs Weight Coverage
    ax4 = axes[1, 1]
    scatter4 = ax4.scatter(df['w_coverage'], df['fitness'], 
                          c=df['fitness'], cmap='viridis', s=100, alpha=0.6, edgecolors='black')
    ax4.set_xlabel('Weight Coverage (w1)', fontweight='bold')
    ax4.set_ylabel('Fitness', fontweight='bold')
    ax4.set_title('Fitness vs Weight Coverage', fontweight='bold')
    plt.colorbar(scatter4, ax=ax4, label='Fitness')
    
    plt.tight_layout()
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Proyecciones 2D guardadas en: {output_file}")
    plt.close()

def plot_weight_influence(df, output_file='weight_influence.png'):
    """Analiza la influencia de los pesos en los objetivos"""
    fig, axes = plt.subplots(2, 2, figsize=(16, 12))
    
    # Weight Coverage vs Coverage
    ax1 = axes[0, 0]
    scatter1 = ax1.scatter(df['w_coverage'], df['coverage'], 
                          c=df['fitness'], cmap='coolwarm', s=100, alpha=0.6, edgecolors='black')
    ax1.set_xlabel('Weight Coverage (w1)', fontweight='bold')
    ax1.set_ylabel('Coverage Achieved', fontweight='bold')
    ax1.set_title('Influencia de w1 en Coverage', fontweight='bold')
    plt.colorbar(scatter1, ax=ax1, label='Fitness')
    
    # Weight Stops vs Stops
    ax2 = axes[0, 1]
    scatter2 = ax2.scatter(df['w_stops'], df['num_stops'], 
                          c=df['fitness'], cmap='coolwarm', s=100, alpha=0.6, edgecolors='black')
    ax2.set_xlabel('Weight Stops (w2)', fontweight='bold')
    ax2.set_ylabel('Number of Stops', fontweight='bold')
    ax2.set_title('Influencia de w2 en Number of Stops', fontweight='bold')
    plt.colorbar(scatter2, ax=ax2, label='Fitness')
    
    # Weight Cost vs Cost
    ax3 = axes[1, 0]
    scatter3 = ax3.scatter(df['w_cost'], df['cost'], 
                          c=df['fitness'], cmap='coolwarm', s=100, alpha=0.6, edgecolors='black')
    ax3.set_xlabel('Weight Cost (w3)', fontweight='bold')
    ax3.set_ylabel('Cost Achieved', fontweight='bold')
    ax3.set_title('Influencia de w3 en Cost', fontweight='bold')
    plt.colorbar(scatter3, ax=ax3, label='Fitness')
    
    # Combined weight distribution
    ax4 = axes[1, 1]
    ax4.scatter(df['w_coverage'], df['w_stops'], 
               s=df['fitness']*200, c=df['fitness'], cmap='viridis', alpha=0.6, edgecolors='black')
    ax4.set_xlabel('Weight Coverage (w1)', fontweight='bold')
    ax4.set_ylabel('Weight Stops (w2)', fontweight='bold')
    ax4.set_title('Distribución de Pesos (tamaño = fitness)', fontweight='bold')
    
    plt.tight_layout()
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Análisis de influencia de pesos guardado en: {output_file}")
    plt.close()

def plot_fitness_heatmap(df, output_file='fitness_heatmap.png'):
    """Genera heatmap del fitness en función de las combinaciones de pesos"""
    fig, axes = plt.subplots(1, 2, figsize=(16, 6))
    
    # Heatmap w1 vs w2
    pivot1 = df.pivot_table(values='fitness', index='w_stops', columns='w_coverage', aggfunc='mean')
    sns.heatmap(pivot1, annot=False, fmt='.3f', cmap='YlOrRd', ax=axes[0], cbar_kws={'label': 'Fitness'})
    axes[0].set_title('Fitness: Weight Coverage vs Weight Stops', fontweight='bold')
    axes[0].set_xlabel('Weight Coverage (w1)', fontweight='bold')
    axes[0].set_ylabel('Weight Stops (w2)', fontweight='bold')
    
    # Heatmap w1 vs w3
    pivot2 = df.pivot_table(values='fitness', index='w_cost', columns='w_coverage', aggfunc='mean')
    sns.heatmap(pivot2, annot=False, fmt='.3f', cmap='YlOrRd', ax=axes[1], cbar_kws={'label': 'Fitness'})
    axes[1].set_title('Fitness: Weight Coverage vs Weight Cost', fontweight='bold')
    axes[1].set_xlabel('Weight Coverage (w1)', fontweight='bold')
    axes[1].set_ylabel('Weight Cost (w3)', fontweight='bold')
    
    plt.tight_layout()
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Heatmap de fitness guardado en: {output_file}")
    plt.close()

def generate_statistics(df, output_file='pareto_statistics.txt'):
    """Genera estadísticas descriptivas del frente de Pareto"""
    with open(output_file, 'w') as f:
        f.write("=" * 70 + "\n")
        f.write("ESTADÍSTICAS DEL FRENTE DE PARETO\n")
        f.write("=" * 70 + "\n\n")
        
        f.write("RESUMEN DE OBJETIVOS:\n")
        f.write("-" * 70 + "\n")
        f.write(df[['coverage', 'num_stops', 'cost', 'fitness']].describe().to_string())
        f.write("\n\n")
        
        f.write("MEJORES SOLUCIONES POR OBJETIVO:\n")
        f.write("-" * 70 + "\n")
        
        # Mejor coverage
        best_coverage = df.loc[df['coverage'].idxmax()]
        f.write(f"\nMejor Coverage:\n")
        f.write(f"  Coverage: {best_coverage['coverage']:.2f}\n")
        f.write(f"  Stops: {best_coverage['num_stops']:.2f}\n")
        f.write(f"  Cost: {best_coverage['cost']:.2f}\n")
        f.write(f"  Weights: w1={best_coverage['w_coverage']:.4f}, w2={best_coverage['w_stops']:.4f}, w3={best_coverage['w_cost']:.4f}\n")
        
        # Mínimo stops
        min_stops = df.loc[df['num_stops'].idxmin()]
        f.write(f"\nMínimo Number of Stops:\n")
        f.write(f"  Coverage: {min_stops['coverage']:.2f}\n")
        f.write(f"  Stops: {min_stops['num_stops']:.2f}\n")
        f.write(f"  Cost: {min_stops['cost']:.2f}\n")
        f.write(f"  Weights: w1={min_stops['w_coverage']:.4f}, w2={min_stops['w_stops']:.4f}, w3={min_stops['w_cost']:.4f}\n")
        
        # Mínimo cost
        min_cost = df.loc[df['cost'].idxmin()]
        f.write(f"\nMínimo Cost:\n")
        f.write(f"  Coverage: {min_cost['coverage']:.2f}\n")
        f.write(f"  Stops: {min_cost['num_stops']:.2f}\n")
        f.write(f"  Cost: {min_cost['cost']:.2f}\n")
        f.write(f"  Weights: w1={min_cost['w_coverage']:.4f}, w2={min_cost['w_stops']:.4f}, w3={min_cost['w_cost']:.4f}\n")
        
        # Mejor fitness
        best_fitness = df.loc[df['fitness'].idxmax()]
        f.write(f"\nMejor Fitness Global:\n")
        f.write(f"  Coverage: {best_fitness['coverage']:.2f}\n")
        f.write(f"  Stops: {best_fitness['num_stops']:.2f}\n")
        f.write(f"  Cost: {best_fitness['cost']:.2f}\n")
        f.write(f"  Fitness: {best_fitness['fitness']:.6f}\n")
        f.write(f"  Weights: w1={best_fitness['w_coverage']:.4f}, w2={best_fitness['w_stops']:.4f}, w3={best_fitness['w_cost']:.4f}\n")
        
        f.write("\n" + "=" * 70 + "\n")
    
    print(f"Estadísticas guardadas en: {output_file}")

def main():
    """Función principal"""
    print("=" * 70)
    print("VISUALIZACIÓN DEL FRENTE DE PARETO")
    print("=" * 70)
    print()
    
    try:
        # Cargar datos
        df, source_file = load_latest_pareto_results()
        print(f"Total de soluciones: {len(df)}")
        print()
        
        # Crear directorio para outputs si no existe
        output_dir = "pareto_analysis"
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)
        
        # Generar visualizaciones
        print("Generando visualizaciones...")
        plot_pareto_3d(df, f'{output_dir}/pareto_3d.png')
        plot_pareto_2d_projections(df, f'{output_dir}/pareto_2d_projections.png')
        plot_weight_influence(df, f'{output_dir}/weight_influence.png')
        plot_fitness_heatmap(df, f'{output_dir}/fitness_heatmap.png')
        
        # Generar estadísticas
        print("\nGenerando estadísticas...")
        generate_statistics(df, f'{output_dir}/pareto_statistics.txt')
        
        print("\n" + "=" * 70)
        print("PROCESO COMPLETADO")
        print(f"Todos los archivos guardados en el directorio: {output_dir}/")
        print("=" * 70)
        
    except FileNotFoundError as e:
        print(f"Error: {e}")
        print("Por favor, ejecuta primero el algoritmo genético para generar los datos.")
    except Exception as e:
        print(f"Error inesperado: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
