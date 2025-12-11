package ae.materiales.problem;

import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.ArrayList;
import java.util.List;

public class MaterialAllocationProblem extends AbstractIntegerProblem {

    private int nFamilias;
    private int nMateriales;

    private int[][] demanda;   // d_ij
    private int[] stock;      // s_j
    private double[] peso;  // p_j
    private double capacidad;  // C

    public MaterialAllocationProblem(
            int nFamilias,
            int nMateriales,
            int[][] demanda,
            int[] stock,
            double[] peso,
            double capacidad) {

        this.nFamilias = nFamilias;
        this.nMateriales = nMateriales;
        this.demanda = demanda;
        this.stock = stock;
        this.peso = peso;
        this.capacidad = capacidad;

        int numberOfVariables = nFamilias * nMateriales;

        //bounds
        List<Integer> lower = new ArrayList<>(numberOfVariables);
        List<Integer> upper = new ArrayList<>(numberOfVariables);

        for (int i = 0; i < nFamilias; i++) {
            for (int j = 0; j < nMateriales; j++) {
                lower.add(0);
                upper.add(demanda[i][j]); // no puede asignar más que la demanda
            }
        }


        variableBounds(lower, upper);

        // cantidad de objetivos / restricciones y nombre
        numberOfObjectives(2);        // f1 y f2
        numberOfConstraints(0);       // por ahora sin constraints explícitas
        name("MaterialAllocationProblem");
    }

    private int index(int i, int j) {
        return i * nMateriales + j;
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        // dummy para que compile y corra
        solution.objectives()[0] = 0.0;
        solution.objectives()[1] = 0.0;
        return solution;
    }
}
