# Análisis del Frente de Pareto - Optimización de Transporte Público

Este documento describe cómo ejecutar el algoritmo genético con múltiples combinaciones de pesos para aproximar el frente de Pareto y analizar las métricas de desempeño.

## Resumen de Cambios Implementados

### 1. Modificaciones en `ParadasProblem.java`

- **Pesos configurables**: Los pesos ahora se pueden establecer al crear el problema, en lugar de ser constantes.
- **Almacenamiento de objetivos originales**: Se guardan los valores de cobertura, número de paradas y costo antes de la agregación.
- **Nuevos métodos**:
  - `getOriginalObjectives(IntegerSolution)`: Retorna los valores originales de los objetivos [coverage, numStops, cost]
  - `getBestSolution(List<IntegerSolution>)`: Obtiene la mejor solución según el fitness agregado

### 2. Modificaciones en `Main.java`

- **Lectura de pesos**: Nuevo método `readWeights()` que lee el archivo `pesos.csv` desde los recursos.
- **Clases auxiliares**:
  - `WeightCombination`: Almacena una combinación de pesos (w1, w2, w3)
  - `ParetoResult`: Almacena los resultados para cada combinación
- **Ejecución iterativa**: El método `main()` ahora:
  - Lee todas las combinaciones de pesos de `pesos.csv`
  - Ejecuta el algoritmo múltiples veces (por defecto 3) por cada combinación
  - Guarda la mejor solución de cada combinación
  - Genera un archivo CSV con todos los resultados

### 3. Script de Visualización Python

`visualize_pareto.py` genera:
- Gráfico 3D del frente de Pareto en el espacio de objetivos
- Proyecciones 2D de todas las combinaciones de objetivos
- Análisis de influencia de los pesos en cada objetivo
- Heatmaps del fitness según combinaciones de pesos
- Estadísticas descriptivas completas

## Estructura de Archivos

```
proyecto/
├── paradas/
│   ├── src/main/
│   │   ├── java/com/paradas/
│   │   │   ├── Main.java (modificado)
│   │   │   └── Abstraccion/
│   │   │       └── ParadasProblem.java (modificado)
│   │   └── resources/
│   │       ├── pesos.csv (requerido)
│   │       └── data_mvd.csv
│   └── pom.xml
├── visualize_pareto.py (nuevo)
└── PARETO_README.md (este archivo)
```

## Cómo Ejecutar

### Paso 1: Compilar el Proyecto Java

```bash
cd paradas
mvn clean package
```

### Paso 2: Ejecutar el Algoritmo

```bash
mvn exec:java -Dexec.mainClass="com.paradas.Main"
```

**Nota**: Este proceso puede tardar varias horas dependiendo del número de combinaciones de pesos y ejecuciones por combinación.

### Configuración de Parámetros

En `Main.java`, puedes ajustar:

```java
int runsPerCombination = 3;     // Ejecuciones por combinación (estabilidad estadística)
int populationSize = 300;        // Tamaño de población
int maxEvaluations = 60000;      // Evaluaciones máximas por ejecución
double crossoverProbability = 0.9;
double mutationProbability = 0.06;
```

### Paso 3: Visualizar Resultados

```bash
cd ..
python visualize_pareto.py
```

Requisitos Python:
```bash
pip install pandas matplotlib seaborn numpy
```

## Archivos de Salida

### Durante la Ejecución del Algoritmo

El algoritmo genera un archivo:
- `pareto_results_YYYY-MM-DD_HH-mm-ss.csv`: Resultados de todas las combinaciones de pesos

Formato del CSV:
```csv
w_coverage,w_stops,w_cost,coverage,num_stops,cost,fitness
0.4000,0.0500,0.5500,123456.00,45.00,123.45,0.856234
...
```

Donde:
- `w_coverage`, `w_stops`, `w_cost`: Pesos utilizados (de pesos.csv)
- `coverage`: Cobertura de pasajeros obtenida (valor bruto)
- `num_stops`: Número de paradas instaladas (valor bruto)
- `cost`: Costo ponderado por demanda (valor bruto)
- `fitness`: Fitness agregado obtenido (valor normalizado)

### Después de la Visualización

El script Python crea una carpeta `pareto_analysis/` con:

1. **`pareto_3d.png`**: Visualización 3D del frente de Pareto
   - Ejes: Coverage, Number of Stops, Cost
   - Color: Fitness agregado

2. **`pareto_2d_projections.png`**: Cuatro proyecciones 2D
   - Coverage vs Stops
   - Coverage vs Cost
   - Stops vs Cost
   - Fitness vs Weight Coverage

3. **`weight_influence.png`**: Análisis de influencia de pesos
   - Cómo afecta cada peso a su objetivo correspondiente
   - Distribución de pesos

4. **`fitness_heatmap.png`**: Mapas de calor
   - Fitness según w1 vs w2
   - Fitness según w1 vs w3

5. **`pareto_statistics.txt`**: Estadísticas descriptivas
   - Resumen estadístico de todos los objetivos
   - Mejores soluciones por cada objetivo
   - Mejor solución global

## Interpretación de Resultados

### Frente de Pareto

El frente de Pareto representa el conjunto de soluciones óptimas donde no es posible mejorar un objetivo sin empeorar otro. En nuestro caso:

- **Coverage (Cobertura)**: Queremos MAXIMIZAR (más pasajeros cubiertos)
- **Number of Stops**: Queremos MINIMIZAR (menos infraestructura)
- **Cost**: Queremos MINIMIZAR (menor costo en áreas de baja demanda)

### Trade-offs Típicos

1. **Alta cobertura → Más paradas**: Para cubrir más pasajeros, generalmente se necesitan más paradas
2. **Pocas paradas → Menor cobertura**: Minimizar paradas reduce la cobertura
3. **Balance**: El frente de Pareto muestra las mejores combinaciones posibles

### Análisis de Pesos

- **w1 (Coverage)**: Mayor peso → soluciones con más cobertura
- **w2 (Stops)**: Mayor peso → soluciones con menos paradas
- **w3 (Cost)**: Mayor peso → soluciones con menor costo en áreas de baja demanda

## Métricas de Desempeño

Para comparar el desempeño del algoritmo con diferentes pesos:

1. **Diversidad del frente**: ¿Qué tan dispersas están las soluciones?
2. **Convergencia**: ¿Qué tan buenos son los valores de fitness obtenidos?
3. **Consistencia**: ¿Las múltiples ejecuciones producen resultados similares?

### Métricas Adicionales (Futuras)

Para análisis más riguroso, podrías calcular:

- **Hypervolume**: Volumen del espacio de objetivos dominado por el frente
- **Spacing**: Uniformidad de la distribución de soluciones
- **IGD (Inverted Generational Distance)**: Distancia al frente de Pareto verdadero

## Ejemplo de Uso

```bash
# 1. Compilar
cd paradas
mvn clean package

# 2. Ejecutar (esto tardará varias horas)
mvn exec:java -Dexec.mainClass="com.paradas.Main"

# Salida esperada:
# Loaded 125 weight combinations
# Using 8 threads for parallel evaluation
# 
# [1/125] Running with weights: w1=0.4000, w2=0.0500, w3=0.5500
#   Run 1/3...
#   Run 2/3...
#   Run 3/3...
#   Best result: Coverage=123456.00, Stops=45.00, Cost=123.45, Fitness=0.856234
# ...
# Pareto results saved to: pareto_results_2025-12-21_15-30-00.csv

# 3. Visualizar
cd ..
python visualize_pareto.py

# Salida esperada:
# Cargando datos desde: pareto_results_2025-12-21_15-30-00.csv
# Total de soluciones: 125
# Generando visualizaciones...
# Gráfico 3D guardado en: pareto_analysis/pareto_3d.png
# ...
# Todos los archivos guardados en el directorio: pareto_analysis/
```

## Personalización

### Modificar Combinaciones de Pesos

Edita `paradas/src/main/resources/pesos.csv` para probar diferentes combinaciones. Por ejemplo:

```csv
f1,f2,f3,Suma
0.5,0.25,0.25,1.0
0.33,0.33,0.34,1.0
0.8,0.1,0.1,1.0
```

**Importante**: La suma debe ser 1.0 para cada fila.

### Ajustar Ejecuciones por Combinación

En `Main.java`, modifica:
```java
int runsPerCombination = 5; // Aumentar para más estabilidad
```

### Cambiar Dataset

Modifica en `Main.java`:
```java
Map<String, Map<String, Integer>> matrix = readCsvToMap("data_bsas.csv"); // Buenos Aires
```

## Solución de Problemas

### Error: "File not found: pesos.csv"
- Verifica que `pesos.csv` esté en `paradas/src/main/resources/`
- Recompila con `mvn clean package`

### La ejecución es muy lenta
- Reduce `runsPerCombination` de 3 a 1
- Reduce `maxEvaluations` de 60000 a 30000
- Reduce el número de filas en `pesos.csv`

### No se generan las visualizaciones
- Verifica que tengas instaladas las librerías Python: `pip install pandas matplotlib seaborn numpy`
- Verifica que exista un archivo `pareto_results_*.csv`

## Referencias

- **JMetal**: Framework para algoritmos evolutivos multi-objetivo
- **Weighted Sum Method**: Técnica de scalarización para problemas multi-objetivo
- **Pareto Optimality**: Concepto de optimalidad en optimización multi-objetivo

## Contacto y Contribuciones

Para preguntas o mejoras, por favor contacta al equipo de desarrollo.
