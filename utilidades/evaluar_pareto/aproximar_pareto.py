import pandas as pd
import numpy as np

# Function to check if a solution dominates another
def dominates(solution_a, solution_b):
    # solution_a dominates solution_b if it is better in all objectives (or equal in some and better in others)
    return all(a <= b for a, b in zip(solution_a, solution_b)) and any(a < b for a, b in zip(solution_a, solution_b))


# Function to get non-dominated solutions
def get_non_dominated_solutions(solutions):
    non_dominated = []
    for i, sol_a in enumerate(solutions):
        dominated = False
        for j, sol_b in enumerate(solutions):
            if i != j and dominates(sol_b, sol_a):  # If sol_b dominates sol_a
                print(str(sol_b) + " dominates " + str(sol_a))
                dominated = True
                break
        if not dominated:
            print(str(sol_a) + " not dominated")
            non_dominated.append(sol_a)
    return np.array(non_dominated)


# Read all Pareto front files and combine them
def combine_pareto_fronts(file_list):
    all_solutions = []
    
    # Read each file and add the solutions to the list
    for file in file_list:
        df = pd.read_csv(file, header=None)
        all_solutions.append(df.values)
    
    # Concatenate all the solutions from all files
    all_solutions = np.concatenate(all_solutions, axis=0)
    
    return all_solutions


pareto_front_files = []

for i in range(810):
    pareto_front_files.append(f'datos_pittsburgh/FUN{i+1}.csv')

combined_pareto = combine_pareto_fronts(pareto_front_files)

non_dominated = get_non_dominated_solutions(combined_pareto)

# Create a DataFrame from the non-dominated solutions
non_dominated_df = pd.DataFrame(non_dominated)

headerPareto = ['f1', 'f2', 'f3']

# Save the combined Pareto front to a new CSV file
non_dominated_df.to_csv('approximated_pareto_front.csv', index=False, header=headerPareto)