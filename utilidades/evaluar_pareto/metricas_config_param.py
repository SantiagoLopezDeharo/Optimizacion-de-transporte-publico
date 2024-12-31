import statistics
import pandas as pd

metricas = pd.read_csv('metricas_pgh.csv')

# Every 30 rows comes a new configuration tested
chunk_size = 30
num_chunks = 27 # 810 / 30

print("CONFIGURACION, PROMEDIO, MEJOR, PEOR, DESVIACION ESTANDAR")

for i in range(num_chunks):
    # Get the subset of the DataFrame for the current chunk
    chunk = metricas.iloc[i * chunk_size : (i + 1) * chunk_size]
    
    # Extract the hypervolume values for the current chunk
    hipervolumen = chunk['HIPERVOLUMEN'].values
    
    # Calculate the statistics for this chunk
    hipervolumen_promedio = statistics.mean(hipervolumen)
    mejor_hipervolumen = hipervolumen.max()
    peor_hipervolumen = hipervolumen.min()
    st_dev_hipervolumen = statistics.stdev(hipervolumen)

    # Print the results for the current chunk
    print(f"Chunk {i + 1} - {hipervolumen_promedio}, {st_dev_hipervolumen}, {mejor_hipervolumen}, {peor_hipervolumen}")