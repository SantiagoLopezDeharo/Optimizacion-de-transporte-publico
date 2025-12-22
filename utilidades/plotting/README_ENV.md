Windows setup instructions

Recommended (Conda - easiest for geopandas and deps):
1. Install Anaconda or Miniconda if you don't have it: https://docs.conda.io/en/latest/miniconda.html
2. Open Anaconda Prompt and run:

   conda create -n paradas_env python=3.11 geopandas matplotlib pandas -y
   conda activate paradas_env

3. Run the plotting script from project root or from this folder. Example:

   python venv_test.py
   python mapa_bsas.py


Alternative (venv + pip):
1. From a PowerShell prompt in this folder, run:

   python -m venv .venv
   .venv\Scripts\python -m pip install --upgrade pip setuptools wheel
   .venv\Scripts\python -m pip install -r requirements.txt

2. If `geopandas` pip install fails on Windows, use the Conda method above.

Files added:
- requirements.txt : pip list of packages
- venv_test.py     : quick import/version test

