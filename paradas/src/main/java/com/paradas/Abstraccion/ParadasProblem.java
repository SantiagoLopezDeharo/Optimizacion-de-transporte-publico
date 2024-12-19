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
            int indexOrigen = segmentToIndex.get(origen );
            int indexDestino= segmentToIndex.get(destino);
    
            if ( solution.variables().get(indexOrigen) > 0 && solution.variables().get(indexDestino) > 0 )
                return 1;
    
            return 0;
        } 
        catch (Exception e) {
            return 0;
        }
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        double f1 = 0;
        for (String origin : matrix.keySet())
            for (String destination : matrix.get(origin).keySet())
                f1 += matrix.get(origin).get(destination) * cubierto(solution, origin, destination); // A revisar
        

        double f2 = 0;
        for ( int v = 0; v < numberOfVariables(); v++ )
            f2 += solution.variables().get(v) == 0 ? 0 : 1;
        
        double f3 = 0;

        for ( int v = 0; v < numberOfVariables(); v++ )
            f3 += solution.variables().get(v) * 2 * (1 - (demanda.get( indexToSegement.get(v) ) / maxDemanda))  ;  // A revisar

        double fitnes = (-1) * 0.4 * f1 + 0.3 * f2 + 0.3 * f3;

        solution.objectives()[0] = fitnes;

        return solution;
    }

    public void printResult(IntegerSolution solution) {
        System.out.println("-----------------");
        System.out.println("Segemnt;value");
        for (int v = 0; v < numberOfVariables(); v++)
            System.out.println(indexToSegement.get(v) + ";" + String.valueOf(solution.variables().get(v)));
        
        System.out.println("-----------------");
        System.out.println("Objective value: " + solution.objectives()[0]);
    }
    
}
