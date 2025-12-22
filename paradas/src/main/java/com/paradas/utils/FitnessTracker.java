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
    final private List<double[]> history = new ArrayList<>();

    public void update(List<S> data) {
        generation++;

        S max = data.get(0);

        for (int i = 0; i < data.size(); i++)
            if (data.get(i).objectives()[0] < max.objectives()[0])
                max = data.get(i);

        history.add(max.objectives());

        // Display fitness as positive value (0-10 scale)
        System.out.printf("Generation %d: Best Fitness = %.4f (out of 1) \n",
                generation, -max.objectives()[0]);

    }

    public void saveToCsv() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "fitness_" + dateTime + ".csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("generation,fitness\n");

            for (int v = 0; v < history.size(); v++) {
                // Save as positive fitness value (0-10 scale)
                writer.write((v + 1) + "," + (-history.get(v)[0]) + "\n");
            }

            System.out.println("Results from fitness saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("An error occurred while saving the results: " + e.getMessage());
        }
    }
}
