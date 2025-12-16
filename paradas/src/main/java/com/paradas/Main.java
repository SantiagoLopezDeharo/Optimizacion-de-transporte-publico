package com.paradas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.TwoPointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
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
        Map<String, Map<String, Integer>> matrix = readCsvToMap("data_mvd.csv");

        // Step 1: Create the problem
        Problem<IntegerSolution> problem = new ParadasProblem(matrix);

        // Step 2: Configure the operators
        @SuppressWarnings({ "rawtypes", "unchecked" })
        CrossoverOperator<IntegerSolution> crossover = new TwoPointCrossover(0.8); // 80% crossover probability
        MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(0.1, 5); // 1% mutation probability
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>(
                new RankingAndCrowdingDistanceComparator<>());
        int populationSize = 200;

        // Get available processors for parallel evaluation
        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + threads + " threads for parallel evaluation");

        // Step 3: Create the Genetic Algorithm instance with parallel evaluator
        CustomAlgorithm<IntegerSolution> algorithm = new CustomAlgorithmBuilder<>(problem, crossover, mutation,
                populationSize)
                .setMaxEvaluations(60000)
                .setSelectionOperator(selection)
                .setSolutionListEvaluator(new ParallelEvaluator<>())
                .build();

        algorithm.run();
        List<IntegerSolution> population = algorithm.result();
        printFinalSolutionSet(population);

        ((ParadasProblem) problem).saveResultToCSV(population);

        ((CustomAlgorithm<IntegerSolution>) algorithm).saveFitnessToCsv();

    }
}