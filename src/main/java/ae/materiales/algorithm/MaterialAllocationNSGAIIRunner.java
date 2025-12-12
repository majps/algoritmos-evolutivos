package ae.materiales.algorithm;

import ae.materiales.instances.Instance;
import ae.materiales.instances.ProblemInstances;
import ae.materiales.operadores.IncrementUnitMutation;
import ae.materiales.operadores.SwapMaterialesMutation;
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

    	String nombreInstancia = (args.length > 0)? args[0] : "pequena";
    	
    	Instance instancia = ProblemInstances.instanciaPequena();
    	
    	switch (nombreInstancia) {
    	case "pequena":
    		instancia = ProblemInstances.instanciaPequena();
    		break;
    	case "mediana":
    		instancia = ProblemInstances.instanciaMediana();
    		break;
    	case "grande":
    		instancia = ProblemInstances.instanciaGrande();
    		break;
    	}
    	
        MaterialAllocationProblem problema =
                new MaterialAllocationProblem(
                		instancia.nFamilias,
                		instancia.nMateriales,
                		instancia.demanda,
                		instancia.stock,
                		instancia.peso,
                		instancia.capacidad
                );

        CrossoverOperator<IntegerSolution> crossover =
                new IntegerSBXCrossover(1.0, 20.0);

        MutationOperator<IntegerSolution> mutacion =
                new SwapMaterialesMutation(0.8, instancia.nFamilias, instancia.nMateriales, instancia.demanda);

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
        
        for (int idx = 0; idx < Math.min(50, poblacionFinal.size()); idx++) {
            IntegerSolution s = poblacionFinal.get(idx);
            double f1 = -s.objectives()[0];
            double f2 = -s.objectives()[1];
            System.out.println("Solución " + idx + " -> f1 = " + f1 + "  f2 = " + f2);
        }
    }
}
