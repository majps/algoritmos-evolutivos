package ae.materiales.algorithm;

import ae.materiales.problem.MaterialAllocationProblem;
import org.uma.jmetal.component.algorithm.EvolutionaryAlgorithm;
import org.uma.jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.List;

public class MaterialAllocationNSGAIIRunner {

    public static void main(String[] args) {

        // Instancia chiquita de prueba
        int nFamilias = 2;
        int nMateriales = 2;

        int[][] demanda = {
                {10, 5},
                {3, 7}
        };
        int[] stock = {20, 12};
        double[] peso = {1.0, 1.5};
        double capacidad = 100.0;

        MaterialAllocationProblem problema =
                new MaterialAllocationProblem(
                        nFamilias,
                        nMateriales,
                        demanda,
                        stock,
                        peso,
                        capacidad
                );

        CrossoverOperator<IntegerSolution> crossover =
                new IntegerSBXCrossover(1.0, 20.0);

        MutationOperator<IntegerSolution> mutacion =
                new IntegerPolynomialMutation(
                        1.0 / problema.numberOfVariables(), 20.0);

        int populationSize = 50;
        int offspringPopulationSize = 50;

        // de jmetal-component
        EvolutionaryAlgorithm<IntegerSolution> algoritmo =
                new NSGAIIBuilder<IntegerSolution>(
                        problema,
                        populationSize,
                        offspringPopulationSize,
                        crossover,
                        mutacion
                ).build();

        // Ejecutar el algoritmo
        algoritmo.run();

        List<IntegerSolution> poblacionFinal = algoritmo.result();

        
        //pruebas para ver en consola 
        System.out.println("NSGA-II corrió sin romperse");
        System.out.println("Tamaño de la población final: " + poblacionFinal.size());
        
        for (int idx = 0; idx < Math.min(5, poblacionFinal.size()); idx++) {
            IntegerSolution s = poblacionFinal.get(idx);
            double f1 = -s.objectives()[0];
            double f2 = -s.objectives()[1];
            System.out.println("Solución " + idx + " -> f1 = " + f1 + "  f2 = " + f2);
        }
    }
}
