package com.paradas.Abstraccion;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

public class AntenasProblem extends AbstractIntegerProblem{

    private int[][] grid = {
        {0,0,0,0,0,0,0,0},

        {0, 3,4,2,1,3,0 ,0},
        {0, 0,2,4,0,3,2 ,0},
        {0, 1,5,1,1,8,5 ,0},
        {0, 2,7,2,5,4,3 ,0},
        {0, 1,0,4,1,7,4 ,0},
        {0, 6,1,0,6,0,0 ,0},

        {0,0,0,0,0,0,0,0}
    };

    public AntenasProblem()
    {

    int cantVariables = 6;
    numberOfObjectives(1);

    // Set the lower and upper bounds for each variable (1 to 6 for each)
    List<Integer> lowerLimit = new ArrayList<>(cantVariables);
    List<Integer> upperLimit = new ArrayList<>(cantVariables);

    for (int i = 0; i < cantVariables; i++) {
      lowerLimit.add(1);  // Lower bound is 1
      upperLimit.add(6);  // Upper bound is 6
    }

    this.variableBounds(lowerLimit, upperLimit);
    }

    @Override
    public String name() {
      return "AntenasOptimasProblem";
    }

    @Override
    public IntegerSolution createSolution() {
        return new DefaultIntegerSolution(this.variableBounds(), this.numberOfObjectives(), 0);
    }


    private boolean contains( List<Integer[]> lista, Integer[] n )
    {
        for (Integer[] l : lista)
            if (l[0].equals(n[0]) && l[1].equals(n[1]) )
                return true;
        return  false;
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        int alcance = 0;

        List<Integer[]> visited = new ArrayList<>();

        for (int v = 0; v < numberOfVariables(); v+= 2)
        {
            for (int i = -1; i < 2; i++)
            {
                int y = solution.variables().get( v );
                int x = solution.variables().get( v + 1 );

                Integer[] coords1 =  {x + i, y};
                if (! contains(visited ,coords1))
                {
                    alcance += grid[x + i][y];
                    visited.add(coords1);
                    }
                    Integer[] coords2 =  {x, y + i};

                if (! contains(visited ,coords2))
                {
                    alcance += grid[x][y + i];
                    visited.add(coords2);
                }
            }
        }

        solution.objectives()[0] = -1.0 * alcance;

        return solution;
    }
    
}
