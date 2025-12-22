#!/bin/bash
# Script para ejecutar el análisis completo del frente de Pareto
# Unix/Linux/Mac Bash Script

echo "========================================"
echo "ANALISIS DEL FRENTE DE PARETO"
echo "Optimizacion de Transporte Publico"
echo "========================================"
echo ""

# Verificar que estamos en el directorio correcto
if [ ! -f "paradas/pom.xml" ]; then
    echo "Error: No se encuentra paradas/pom.xml"
    echo "Por favor ejecuta este script desde el directorio raiz del proyecto"
    exit 1
fi

echo "[1/3] Compilando el proyecto..."
echo ""
cd paradas
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo ""
    echo "Error: Fallo la compilacion"
    cd ..
    exit 1
fi
cd ..

echo ""
echo "========================================"
echo "[2/3] Ejecutando el algoritmo genetico..."
echo ""
echo "ATENCION: Este proceso puede tardar varias horas"
echo "dependiendo del numero de combinaciones de pesos."
echo ""
read -p "Deseas continuar? (S/N): " continuar
if [ "$continuar" != "S" ] && [ "$continuar" != "s" ]; then
    echo "Ejecucion cancelada por el usuario"
    exit 0
fi

echo ""
cd paradas
mvn exec:java -Dexec.mainClass="com.paradas.Main"
if [ $? -ne 0 ]; then
    echo ""
    echo "Error: Fallo la ejecucion del algoritmo"
    cd ..
    exit 1
fi
cd ..

echo ""
echo "========================================"
echo "[3/3] Generando visualizaciones..."
echo ""

# Verificar si Python esta instalado
if ! command -v python3 &> /dev/null; then
    echo "Error: Python3 no esta instalado"
    echo "Por favor instala Python3 y las librerias requeridas:"
    echo "  pip3 install pandas matplotlib seaborn numpy"
    exit 1
fi

python3 visualize_pareto.py
if [ $? -ne 0 ]; then
    echo ""
    echo "Error: Fallo la generacion de visualizaciones"
    echo "Verifica que tengas instaladas las librerias necesarias:"
    echo "  pip3 install pandas matplotlib seaborn numpy"
    exit 1
fi

echo ""
echo "========================================"
echo "PROCESO COMPLETADO EXITOSAMENTE"
echo "========================================"
echo ""
echo "Los resultados se encuentran en:"
echo "  - pareto_results_*.csv"
echo "  - pareto_analysis/*.png"
echo "  - pareto_analysis/pareto_statistics.txt"
echo ""
