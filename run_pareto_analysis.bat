@echo off
REM Script para ejecutar el análisis completo del frente de Pareto
REM Windows Batch Script

echo ========================================
echo ANALISIS DEL FRENTE DE PARETO
echo Optimizacion de Transporte Publico
echo ========================================
echo.

REM Verificar que estamos en el directorio correcto
if not exist "paradas\pom.xml" (
    echo Error: No se encuentra paradas\pom.xml
    echo Por favor ejecuta este script desde el directorio raiz del proyecto
    pause
    exit /b 1
)

echo [1/3] Compilando el proyecto...
echo.
cd paradas
call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo Error: Fallo la compilacion
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo ========================================
echo [2/3] Ejecutando el algoritmo genetico...
echo.
echo ATENCION: Este proceso puede tardar varias horas
echo dependiendo del numero de combinaciones de pesos.
echo.
set /p continuar="Deseas continuar? (S/N): "
if /i not "%continuar%"=="S" (
    echo Ejecucion cancelada por el usuario
    pause
    exit /b 0
)

echo.
cd paradas
call mvn exec:java -Dexec.mainClass="com.paradas.Main"
if errorlevel 1 (
    echo.
    echo Error: Fallo la ejecucion del algoritmo
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo ========================================
echo [3/3] Generando visualizaciones...
echo.

REM Verificar si Python esta instalado
python --version >nul 2>&1
if errorlevel 1 (
    echo Error: Python no esta instalado o no esta en el PATH
    echo Por favor instala Python y las librerias requeridas:
    echo   pip install pandas matplotlib seaborn numpy
    pause
    exit /b 1
)

python visualize_pareto.py
if errorlevel 1 (
    echo.
    echo Error: Fallo la generacion de visualizaciones
    echo Verifica que tengas instaladas las librerias necesarias:
    echo   pip install pandas matplotlib seaborn numpy
    pause
    exit /b 1
)

echo.
echo ========================================
echo PROCESO COMPLETADO EXITOSAMENTE
echo ========================================
echo.
echo Los resultados se encuentran en:
echo   - pareto_results_*.csv
echo   - pareto_analysis\*.png
echo   - pareto_analysis\pareto_statistics.txt
echo.
pause
