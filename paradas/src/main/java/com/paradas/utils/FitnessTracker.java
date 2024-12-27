package com.paradas.utils;

import org.uma.jmetal.solution.Solution;

public class FitnessTracker<S extends Solution<?>>  {
    private int generation = 0;

    public void update(S data) {
        generation++;

        System.out.printf("Generation %d: Best = ( %.4f, %.4f, %.4f )", 
            generation, data.objectives()[0], data.objectives()[1], data.objectives()[2]);
    }
}
