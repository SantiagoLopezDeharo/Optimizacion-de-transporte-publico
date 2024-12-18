package com.paradas;

import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.TwoPointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import com.paradas.Abstraccion.ParadasProblem;

public class Main {
    public static void main(String[] args) {
        // Step 1: Create the problem
        Problem<IntegerSolution> problem = new ParadasProblem();

        // Step 2: Configure the operators
        @SuppressWarnings({ "rawtypes", "unchecked" })
        CrossoverOperator<IntegerSolution> crossover = new TwoPointCrossover(0.75); // 75% crossover probability
        MutationOperator<IntegerSolution> mutation = new IntegerPolynomialMutation(0.01, 9); // 0.01% mutation probability
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<>();

        // Step 3: Create the Genetic Algorithm instance
        Algorithm<IntegerSolution> algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
                .setPopulationSize(100)
                .setMaxEvaluations(20000)
                .setSelectionOperator(selection)
                .setSolutionListEvaluator(new SequentialSolutionListEvaluator<>())
                .build();

        // Step 4: Run the algorithm
        algorithm.run();

        // Step 5: Get and print the solution
        IntegerSolution solution = algorithm.result();
        System.out.println("Best solution: " + solution.variables());
        System.out.println("Objective value: " + solution.objectives()[0]);
    }
}