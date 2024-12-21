import pandas as pd
import geopandas as gpd
import matplotlib.pyplot as plt

# Path to shapefile and csv
shapefile_path = 'segmentos/Marco2011_SEG_Montevideo_Total.shp'
csv_path = 'paradas/paradas.csv'

# Read the shapefile and csv
gdf = gpd.read_file(shapefile_path)
color_df = pd.read_csv(csv_path)

# Merge the shapefile GeoDataFrame with the value DataFrame on the 'CODSEG' column
gdf = gdf.merge(color_df, on='CODSEG', how='left')

# Reproject to a different CRS (for example, EPSG:4326 for WGS 84)
gdf = gdf.to_crs(epsg=4326)

##################################### ONE-COLORED MAP #####################################

# Create a color column in the GeoDataFrame based on the value column
gdf['color_one'] = gdf['value'].apply(lambda x: 'red' if x > 0 else 'white')

# Plot the shapefile
map_one = gdf.plot(figsize=(10, 10), color=gdf['color_one'], edgecolor='black')
map_one.set_title('Paradas por segmento censal', fontsize=15)
map_one.set_xlabel('Longitude')
map_one.set_ylabel('Latitude')

# Save the plot to a file
plt.savefig('mapa_paradas_1.png', dpi=300)

#################################### MULTI-COLORED MAP ####################################

# Assign colors based on the 'value' values
color_map = {1: 'red', 2: 'blue', 3: 'green'}

# Apply the color map to the 'value' column
gdf['color_mult'] = gdf['value'].map(color_map).fillna('white')

# Plot the shapefile
map_mult = gdf.plot(figsize=(10, 10), color=gdf['color_mult'], edgecolor='black')
map_mult.set_title('Paradas por segmento censal', fontsize=15)
map_mult.set_xlabel('Longitude')
map_mult.set_ylabel('Latitude')

# Save the plot to a file
plt.savefig('mapa_paradas_2.png', dpi=300)

###########################################################################################

# Show the plot
plt.show()