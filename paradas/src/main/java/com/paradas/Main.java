package com.paradas;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.TwoPointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import com.paradas.Abstraccion.ParadasProblem;
import com.paradas.utils.CustomAlgorithm;
import com.paradas.utils.CustomAlgorithmBuilder;
import com.paradas.utils.ParallelEvaluator;

import tech.tablesaw.io.csv.CsvReader;

public class Main extends AbstractAlgorithmRunner {

    // Class to store weight combinations
    static class WeightCombination {
        double f1; // Coverage weight
        double f2; // Stops weight
        double f3; // Cost weight

        public WeightCombination(double f1, double f2, double f3) {
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
        }

        @Override
        public String toString() {
            return String.format("w1=%.4f, w2=%.4f, w3=%.4f", f1, f2, f3);
        }
    }

    // Class to store Pareto results
    static class ParetoResult {
        WeightCombination weights;
        double coverage;
        double numStops;
        double cost;
        double fitness;

        public ParetoResult(WeightCombination weights, double coverage, double numStops, double cost, double fitness) {
            this.weights = weights;
            this.coverage = coverage;
            this.numStops = numStops;
            this.cost = cost;
            this.fitness = fitness;
        }
    }

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
     * Read weight combinations from pesos.csv
     */
    public static List<WeightCombination> readWeights(String fileName) {
        List<WeightCombination> weights = new ArrayList<>();

        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found in resources: " + fileName);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                // Skip the header line
                br.readLine();

                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");

                    if (values.length >= 3) {
                        double f1 = Double.parseDouble(values[0].trim());
                        double f2 = Double.parseDouble(values[1].trim());
                        double f3 = Double.parseDouble(values[2].trim());

                        weights.add(new WeightCombination(f1, f2, f3));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return weights;
    }

    /**
     * Save Pareto results to CSV
     */
    public static void saveParetoResults(List<ParetoResult> results, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("w_coverage,w_stops,w_cost,coverage,num_stops,cost,fitness\n");

            for (ParetoResult result : results) {
                writer.write(String.format("%.4f,%.4f,%.4f,%.2f,%.2f,%.2f,%.6f\n",
                        result.weights.f1, result.weights.f2, result.weights.f3,
                        result.coverage, result.numStops, result.cost, result.fitness));
            }

            System.out.println("Pareto results saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving Pareto results: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void printFinalSolutionSet(List<? extends Solution<?>> population) {
        new SolutionListOutput(population)
                .setVarFileOutputContext(
                        new DefaultFileOutputContext("VAR" + JMetalRandom.getInstance().getSeed() + ".csv", ","))
                .setFunFileOutputContext(
                        new DefaultFileOutputContext("FUN" + JMetalRandom.getInstance().getSeed() + ".csv", ","))
                .print();

        JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
        JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
    }

    public static void main(String[] args) {
        // Read the data matrix
        Map<String, Map<String, Integer>> matrix = readCsvToMap("data_mvd.csv");

        // Read weight combinations from pesos.csv
        List<WeightCombination> weightCombinations = readWeights("pesos.csv");
        System.out.println("Loaded " + weightCombinations.size() + " weight combinations");

        // List to store all Pareto results
        List<ParetoResult> paretoResults = new ArrayList<>();

        // Number of runs per weight combination (for statistical stability)
        int runsPerCombination = 3;

        // Algorithm configuration
        int populationSize = 300;
        int maxEvaluations = 35000;
        double crossoverProbability = 0.9;
        double mutationProbability = 0.06;
        int mutationDistributionIndex = 6;

        // Get available processors for parallel evaluation
        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + threads + " threads for parallel evaluation");

        // Generate timestamp for output files
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        // Iterate over each weight combination
        int combinationCount = 0;
        for (WeightCombination weights : weightCombinations) {
            combinationCount++;
            System.out.println(String.format("\\n[%d/%d] Running with weights: %s",
                    combinationCount, weightCombinations.size(), weights));

            // Store best result across all runs for this weight combination
            ParetoResult bestResult = null;
            double bestFitness = Double.NEGATIVE_INFINITY;

            // Run multiple times for statistical stability
            for (int run = 0; run < runsPerCombination; run++) {
                System.out.println(String.format("  Run %d/%d...", run + 1, runsPerCombination));

                // Create problem with current weights
                ParadasProblem problem = new ParadasProblem(matrix, weights.f1, weights.f2, weights.f3);

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
                List<IntegerSolution> population = algorithm.result();

                // Get best solution
                IntegerSolution bestSolution = problem.getBestSolution(population);
                double[] objectives = problem.getOriginalObjectives(bestSolution);
                double fitness = -bestSolution.objectives()[0]; // Negate back to get positive fitness

                // Check if this is the best run for this weight combination
                if (fitness > bestFitness) {
                    bestFitness = fitness;
                    bestResult = new ParetoResult(
                            weights,
                            objectives[0], // coverage
                            objectives[1], // numStops
                            objectives[2], // cost
                            fitness);
                }
            }

            // Store the best result for this weight combination
            if (bestResult != null) {
                paretoResults.add(bestResult);
                System.out.println(String.format("  Best result: Coverage=%.2f, Stops=%.2f, Cost=%.2f, Fitness=%.6f",
                        bestResult.coverage, bestResult.numStops, bestResult.cost, bestResult.fitness));
            }
        }

        // Save Pareto results to CSV
        String paretoFileName = "pareto_results_" + timestamp + ".csv";
        saveParetoResults(paretoResults, paretoFileName);

        System.out.println("\\n========================================");
        System.out.println("Pareto front approximation completed!");
        System.out.println("Total weight combinations: " + weightCombinations.size());
        System.out.println("Runs per combination: " + runsPerCombination);
        System.out.println("Total algorithm runs: " + (weightCombinations.size() * runsPerCombination));
        System.out.println("Results saved to: " + paretoFileName);
        System.out.println("========================================");
    }
}