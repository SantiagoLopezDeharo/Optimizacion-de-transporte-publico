package com.paradas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.TwoPointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import com.paradas.Abstraccion.ParadasProblem;

import tech.tablesaw.io.csv.CsvReader;

public class Main {
    @SuppressWarnings("CallToPrintStackTrace")
    public static Map<String, Map<String, Integer>> readCsvToMap(String fileName) {
        Map<String, Map<String, Integer>> dataMap = new HashMap<>();

        // Load the file from the classpath
        try (InputStream inputStream = CsvReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found in resources: " + fileName);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                // Skip the header line
                br.readLine();

                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");

                    if (values.length == 3) {
                        String origin = values[0];
                        String destination = values[1];
                        int passengers = Integer.parseInt(values[2]);

                        // Add data to the map
                        dataMap.computeIfAbsent(origin, k -> new HashMap<>())
                               .put(destination, passengers);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return dataMap;
    }
    public static void main(String[] args) {
        Map<String, Map<String, Integer>> matrix = readCsvToMap("data.csv");

        // Step 1: Create the problem
        Problem<IntegerSolution> problem = new ParadasProblem(matrix);

        // Step 2: Configure the operators
        @SuppressWarnings({ "rawtypes", "unchecked" })
        CrossoverOperator<IntegerSolution> crossover = new TwoPointCrossover(0.75); // 75% crossover probability
        MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(0.01, 9); // 0.01% mutation probability
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>();

        // Step 3: Create the Genetic Algorithm instance
        Algorithm<IntegerSolution> algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
                .setPopulationSize(100)
                .setMaxEvaluations(2000)
                .setSelectionOperator(selection)
                .setSolutionListEvaluator(new SequentialSolutionListEvaluator<>())
                .build();

        // Step 4: Run the algorithm
        algorithm.run();

        // Step 5: Get and print the solution
        IntegerSolution solution = algorithm.result();
        System.out.println("Best solution: " + solution.variables());
        System.out.println("Objective value: " + solution.objectives()[0]);
    }
}