package com.paradas.utils;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;

/**
 * High-performance parallel evaluator using Java's ExecutorService
 * Evaluates multiple solutions concurrently for faster processing
 * 
 * @param <S> Solution type
 */
public class ParallelEvaluator<S extends Solution<?>> implements SolutionListEvaluator<S> {
    @Override
    public List<S> evaluate(List<S> population, Problem<S> problem) {
        population.parallelStream().forEach(problem::evaluate);

        return population;
    }

    @Override
    public void shutdown() {
    }
}
