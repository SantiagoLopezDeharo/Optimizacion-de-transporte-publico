package com.paradas;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.TwoPointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import com.paradas.Abstraccion.ParadasProblem;
import com.paradas.utils.CustomAlgorithm;
import com.paradas.utils.CustomAlgorithmBuilder;
import com.paradas.utils.ParallelEvaluator;

import tech.tablesaw.io.csv.CsvReader;

public class Main extends AbstractAlgorithmRunner {

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

    /**
     * Save multi-objective Pareto approximation to CSV
     */
    public static void saveParetoResultsMulti(List<IntegerSolution> solutions, ParadasProblem problem,
            String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("coverage,num_stops,cost,norm_coverage,norm_stops_inv,norm_cost_inv\n");

            for (IntegerSolution solution : solutions) {
                double[] raw = problem.getOriginalObjectives(solution);
                double[] norm = problem.getNormalizedObjectives(solution);
                writer.write(String.format("%.2f,%.2f,%.6f,%.6f,%.6f,%.6f\n",
                        raw[0], raw[1], raw[2],
                        norm[0], norm[1], norm[2]));
            }

            System.out.println("Multi-objective Pareto results saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving Pareto results: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Read the data matrix
        Map<String, Map<String, Integer>> matrix = readCsvToMap("data_mvd.csv");

        // Algorithm configuration
        int populationSize = 200;
        int maxEvaluations = 45000;
        double crossoverProbability = 0.9;
        double mutationProbability = 0.06;
        int mutationDistributionIndex = 6;

        // Generate timestamp for output files
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        System.out.println("\nRunning multi-objective optimization (3 objectives, no weights)...");

        ParadasProblem problem = new ParadasProblem(matrix);

        // Configure operators
        @SuppressWarnings({ "rawtypes", "unchecked" })
        CrossoverOperator<IntegerSolution> crossover = new TwoPointCrossover(crossoverProbability);
        MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(
                mutationProbability, mutationDistributionIndex);
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>(
                new RankingAndCrowdingDistanceComparator<>());

        // Create and run algorithm
        CustomAlgorithm<IntegerSolution> algorithm = new CustomAlgorithmBuilder<>(
                problem, crossover, mutation, populationSize)
                .setMaxEvaluations(maxEvaluations)
                .setSelectionOperator(selection)
                .setSolutionListEvaluator(new ParallelEvaluator<>())
                .build();

        algorithm.run();
        List<IntegerSolution> paretoSolutions = algorithm.result();

        // Save Pareto approximation to CSV
        String paretoFileName = "pareto_results_multi_" + timestamp + ".csv";
        saveParetoResultsMulti(paretoSolutions, problem, paretoFileName);

        System.out.println("\n========================================");
        System.out.println("Multi-objective Pareto approximation completed!");
        System.out.println("Non-dominated solutions: " + paretoSolutions.size());
        System.out.println("Results saved to: " + paretoFileName);
        System.out.println("========================================");

        algorithm.saveFitnessToCsv();
    }
}