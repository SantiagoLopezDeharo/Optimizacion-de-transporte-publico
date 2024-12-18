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

    public ParadasProblem(int cantidadDeSegmentos, Map<String, Map<String, Integer>> matrix) {

        this.matrix = matrix;

        this.demanda = new HashMap<>();

        for (String origin : matrix.keySet())
        {
            int demandadx = 0;

            for (String destination : matrix.get(origin).keySet())
                demandadx += matrix.get(origin).get(destination);

            demanda.put(origin, demandadx);
        }

        int cantVariables = cantidadDeSegmentos;
        
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

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        int f1 = 0;
        for (String origin : matrix.keySet())
            for (String destination : matrix.get(origin).keySet())
                f1 += matrix.get(origin).get(destination); // TO DO 
        

        int f2 = 0;
        for (int v = 0; v < numberOfVariables(); v+= 2)
            f2 += solution.variables().get(v) == 0 ? 0 : 1;
        
        int f3 = 0;

        for (int v = 0; v < numberOfVariables(); v+= 2)
            f3 += solution.variables().get(v)  ;  // TO DO

        double fitnes = (-1) * 0.4 * f1 + 0.3 * f2 + 0.3 * f3;

        solution.objectives()[0] = fitnes;

        return solution;
    }
    
}
