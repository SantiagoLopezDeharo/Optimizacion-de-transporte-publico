package com.paradas.utils;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * High-performance parallel evaluator using Java's ExecutorService
 * Evaluates multiple solutions concurrently for faster processing
 * 
 * @param <S> Solution type
 */
public class ParallelEvaluator<S extends Solution<?>> implements SolutionListEvaluator<S> {

    private final ExecutorService executor;

    /**
     * Create a parallel evaluator with specified thread count
     * 
     * @param threads Number of threads to use (typically
     *                Runtime.getRuntime().availableProcessors())
     */
    public ParallelEvaluator(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    @Override
    public List<S> evaluate(List<S> population, Problem<S> problem) {
        population.parallelStream().forEach(problem::evaluate);

        return population;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
