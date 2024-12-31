import pandas as pd
import numpy as np
from scipy import stats
import statsmodels.api as sm
from statsmodels.stats.multicomp import pairwise_tukeyhsd
from statsmodels.stats.multicomp import MultiComparison
import matplotlib.pyplot as plt

# 1. Load the CSV data
data = pd.read_csv('metricas_pgh.csv')

metric = data['HIPERVOLUMEN'].values

# 2. Extract the fitness values for each configuration
configurations = []

# Every 30 rows comes a new configuration tested
chunk_size = 30
num_chunks = 27 # 810 / 30

for i in range(num_chunks):
    configurations.append(metric[i * chunk_size : (i + 1) * chunk_size])

configurations = np.array(configurations)
flat_data = configurations.flatten()

groups = np.concatenate([np.repeat(i, len(configurations[i])) for i in range(len(configurations))])

mc = MultiComparison(flat_data, groups)
result = mc.allpairtest(stats.ttest_ind, method='bonferroni')  # Bonferroni adjustment

# The results are in a tuple, with the first element being a SimpleTable
result_table = result[0]  # This contains the pairwise comparisons and p-values

# Print the results for debugging (you can keep or remove this line if you don't need the results printed)
print(result_table)

# 4. Box plot for visualizing the configurations
plt.figure(figsize=(20, 10))  # Increased width to make more space

# Box plot showing distributions of HIPERVOLUMEN across configurations
plt.boxplot(configurations.T, vert=False, patch_artist=True)

# Adding labels and title
plt.title("Box Plot para el hipervolumen de diferentes configuraciones")
plt.xlabel("Hipervolumen")
plt.ylabel("Configuracion")

# Show the plot
plt.tight_layout()
plt.savefig('box_plot.png')  # You can change the file format here
plt.show()