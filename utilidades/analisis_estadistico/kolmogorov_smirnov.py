import pandas as pd
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt
import seaborn as sns

# 1. Read the CSV file
metricas = pd.read_csv('metricas_mvd.csv')

tasa_error = metricas['TASA_ERROR'].values
dist_gen = metricas['DISTANCIA_GENERACIONAL'].values
spread = metricas['SPREAD'].values
hipervolumen = metricas['HIPERVOLUMEN'].values

# 3. Plot a histogram and a Q-Q plot for visualization
plt.figure(figsize=(12, 6))

# Histogram with a KDE (Kernel Density Estimate) overlay
plt.subplot(1, 2, 1)
sns.histplot(tasa_error, kde=True, bins=30, color='skyblue')
plt.title('Histogram with KDE (Tasa_error)')

# Q-Q Plot to visually check normality
plt.subplot(1, 2, 2)
stats.probplot(tasa_error, dist="norm", plot=plt)
plt.title('Q-Q Plot')

plt.tight_layout()

plt.savefig('tasa_error.png')  # You can change the file format here

plt.show()

# Calculate the statistics
tasa_promedio = np.mean(tasa_error)
dist_gen_promedio = np.mean(dist_gen)
spread_promedio = np.mean(spread)
hipervolumen_promedio = np.mean(hipervolumen)

st_dev_tasa = np.std(tasa_error)
st_dev_dist_gen = np.std(dist_gen)
st_dev_spread = np.std(spread)
st_dev_hipervolumen = np.std(hipervolumen)

# Perform the Kolmogorov-Smirnov test for normality
ks_statistic_tasa_error, ks_p_value_tasa_error = stats.kstest(tasa_error, 'norm', args=(tasa_promedio, st_dev_tasa))
ks_statistic_dist_gen, ks_p_value_dist_gen = stats.kstest(dist_gen, 'norm', args=(dist_gen_promedio, st_dev_dist_gen))
ks_statistic_spread, ks_p_value_spread = stats.kstest(spread, 'norm', args=(spread_promedio, st_dev_spread))
ks_statistic_hipervolumen, ks_p_value_hipervolumen = stats.kstest(hipervolumen, 'norm', args=(hipervolumen_promedio, st_dev_hipervolumen))

# Print the results
print(f"Tasa_error - {round(ks_statistic_tasa_error,4)}, {round(ks_p_value_tasa_error,4)}")
print(f"Distancia_generacional - {round(ks_statistic_dist_gen,4)}, {round(ks_p_value_dist_gen,4)}")
print(f"Spread - {round(ks_statistic_spread,4)}, {round(ks_p_value_spread,4)}")
print(f"Hipervolumen - {round(ks_statistic_hipervolumen,4)}, {round(ks_p_value_hipervolumen,4)}")