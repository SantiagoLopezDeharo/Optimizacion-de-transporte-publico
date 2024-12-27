package com.paradas.utils;

import java.util.List;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.observable.Observable;
import org.uma.jmetal.util.observer.Observer;

public class FitnessTracker<S extends Solution<?>> implements Observer<List<S>> {
    private int generation = 0;

    @Override
    public void update(Observable<List<S>> observable, List<S> data) {
        generation++;

        // Log the best, average, and worst fitness for the population
        double bestFitness = data.stream()
            .mapToDouble(s -> s.objectives()[0]) // Assuming you're tracking the first objective
            .min()
            .orElse(Double.NaN);

        double averageFitness = data.stream()
            .mapToDouble(s -> s.objectives()[0])
            .average()
            .orElse(Double.NaN);

        double worstFitness = data.stream()
            .mapToDouble(s -> s.objectives()[0])
            .max()
            .orElse(Double.NaN);

        System.out.printf("Generation %d: Best = %.4f, Average = %.4f, Worst = %.4f%n", 
                          generation, bestFitness, averageFitness, worstFitness);
    }
}
