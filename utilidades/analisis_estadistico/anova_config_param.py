import pandas as pd
from scipy import stats

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

# 3. Perform the one-way ANOVA test
f_statistic, p_value = stats.f_oneway(*configurations)

# 4. Output the results
print(f"ANOVA test statistic: {f_statistic}")
print(f"ANOVA p-value: {p_value}")

# 5. Interpretation of the results
if p_value < 0.05:
    print("Reject the null hypothesis: There is a significant difference between the configuration means.")
else:
    print("Fail to reject the null hypothesis: There is no significant difference between the configuration means.")