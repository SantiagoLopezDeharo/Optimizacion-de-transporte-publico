package com.paradas.utils;

import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

/**
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class CustomAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {
  final private Comparator<S> comparator;
  final private int maxEvaluations;
  private int evaluations;

  final private SolutionListEvaluator<S> evaluator;

  final private FitnessTracker tracker = new FitnessTracker<>();

  private double f1, f2, f3;

  private int conversionCount;

  /**
   * Constructor
   */
  public CustomAlgorithm(Problem<S> problem, int maxEvaluations, int populationSize,
                                      CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                                      SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
    super(problem);
    this.maxEvaluations = maxEvaluations;
    this.setMaxPopulationSize(populationSize);

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    this.evaluator = evaluator;

    comparator = new ObjectiveComparator<>(0);
  }

  @Override protected boolean isStoppingConditionReached() {

    return (evaluations >= maxEvaluations);
  }

  @Override protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    population.sort(comparator);
    offspringPopulation.add(population.get(0));
    offspringPopulation.add(population.get(1));
    offspringPopulation.sort(comparator);
    offspringPopulation.remove(offspringPopulation.size() - 1);
    offspringPopulation.remove(offspringPopulation.size() - 1);

    return offspringPopulation;
  }

  @Override protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());

    return population;
  }

  @Override public S result() {
    getPopulation().sort(comparator);
    return getPopulation().get(0);
  }

  @Override public void initProgress() {
    evaluations = getMaxPopulationSize();
  }

  @SuppressWarnings("unchecked")
  @Override 
  public void updateProgress() {
    tracker.update(null, getPopulation());
    evaluations += getMaxPopulationSize();
  }

  @Override public String name() {
    return "gGA" ;
  }

  @Override public String description() {
    return "Generational Genetic Algorithm" ;
  }
}
