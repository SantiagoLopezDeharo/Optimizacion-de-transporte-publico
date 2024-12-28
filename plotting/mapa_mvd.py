import pandas as pd
import geopandas as gpd
import matplotlib.pyplot as plt

# Path to shapefile and csv
shapefile_path = 'segmentos/segmentos_mvd.shp'
csv_path = 'paradas/paradas_mvd.csv'

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

# Assign shades of green based on the 'value' values
color_map = {
    1: '#a1d99b',  # Light green
    2: '#41ab5d',  # Medium green
    3: '#006d2c'   # Dark green
}

# Apply the color map to the 'value' column
gdf['color_mult'] = gdf['value'].map(color_map).fillna('white')

# Create the figure and axis
fig, ax = plt.subplots(figsize=(10, 10))

# Plot the GeoDataFrame
gdf.plot(ax=ax, color=gdf['color_mult'], edgecolor='black')

# Add a title and labels
ax.set_title('Paradas por segmento censal', fontsize=15)
ax.set_xlabel('Longitude')
ax.set_ylabel('Latitude')

# Create a legend
import matplotlib.patches as mpatches
legend_labels = {
    1: '1',
    2: '2',
    3: '3'
}
patches = [mpatches.Patch(color=color_map[key], label=legend_labels[key]) for key in legend_labels]
ax.legend(handles=patches, loc='lower right', title='Legend')

# Save the plot to a file
plt.savefig('mapa_paradas_2.png', dpi=300)

###########################################################################################

# Show the plot
plt.show()