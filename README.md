
<img height="120" src="https://i0.wp.com/parlamentodata.com/wp-content/uploads/2020/05/logo-udelar.png?ssl=1">

# Proyecto final de Algoritmos Evolutivos

Este repo contiene el proyecto final para el curso de 2024 de algoritmos evolutivos en la fing.

El proyecto consiste en hacer uso de un algoritmo genetico para optimizar el posicionamiento de las paradas de transporte público en Montevideo.


## Tech Stack

| Java | JMetal | Maven | 
| ------ | ------ | ---------- |
|<img height="60" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/java.png"> |<img height="60" src="https://jmetal.sourceforge.net/images/jMetalLogo.png">|<img height="60" src="https://static-00.iconduck.com/assets.00/file-type-maven-icon-766x1024-86phvtjn.png">|


## Enconding del problema

Para el problema se representara las entidades como tiras de numeros entre 0 y 3, con el fin de determinar si hay que poner una parada y en el caso de que si (> 0) de que calidad debe ser la parada.

Cada numero representa si hay que colocar la parada en una zona de sezgo en especifica en base a cuanta gente transita por dicha zona.


## Authors

- [@Santiago Gestal](https://github.com/SantiGestal)
- [@Santiago Lopez de haro](https://github.com/SantiagoLopezDeharo)
## Documentación

[Documentación](https://github.com/SantiagoLopezDeharo/Optimizacion-de-transporte-publico/blob/main/Informe.pdf)

## 🆕 Análisis del Frente de Pareto

Este proyecto ahora incluye funcionalidad para aproximar el frente de Pareto mediante la ejecución del algoritmo genético con múltiples combinaciones de pesos.

### Características

- ✅ **Ejecución automática** con 125 combinaciones de pesos (configurable)
- ✅ **Múltiples ejecuciones** por combinación para estabilidad estadística
- ✅ **Almacenamiento de objetivos originales** (cobertura, paradas, costo)
- ✅ **Visualizaciones profesionales** del frente de Pareto en 3D y 2D
- ✅ **Análisis de métricas** de desempeño del algoritmo
- ✅ **Scripts automatizados** para ejecutar todo el proceso

### Inicio Rápido

#### Windows
```batch
run_pareto_analysis.bat
```

#### Linux/Mac
```bash
chmod +x run_pareto_analysis.sh
./run_pareto_analysis.sh
```

#### Manual
```bash
# 1. Compilar
cd paradas
mvn clean package

# 2. Ejecutar algoritmo
mvn exec:java -Dexec.mainClass="com.paradas.Main"

# 3. Visualizar resultados
cd ..
pip install -r requirements.txt
python visualize_pareto.py
```

### Resultados Generados

El análisis genera:
- 📊 `pareto_results_*.csv` - Datos de todas las ejecuciones
- 📈 `pareto_analysis/pareto_3d.png` - Visualización 3D del frente de Pareto
- 📉 `pareto_analysis/pareto_2d_projections.png` - Proyecciones 2D
- 🎨 `pareto_analysis/weight_influence.png` - Influencia de pesos
- 🔥 `pareto_analysis/fitness_heatmap.png` - Mapas de calor
- 📝 `pareto_analysis/pareto_statistics.txt` - Estadísticas completas

### Documentación Adicional

Para más información sobre el análisis del frente de Pareto:
- 📖 [PARETO_README.md](PARETO_README.md) - Guía completa de uso
- 📋 [CAMBIOS_REALIZADOS.md](CAMBIOS_REALIZADOS.md) - Resumen de cambios implementados

### Dependencias Python

```bash
pip install -r requirements.txt
```

Incluye: pandas, matplotlib, seaborn, numpy

