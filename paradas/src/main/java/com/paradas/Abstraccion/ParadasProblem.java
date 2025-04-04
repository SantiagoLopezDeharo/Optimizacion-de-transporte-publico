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
    private final List<String> indexToSegement; // We keep a list to know wich position in the individuals represent which segment
    private final Map<String, Integer> segmentToIndex;

    public ParadasProblem(Map<String, Map<String, Integer>> matrix) {

        this.matrix = matrix;

        this.demanda = new HashMap<>();

        this.indexToSegement = new ArrayList<>();
        this.segmentToIndex = new HashMap<>();

        for (String origin : matrix.keySet())
        {
            int demandadx = 0;

            for (String destination : matrix.get(origin).keySet())
            {
                int demandValue = matrix.get(origin).get(destination);

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
        
        numberOfObjectives(3);

        // Set the lower and upper bounds for each variable (0 to 3 for each)
        List<Integer> lowerLimit = new ArrayList<>(cantVariables);
        List<Integer> upperLimit = new ArrayList<>(cantVariables);

        for (int i = 0; i < cantVariables; i++) {
            lowerLimit.add(0);  // Lower bound is 0
            upperLimit.add(3);  // Upper bound is 3
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

    // Function to determine if a given origin-destionation bus stop are present on the solution
    private int cubierto(IntegerSolution solution, String origen, String destino) {
        try {
            return solution.variables().get(segmentToIndex.get(origen )) > 0 && solution.variables().get(segmentToIndex.get(destino)) > 0 ? 1 : 0;
        }
        catch (Exception e) {
            return 0;
        }
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        double f1 = 0;
        // Maximize coverage of the demand of each route
        for (String origin : matrix.keySet())
            for (String destination : matrix.get(origin).keySet())
            {
                int demandaDestino = matrix.get(destination) != null && matrix.get(destination).keySet().contains(origin) ? matrix.get(destination).get(origin) : 0;
                f1 += ( matrix.get(origin).get(destination) + demandaDestino )* cubierto(solution, origin, destination); // A revisar
            }
        
        double f2 = 0;
        // Minimize amount of bus stops ( less bus stops would reduce the amount of time for the bus to go from A to B )
        for ( int v = 0; v < numberOfVariables(); v++ )
            f2 += solution.variables().get(v) == 0 ? 0 : 1;
        
        
        double f3 = 0;
        // Minimize cost
        for ( int v = 0; v < numberOfVariables(); v++ )
            f3 += solution.variables().get(v) * ( 2 * (1 - ( ( demanda.get( indexToSegement.get(v) ) * 1.0 ) / maxDemanda)) - 1 )  ;  // A revisar


        solution.objectives()[0] = - f1;
        solution.objectives()[1] = f2;
        solution.objectives()[2] = f3;

        return solution;
    }

    public void printResult(IntegerSolution solution) {
        System.out.println("-----------------");
        System.out.println("CODSEG,value");
        for (int v = 0; v < numberOfVariables(); v++)
            System.out.println(indexToSegement.get(v) + "," + String.valueOf(solution.variables().get(v)));
        
        System.out.println("-----------------");
        System.out.println("Objective value: ( " + solution.objectives()[0] + ", " + solution.objectives()[1] + ", " + solution.objectives()[2] + " )");
    }

    public void saveResultToCSV(List<IntegerSolution> population) {

    IntegerSolution solution = population.get(0);

    for (int i = 0; i < population.size(); i++)
        if (solution.objectives()[0] > population.get(i).objectives()[0])
            solution = population.get(i);
    
    String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    String fileName = "results_" + dateTime + "_buenos_aires.csv";

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
        
}
