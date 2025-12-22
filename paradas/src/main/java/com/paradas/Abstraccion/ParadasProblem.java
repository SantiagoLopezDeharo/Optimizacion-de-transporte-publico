package com.paradas.Abstraccion;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

public class ParadasProblem extends AbstractIntegerProblem {

    private final Map<String, Map<String, Integer>> matrix;
    private final Map<String, Integer> demanda;
    private int maxDemanda = 0;
    private final List<String> indexToSegement;
    private final Map<String, Integer> segmentToIndex;

    private final Map<String, Map<String, Integer>> bidirectionalDemand;

    // Normalization bounds for single-objective fitness
    private double maxPossibleCoverage = 0;
    private final int maxPossibleStops;
    private double maxPossibleCost = 0;
    private double minPossibleCost = 0;

    // Weights for linear aggregation (coverage should dominate)
    private double weightCoverage = 0.65; // 65% - Most important
    private double weightStops = 0.20; // 20% - Infrastructure cost
    private double weightCost = 0.15; // 15% - Demand-based cost

    public ParadasProblem(Map<String, Map<String, Integer>> matrix) {
        this(matrix, 0.65, 0.20, 0.15);
    }

    public ParadasProblem(Map<String, Map<String, Integer>> matrix, double weightCoverage, double weightStops, double weightCost) {

        this.matrix = matrix;
        this.weightCoverage = weightCoverage;
        this.weightStops = weightStops;
        this.weightCost = weightCost;

        this.demanda = new HashMap<>();

        this.indexToSegement = new ArrayList<>();
        this.segmentToIndex = new HashMap<>();

        this.bidirectionalDemand = new HashMap<>();

        for (String origin : matrix.keySet()) {
            int demandadx = 0;
            bidirectionalDemand.put(origin, new HashMap<>());

            for (String destination : matrix.get(origin).keySet()) {
                int demandValue = matrix.get(origin).get(destination);

                int demandaDestino = matrix.get(destination) != null && matrix.get(destination).containsKey(origin)
                        ? matrix.get(destination).get(origin)
                        : 0;
                bidirectionalDemand.get(origin).put(destination, demandValue + demandaDestino);

                demandadx += demandValue;

                demanda.put(destination, demanda.getOrDefault(destination, 0) + demandValue);

            }

            demanda.put(origin, demanda.getOrDefault(origin, 0) + demandadx);

        }

        for (String d : demanda.keySet()) {
            maxDemanda = demanda.get(d) != null && maxDemanda < demanda.get(d) ? demanda.get(d) : maxDemanda;

            indexToSegement.add(d);

            segmentToIndex.put(d, indexToSegement.size() - 1);
        }

        int cantVariables = demanda.keySet().toArray().length;
        maxPossibleStops = cantVariables;

        // Compute max possible coverage (sum of all bidirectional demands)
        for (String origin : bidirectionalDemand.keySet()) {
            for (Integer demand : bidirectionalDemand.get(origin).values()) {
                maxPossibleCoverage += demand;
            }
        }

        // Compute theoretical cost bounds
        // Max cost: all stops at low-demand segments (factor ≈ +1)
        // Min cost: all stops at high-demand segments (factor ≈ -1)
        for (String segment : demanda.keySet()) {
            double demandRatio = (demanda.get(segment) * 1.0) / maxDemanda;
            double costFactor = 2 * (1 - demandRatio) - 1;
            if (costFactor > 0) {
                maxPossibleCost += 3 * costFactor; // 3 is max variable value
            } else {
                minPossibleCost += 3 * costFactor;
            }
        }

        numberOfObjectives(1); // Single objective now

        List<Integer> lowerLimit = new ArrayList<>(cantVariables);
        List<Integer> upperLimit = new ArrayList<>(cantVariables);

        for (int i = 0; i < cantVariables; i++) {
            lowerLimit.add(0); // Lower bound is 0
            upperLimit.add(3); // Upper bound is 3
        }

        this.variableBounds(lowerLimit, upperLimit);
    }

    @Override
    public String name() {
        return "ParadasOptimasProblem";
    }

    @Override
    public IntegerSolution createSolution() {
        return new DefaultIntegerSolution(this.variableBounds(), this.numberOfObjectives(), 0);
    }

    // Function to determine if a given origin-destionation bus stop are present on
    // the solution
    private int cubierto(IntegerSolution solution, String origen, String destino) {
        try {
            return solution.variables().get(segmentToIndex.get(origen)) > 0
                    && solution.variables().get(segmentToIndex.get(destino)) > 0 ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        double[] objectives = calculateObjectives(solution);
        double coverage = objectives[0];
        double numStops = objectives[1];
        double cost = objectives[2];

        double normalizedCoverage = (coverage / maxPossibleCoverage);
        double normalizedStops = ((maxPossibleStops - numStops) / maxPossibleStops); // Inverted: fewer stops =
                                                                                            // better
        double normalizedCost = ((maxPossibleCost - cost) / (maxPossibleCost - minPossibleCost)); // Inverted:
                                                                                                         // lower cost =
                                                                                                         // better

        // Linear aggregation with weights (coverage dominates)
        double fitness = weightCoverage * normalizedCoverage
                + weightStops * normalizedStops
                + weightCost * normalizedCost;

        // Negate for minimization (NSGA-II minimizes by default)
        solution.objectives()[0] = -fitness;

        return solution;
    }

    /**
     * Compute the raw objective components for a solution.
     * Thread-safe: does not mutate shared state.
     * @return Array with [coverage, numStops, cost]
     */
    private double[] calculateObjectives(IntegerSolution solution) {
        // Component 1: Passenger coverage (higher is better)
        double coverage = 0;
        for (String origin : matrix.keySet()) {
            for (String destination : matrix.get(origin).keySet()) {
                coverage += bidirectionalDemand.get(origin).get(destination) * cubierto(solution, origin, destination);
            }
        }

        // Component 2: Number of bus stops (fewer is better)
        double numStops = 0;
        for (int v = 0; v < numberOfVariables(); v++) {
            numStops += solution.variables().get(v) == 0 ? 0 : 1;
        }

        // Component 3: Weighted cost (lower is better in low-demand areas)
        double cost = 0;
        for (int v = 0; v < numberOfVariables(); v++) {
            cost += solution.variables().get(v)
                    * (2 * (1 - ((demanda.get(indexToSegement.get(v)) * 1.0) / maxDemanda)) - 1);
        }

        return new double[] { coverage, numStops, cost };
    }

    public void printResult(IntegerSolution solution) {
        System.out.println("-----------------");
        System.out.println("CODSEG,value");
        for (int v = 0; v < numberOfVariables(); v++)
            System.out.println(indexToSegement.get(v) + "," + String.valueOf(solution.variables().get(v)));

        System.out.println("-----------------");
        System.out.println("Fitness value: " + (-solution.objectives()[0]) + " (out of 1)");
    }

    public void saveResultToCSV(List<IntegerSolution> population) {

        IntegerSolution solution = population.get(0);

        for (IntegerSolution i : population)
            if (solution.objectives()[0] < i.objectives()[0])
                solution = i;

        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "results_" + dateTime + ".csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("CODSEG,value\n");

            for (int v = 0; v < numberOfVariables(); v++) {
                writer.write(indexToSegement.get(v) + "," + solution.variables().get(v) + "\n");
            }

            System.out.println("Results saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("An error occurred while saving the results: " + e.getMessage());
        }
    }

    /**
     * Get the original objective values for a solution (before aggregation)
     * @param solution The solution to get objectives for
     * @return Array with [coverage, numStops, cost]
     */
    public double[] getOriginalObjectives(IntegerSolution solution) {
        return calculateObjectives(solution);
    }

    /**
     * Get the best solution from population based on aggregated fitness
     */
    public IntegerSolution getBestSolution(List<IntegerSolution> population) {
        IntegerSolution best = population.get(0);
        for (IntegerSolution solution : population) {
            if (solution.objectives()[0] < best.objectives()[0]) {
                best = solution;
            }
        }
        return best;
    }

}
