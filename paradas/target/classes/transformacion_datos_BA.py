import csv

# Ruta al archivo CSV
csv_path = 'BA_sin_transformar.csv'

# Crear el diccionario para almacenar los datos
data_map = {}

# Leer el archivo CSV
with open(csv_path, mode='r', encoding='utf-8') as csv_file:
    reader = csv.DictReader(csv_file)
    
    # Inicializar las claves del diccionario con las columnas del CSV
    for column in reader.fieldnames:
        data_map[column] = []
    
    # Rellenar el diccionario con los valores de cada columna
    for row in reader:
        for column in reader.fieldnames:
            data_map[column].append(row[column])


transformed_csv = {}

for i in range(len(data_map['parada_id_o'])):
    transformed_csv[ data_map['parada_id_o'] [ i ] ] = {}

for i in range(len(data_map['parada_id_o'])):
    transformed_csv[ data_map['parada_id_o'] [ i ]] [data_map['parada_id_d'][i]] = 0

for i in range(len(data_map['parada_id_o'])):
    transformed_csv[ data_map['parada_id_o'] [ i ] ] [ data_map['parada_id_d'] [ i ] ] += 1

output_csv_path = 'transformed_data.csv'

with open(output_csv_path, mode='w', encoding='utf-8', newline='') as output_csv:
    writer = csv.writer(output_csv)
    
    # Escribir la cabecera
    writer.writerow(['origin_cs', 'destination_cs', 'passengers'])
    
    # Escribir los datos transformados
    for parada_id_o, destinations in transformed_csv.items():
        for parada_id_d, value in destinations.items():
            writer.writerow([parada_id_o, parada_id_d, value])

print(f"Archivo transformado guardado en: {output_csv_path}")

