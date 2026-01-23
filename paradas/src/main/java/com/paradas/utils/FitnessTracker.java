package com.paradas.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.Solution;

public class FitnessTracker<S extends Solution<?>> {
    private int generation = 0;
    // store all solutions' objectives for each generation
    private final List<List<double[]>> historyAll = new ArrayList<>();
    // keep best per generation for backwards compatibility / quick view
    private final List<double[]> bestHistory = new ArrayList<>();

    public void update(List<S> data) {
        generation++;

        if (data == null || data.isEmpty()) {
            historyAll.add(new ArrayList<>());
            return;
        }

        // store all objectives for this generation
        List<double[]> gen = new ArrayList<>();
        for (S s : data) {
            gen.add(s.objectives().clone());
        }
        historyAll.add(gen);

        // find best by first objective (same logic as before)
        S max = data.get(0);
        for (int i = 0; i < data.size(); i++)
            if (data.get(i).objectives()[0] < max.objectives()[0])
                max = data.get(i);

        bestHistory.add(max.objectives());

        // Display fitness as positive value (0-1 scale)
        System.out.printf("Generation %d: Best Fitness = %.4f (out of 1) \n",
                generation, -max.objectives()[0]);

    }

    public void saveToCsv() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "pareto_evolution_" + dateTime + ".csv";

        // determine maximum number of objectives across all stored generations
        int maxObj = 0;
        for (List<double[]> gen : historyAll) {
            for (double[] o : gen) {
                if (o != null && o.length > maxObj)
                    maxObj = o.length;
            }
        }

        if (maxObj == 0) {
            System.out.println("No objective data to save.");
            return;
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            // header: generation,solution_index,is_pareto,obj1,...,objN
            StringBuilder header = new StringBuilder();
            header.append("generation,solution_index,is_pareto");
            for (int i = 1; i <= maxObj; i++)
                header.append(",obj").append(i);
            writer.write(header.toString() + "\n");

            for (int g = 0; g < historyAll.size(); g++) {
                List<double[]> gen = historyAll.get(g);
                if (gen == null || gen.isEmpty())
                    continue;

                boolean[] isPareto = computeNondominated(gen);

                for (int i = 0; i < gen.size(); i++) {
                    double[] obj = gen.get(i);
                    StringBuilder line = new StringBuilder();
                    line.append(g + 1).append(',').append(i + 1).append(',').append(isPareto[i] ? 1 : 0);

                    // write objectives, negating first for fitness-positivity consistent with
                    // display
                    for (int k = 0; k < maxObj; k++) {
                        line.append(',');
                        if (obj != null && k < obj.length) {
                            if (k == 0) {
                                line.append(-obj[k]);
                            } else {
                                line.append(obj[k]);
                            }
                        }
                    }

                    writer.write(line.toString() + "\n");
                }
            }

            System.out.println("Pareto evolution saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("An error occurred while saving the results: " + e.getMessage());
        }
    }

    // compute nondominated (Pareto) set for a generation
    private boolean[] computeNondominated(List<double[]> population) {
        int n = population.size();
        boolean[] isPareto = new boolean[n];
        for (int i = 0; i < n; i++)
            isPareto[i] = true;

        for (int i = 0; i < n; i++) {
            double[] a = population.get(i);
            if (a == null) {
                isPareto[i] = false;
                continue;
            }
            for (int j = 0; j < n; j++) {
                if (i == j)
                    continue;
                double[] b = population.get(j);
                if (b == null)
                    continue;
                if (dominates(b, a)) {
                    isPareto[i] = false;
                    break;
                }
            }
        }
        return isPareto;
    }

    // returns true if x dominates y (all objectives <= and at least one <).
    // assumes minimization for all objectives (consistent with jMetal objective
    // arrays).
    private boolean dominates(double[] x, double[] y) {
        boolean atLeastOneStrict = false;
        int len = Math.min(x.length, y.length);
        for (int i = 0; i < len; i++) {
            if (x[i] > y[i])
                return false;
            if (x[i] < y[i])
                atLeastOneStrict = true;
        }
        // if one vector is shorter, treat missing objectives as not comparable -> do
        // not consider domination
        if (x.length != y.length)
            return false;
        return atLeastOneStrict;
    }
}
