package com.paradas.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.RankingAndCrowdingSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

/**
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class CustomAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {

  protected final int maxEvaluations;

  protected final SolutionListEvaluator<S> evaluator;

  protected int evaluations;
  protected Comparator<S> dominanceComparator;

  final private FitnessTracker tracker = new FitnessTracker<>();

  protected int matingPoolSize;
  protected int offspringPopulationSize;

  /**
   * Constructor
   */
  public CustomAlgorithm(Problem<S> problem, int maxEvaluations, int populationSize,
      int matingPoolSize, int offspringPopulationSize,
      CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
      SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
    this(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize,
        crossoverOperator, mutationOperator, selectionOperator, new DefaultDominanceComparator<S>(),
        evaluator);
  }

  /**
   * Constructor
   */
  public CustomAlgorithm(Problem<S> problem, int maxEvaluations, int populationSize,
      int matingPoolSize, int offspringPopulationSize,
      CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
      SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator,
      SolutionListEvaluator<S> evaluator) {
    super(problem);
    this.maxEvaluations = maxEvaluations;
    setMaxPopulationSize(populationSize);

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    this.evaluator = evaluator;
    this.dominanceComparator = dominanceComparator;

    this.matingPoolSize = matingPoolSize;
    this.offspringPopulationSize = offspringPopulationSize;
  }

  @Override
  protected void initProgress() {
    evaluations = getMaxPopulationSize();
  }

  @Override
  protected void updateProgress() {
    tracker.update(result());
    evaluations += offspringPopulationSize;
  }

  @Override
  protected boolean isStoppingConditionReached() {
    return evaluations >= maxEvaluations;
  }

  @Override
  protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());

    return population;
  }

  /**
   * This method iteratively applies a {@link SelectionOperator} to the population to fill the
   * mating pool population.
   *
   * @param population
   * @return The mating pool population
   */
  @Override
  protected List<S> selection(List<S> population) {
    List<S> matingPopulation = new ArrayList<>(population.size());
    for (int i = 0; i < matingPoolSize; i++) {
      S solution = selectionOperator.execute(population);
      matingPopulation.add(solution);
    }

    return matingPopulation;
  }

  /**
   * This methods iteratively applies a {@link CrossoverOperator} a  {@link MutationOperator} to the
   * population to create the offspring population. The population size must be divisible by the
   * number of parents required by the {@link CrossoverOperator}; this way, the needed parents are
   * taken sequentially from the population.
   * <p>
   * The number of solutions returned by the {@link CrossoverOperator} must be equal to the
   * offspringPopulationSize state variable
   *
   * @param matingPool
   * @return The new created offspring population
   */
  @Override
  protected List<S> reproduction(List<S> matingPool) {
    int numberOfParents = crossoverOperator.numberOfRequiredParents();

    checkNumberOfParents(matingPool, numberOfParents);

    List<S> offspringPopulation = new ArrayList<>(offspringPopulationSize);
    for (int i = 0; i < matingPool.size(); i += numberOfParents) {
      List<S> parents = new ArrayList<>(numberOfParents);
      for (int j = 0; j < numberOfParents; j++) {
        parents.add(matingPool.get(i + j));
      }

      List<S> offspring = crossoverOperator.execute(parents);

      for (S s : offspring) {
        mutationOperator.execute(s);
        offspringPopulation.add(s);
        if (offspringPopulation.size() >= offspringPopulationSize) {
          break;
        }
      }
    }
    return offspringPopulation;
  }

  @Override
  protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    List<S> jointPopulation = new ArrayList<>();
    jointPopulation.addAll(population);
    jointPopulation.addAll(offspringPopulation);

    RankingAndCrowdingSelection<S> rankingAndCrowdingSelection;
    rankingAndCrowdingSelection = new RankingAndCrowdingSelection<S>(getMaxPopulationSize(),
        dominanceComparator);

    return rankingAndCrowdingSelection.execute(jointPopulation);
  }

  @Override
  public List<S> result() {
    return SolutionListUtils.getNonDominatedSolutions(getPopulation());
  }

  @Override
  public String name() {
    return "NSGAII";
  }

  @Override
  public String description() {
    return "Nondominated Sorting Genetic Algorithm version II";
  }

  public void saveFitnessToCsv() {
    tracker.saveToCsv();
  }
}
