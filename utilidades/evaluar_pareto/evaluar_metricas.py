import statistics
import pandas as pd

metricas = pd.read_csv('metricas_mvd.csv')

tasa_error = metricas['TASA_ERROR'].values
dist_gen = metricas['DISTANCIA_GENERACIONAL'].values
spread = metricas['SPREAD'].values
hipervolumen = metricas['HIPERVOLUMEN'].values

tasa_promedio = statistics.mean(tasa_error)
dist_gen_promedio = statistics.mean(dist_gen)
spread_promedio = statistics.mean(spread)
hipervolumen_promedio = statistics.mean(hipervolumen)

mejor_tasa = tasa_error.min()
mejor_dist_gen = dist_gen.min()
mejor_spread = spread.min()
mejor_hipervolumen = hipervolumen.max()

peor_tasa = tasa_error.max()
peor_dist_gen = dist_gen.max()
peor_spread = spread.max()
peor_hipervolumen = hipervolumen.min()

st_dev_tasa = statistics.stdev(tasa_error)
st_dev_dist_gen = statistics.stdev(dist_gen)
st_dev_spread = statistics.stdev(spread)
st_dev_hipervolumen = statistics.stdev(hipervolumen)

print("METRICA, PROMEDIO, MEJOR, PEOR, DESVIACION ESTANDAR")
print("TASA_ERROR, {}, {}, {}, {}".format(tasa_promedio, mejor_tasa, peor_tasa, st_dev_tasa))
print("DISTANCIA_GENERACIONAL, {}, {}, {}, {}".format(dist_gen_promedio, mejor_dist_gen, peor_dist_gen, st_dev_dist_gen))
print("SPREAD, {}, {}, {}, {}".format(spread_promedio, mejor_spread, peor_spread, st_dev_spread))
print("HIPERVOLUMEN, {}, {}, {}, {}".format(hipervolumen_promedio, mejor_hipervolumen, peor_hipervolumen, st_dev_hipervolumen))