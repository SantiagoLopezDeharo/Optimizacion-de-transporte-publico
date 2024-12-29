import pandas as pd
import matplotlib.pyplot as plt

# Read the CSV file
file_path = "fitness_evolution_2.csv"  # Replace with the path to your CSV file
data = pd.read_csv(file_path)

# Extract generations and function values
generations = data["generation"]

# Plot each f against generations
for column in ["f1", "f2", "f3"]:
    plt.figure(figsize=(8, 6))
    plt.plot(generations, data[column], marker='o', label=column)
    plt.title(f"Plot of {column} vs Generation")
    plt.xlabel("Generation")
    plt.ylabel(column)
    plt.grid(True)
    plt.legend()
    plt.savefig(f"{column}_vs_generation.png")  # Save the plot as an image

plt.show()