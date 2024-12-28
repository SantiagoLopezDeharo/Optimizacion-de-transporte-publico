import pandas as pd
import geopandas as gpd
from shapely.geometry import Point
import matplotlib.pyplot as plt

# Path to shapefile and csv
shapefile_path = 'segmentos/segmentos_bsas.shp' ######################################################## Cambiar
location_path = 'ubicacion_paradas.csv' ######################################################## Cambiar
odmatrix_path = 'transformed_data.csv' ######################################################## Cambiar

# Read the shapefile and csv
gdf = gpd.read_file(shapefile_path)
df_stops = pd.read_csv(location_path)
df_odmatrix = pd.read_csv(odmatrix_path)

# Convert the bus stop coordinates to a GeoDataFrame
gdf_stops = gpd.GeoDataFrame(df_stops, geometry=[Point(lon, lat) for lon, lat in zip(df_stops['longitud'], df_stops['latitud'])], crs="EPSG:4326")
gdf_stops = gdf_stops.to_crs(gdf.crs)

# Remove unnecessary columns
cols = ['id','geometry']
gdf_stops = gdf_stops[cols]

# Initialize a global dictionary to map stops to their regions
stop_to_region = {}

# Perform the spatial join to find which bus stops are within which segments
joined_gdf = gpd.sjoin(gdf_stops, gdf, how='left', predicate='intersects')

# Iterate through the joined GeoDataFrame and populate the dictionary
for idx, row in joined_gdf.iterrows():
    bus_stop_id = row['id']
    region_id = row['CODSEG']
    
    # Handle cases where no region is found (e.g., NaN region_id)
    if pd.isna(region_id):
        region_id = joined_gdf.iloc[idx]['CODSEG']

    # Store the mapping of bus stop to its region
    stop_to_region[int(bus_stop_id)] = int(region_id)

ODMatrix = {}

# Populate the origin-destination matrix
for row in df_odmatrix.itertuples():

    if not pd.isna(row.origin_id) and not pd.isna(row.destination_id):

        origin_id = int(row.origin_id)
        destination_id = int(row.destination_id)

        if ODMatrix.get(stop_to_region[origin_id]) is None:
            ODMatrix[stop_to_region[origin_id]] = {}    

        if ODMatrix[stop_to_region[origin_id]].get(stop_to_region[destination_id]) is None:
            ODMatrix[stop_to_region[origin_id]][stop_to_region[destination_id]] = 0

        ODMatrix[stop_to_region[origin_id]][stop_to_region[destination_id]] += row.passengers

# Flatten the ODMatrix into a list of rows (origin_region, destination_region, passengers)
data = []

# Iterate through the ODMatrix and extract the data
for origin_region, destination_dict in ODMatrix.items():
    for destination_region, passengers in destination_dict.items():
        data.append([origin_region, destination_region, passengers])

# Create a DataFrame from the flattened data
df_odmatrix = pd.DataFrame(data, columns=['origin_cs', 'destination_cs', 'passengers'])

# Save the DataFrame to a CSV file
df_odmatrix.to_csv('data_bsas.csv', index=False)