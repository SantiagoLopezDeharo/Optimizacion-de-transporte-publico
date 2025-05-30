import pandas as pd
import numpy as np
from scipy import stats

# 1. Read the CSV file
metricas = pd.read_csv('metricas_pgh.csv')

# Every 30 rows comes a new configuration tested
chunk_size = 30
num_chunks = 27 # 810 / 30

for i in range(num_chunks):
    # Get the subset of the DataFrame for the current chunk
    chunk = metricas.iloc[i * chunk_size : (i + 1) * chunk_size]
    
    # Extract the hypervolume values for the current chunk
    hipervolumen = chunk['HIPERVOLUMEN'].values
    
    # Calculate the statistics for this chunk
    hipervolumen_promedio = np.mean(hipervolumen)
    st_dev_hipervolumen = np.std(hipervolumen)

    # Perform the Kolmogorov-Smirnov test for normality
    ks_statistic, ks_p_value = stats.kstest(hipervolumen, 'norm', args=(hipervolumen_promedio, st_dev_hipervolumen))

    # Print the results for the current chunk
    print(f"Chunk {i + 1} - {ks_statistic}, {ks_p_value}")