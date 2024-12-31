package com.paradas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.tablesaw.io.csv.CsvReader;

public class Greedy {

    private static Map<String, Map<String, Integer>> ODMatrix;
    private static Map<String, Integer> demanda;
    private static Map<String, Integer> solution;
    private static List<String> segments;
    private static int maxDemanda = 0;
    private static final int MAX_ITER = 25000;
    
    private static void initializeODMatrix() {
        ODMatrix = new HashMap<>();

        try (InputStream inputStream = CsvReader.class.getClassLoader().getResourceAsStream("data.csv")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found in resources: data.csv");
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                // Skip the header line
                br.readLine();
    
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
    
                    if (values.length == 3) {
                        String origin = values[0];
                        String destination = values[1];
                        int passengers = Integer.parseInt(values[2]);
    
                        // Add data to the map
                        ODMatrix.computeIfAbsent(origin, k -> new HashMap<>())
                                .put(destination, passengers);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }


    }

    private static void initializeSol() {
        solution = new HashMap<>();

        for (String origin : ODMatrix.keySet()) {

            solution.put(origin, 0);

            for (String destination : ODMatrix.get(origin).keySet()) {
                solution.put(destination, 0);
            }

        }
    }

    private static void initializeSeg() {
        segments = new ArrayList<>();

        for (String seg : solution.keySet()) {
            segments.add(seg);
        }
    }

    private static void initializeDem() {
        demanda = new HashMap<>();

        for (String origin : ODMatrix.keySet())
        {
            int demandadx = 0;

            for (String destination : ODMatrix.get(origin).keySet())
            {
                int demandValue = ODMatrix.get(origin).get(destination);

                demandadx += demandValue;
                demanda.put(destination, demanda.getOrDefault(destination, 0) + demandValue);

            }

            demanda.put(origin, demanda.getOrDefault(origin, 0) + demandadx);
        }

        for (String d : demanda.keySet()) { 
            maxDemanda = maxDemanda < demanda.get(d) ? demanda.get(d) : maxDemanda;
        }
    }

    // Function to determine if a given origin-destionation bus stop are present on the solution
    private static int cubierto(Map<String,Integer> solution, String origin, String destination) {
        return solution.get(origin) > 0 && solution.get(destination) > 0 ? 1 : 0;
    }

    private static double fitness(Map<String,Integer> solution) {
        double f1 = 0;
        // Maximize coverage of the demand of each route
        for (String origin : ODMatrix.keySet())
            for (String destination : ODMatrix.get(origin).keySet())
                f1 += (ODMatrix.get(origin).get(destination)) * cubierto(solution, origin, destination); // A revisar
        

        double f2 = 0;
        // Minimize amount of bus stops ( less bus stops would reduce the amount of time for the bus to go from A to B )
        for (int busStop : solution.values()) {
            f2 += busStop == 0 ? 0 : 1;
        }

        double f3 = 0;
        // Minimize cost
        for (Map.Entry<String, Integer> entry : solution.entrySet()) {
            f3 += entry.getValue() * (2 * (1 - (demanda.get(entry.getKey()) / (double) maxDemanda)) - 1);  // A revisar
        }

        return (-1) * f1 + f2 + f3 ; // we make a weighted-fitness with all this factors
    }
    
    public static void main(String[] args) {
        initializeODMatrix();
        initializeSol();
        initializeSeg();
        initializeDem();

        int k = 0;

        while (k < MAX_ITER) {
            String bestSegment = null;
            int bestBusStop = -1;
            double minFitness = fitness(solution);
            System.out.println(minFitness);

            int i = 0;
            for (String segment : segments) {

                int prev = solution.get(segment);

                for (int busStop = 1; busStop <= 3; busStop++) {
                    solution.put(segment, busStop);

                    double fitness = fitness(solution);

                    if (fitness < minFitness) {
                        System.out.println("true: " + segment + " - " + busStop);
                        bestSegment = segment;
                        bestBusStop = busStop;
                        minFitness = fitness;
                    }

                }

                solution.put(segment, prev);

                System.out.println(i);
                i++;
            }

            if (bestSegment != null) {
                solution.put(bestSegment, bestBusStop);
                segments.remove(bestSegment);
            }
            else {
                break;
            }

            k++;
        }

        // Print solution
        for (Map.Entry<String, Integer> entry : solution.entrySet()) {
            System.out.println(entry.getKey() + "," + entry.getValue());
        }
    }
}