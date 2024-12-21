package com.paradas.Abstraccion;

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
    private double demandaTotal = 0;

    public ParadasProblem(Map<String, Map<String, Integer>> matrix) {

        this.matrix = matrix;

        this.demanda = new HashMap<>();

        this.indexToSegement = new ArrayList<>();
        this.segmentToIndex = new HashMap<>();

        for (String origin : matrix.keySet())
        {
            int demandadx = 0;

            indexToSegement.add(origin);
            segmentToIndex.put(origin, indexToSegement.size() - 1);

            for (String destination : matrix.get(origin).keySet())
                demandadx += matrix.get(origin).get(destination);

            demanda.put(origin, demandadx);
            demandaTotal += demandadx;

            maxDemanda = demandadx > maxDemanda ? demandadx : maxDemanda;
        }

        int cantVariables = demanda.keySet().toArray().length;
        
        numberOfObjectives(1);

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
                f1 += matrix.get(origin).get(destination) * cubierto(solution, origin, destination); // A revisar
        
        f1 = f1 / demandaTotal; // We noramlice the coverage into all the demand of the problem to see what porcentaje of the demand is being coverages

        double f2 = 0;
        // Minimize amount of bus stops ( less bus stops would reduce the amount of time for the bus to go from A to B )
        for ( int v = 0; v < numberOfVariables(); v++ )
            f2 += solution.variables().get(v) == 0 ? 0 : 1;
        
        f2 = f2 / this.numberOfVariables(); // We normalice the porcentaje of segments with bus stops
        
        double f3 = 0;
        // Minimize cost
        for ( int v = 0; v < numberOfVariables(); v++ )
            f3 += solution.variables().get(v) * 2 * (1 - (demanda.get( indexToSegement.get(v) ) / maxDemanda))  ;  // A revisar

        f3 = f3 / (3 * this.numberOfVariables()); // We normalice the cost to the porcentaje of the maximum posible cost

        double fitnes = 100 * ( (-1) * 0.5 * f1 + 0.25 * f2 + 0.25 * f3 ); // we make a weighted-fitness with all this factors

        solution.objectives()[0] = fitnes;

        return solution;
    }

    public void printResult(IntegerSolution solution) {
        System.out.println("-----------------");
        System.out.println("CODSEG,value");
        for (int v = 0; v < numberOfVariables(); v++)
            System.out.println(indexToSegement.get(v) + "," + String.valueOf(solution.variables().get(v)));
        
        System.out.println("-----------------");
        System.out.println("Objective value: " + solution.objectives()[0]);
    }
    
}
