package ae.materiales.algorithm;

import ae.materiales.GreedySolver;
import ae.materiales.instances.Instance;
import ae.materiales.instances.ProblemInstances;
import ae.materiales.operadores.IncrementUnitMutation;
import ae.materiales.operadores.SwapMaterialesMutation;
import ae.materiales.problem.MaterialAllocationProblem;
import ae.materiales.operadores.ColumnCrossover;
import ae.materiales.operadores.*;
import org.uma.jmetal.component.algorithm.EvolutionaryAlgorithm;
import org.uma.jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;


import java.util.ArrayList;
import java.util.List;

import java.io.FileWriter;
import java.io.IOException;


public class MaterialAllocationNSGAIIRunner {

	public static void main(String[] args) {

		Instance instancia = ProblemInstances.instanciaPequena(); //inicializar con pequena por default

		String nombreInstancia = (args.length > 0) ? args[0] : "grande";

		switch (nombreInstancia) {
		case "pequena":
			instancia = ProblemInstances.instanciaPequena();
			System.out.println("Instancia pequena");
			break;
		case "mediana":
			instancia = ProblemInstances.instanciaMediana();
			System.out.println("Instancia mediana");
			break;
		case "grande":
			instancia = ProblemInstances.instanciaGrande();
			System.out.println("Instancia grande");
			break;
		}

		MaterialAllocationProblem problema = new MaterialAllocationProblem(instancia.nFamilias, instancia.nMateriales,
				instancia.demanda, instancia.stock, instancia.peso, instancia.capacidad);
		
		//cruzamientos:
		
		CrossoverOperator<IntegerSolution> crossover1 = new ColumnCrossover(0.3, instancia.nFamilias,
				instancia.nMateriales);
		CrossoverOperator<IntegerSolution> crossover2 = new FilasCrossover(0.4, instancia.nFamilias,
				instancia.nMateriales);
		CrossoverOperator<IntegerSolution> crossover3 = new SubMatrizCrossover(0.3, instancia.nFamilias,
				instancia.nMateriales);
		CrossoverOperator<IntegerSolution> crossover4 = new PosicionAPosicionCrossover(0.3, instancia.nFamilias,
				instancia.nMateriales);
		
		List<CrossoverOperator<IntegerSolution>>cruzamientos = new ArrayList<>();
		cruzamientos.add(crossover1);
		cruzamientos.add(crossover2);
		cruzamientos.add(crossover3);
		cruzamientos.add(crossover4);
		
		CrossoverOperator<IntegerSolution> crossover = new CompositeCrossover(cruzamientos);
		
		//--------------------------------------------------------
		
		//mutaciones:
		
		double pm;
		switch (nombreInstancia) {
		  case "pequena": pm = 0.15; break;
		  case "mediana": pm = 0.40; break;
		  default:        pm = 0.12; break;
		}

		MutationOperator<IntegerSolution> mutacion1 = new SwapMaterialesMutation(pm, instancia.nFamilias,
		    instancia.nMateriales, instancia.demanda);

		MutationOperator<IntegerSolution> mutacion2 = new IncrementUnitMutation(pm, instancia.nFamilias,
		    instancia.nMateriales, instancia.demanda, instancia.stock, instancia.peso, instancia.capacidad);

		List<MutationOperator<IntegerSolution>> mutaciones = new ArrayList<>();
		mutaciones.add(mutacion1);
		mutaciones.add(mutacion2);
		
		MutationOperator<IntegerSolution> mutacion = new CompositeMutation(mutaciones);
		
		//-----------------------------------------------------

		//ejecuta el greedy:
		
		GreedySolver greedy = new GreedySolver(instancia, problema);
		IntegerSolution solGreedy = greedy.allocate();
		problema.evaluate(solGreedy);
		if (!"pequena".equals(nombreInstancia) && !"mediana".equals(nombreInstancia)) {
		    problema.setSeedFromSolution(solGreedy);
		}


		// --------------------------------------------------

		int populationSize = 100;
		int offspringPopulationSize = 100;

		int maxEvaluations;
		switch (nombreInstancia) {
		  case "pequena": maxEvaluations = 50_000; break;
		  case "mediana": maxEvaluations = 600_000; break;
		  default:        maxEvaluations = 300_000; break; // grande
		}

		EvolutionaryAlgorithm<IntegerSolution> algoritmo =
		    new NSGAIIBuilder<IntegerSolution>(problema, populationSize, offspringPopulationSize, crossover, mutacion)
		        .setTermination(new TerminationByEvaluations(maxEvaluations))
		        .build();

		long startTime = System.currentTimeMillis();

		// Ejecutar el algoritmo
		algoritmo.run();

		List<IntegerSolution> poblacionFinal = algoritmo.result();
		
		long endTime = System.currentTimeMillis();
		long elapsedMillis = endTime - startTime;
		double elapsedSeconds = elapsedMillis / 1000.0;

		System.out.println("Tiempo de ejecución NSGA-II: " + elapsedSeconds + " segundos");
		
		// exportar resultados a CSV para graficar 
		String outputFile = "resultados_nsga2_greedy.csv";

		try (FileWriter writer = new FileWriter(outputFile)) {
		    writer.write("tipo,f1,f2\n");

		    // población final de NSGA-II
		    for (IntegerSolution s : poblacionFinal) {
		        double f1 = -s.objectives()[0]; 
		        double f2 = -s.objectives()[1];

		        writer.write(String.format("nsga2,%.6f,%.6f%n", f1, f2));
		    }

		    // solución greedy 
		    double f1Greedy = -solGreedy.objectives()[0];
		    double f2Greedy = -solGreedy.objectives()[1];
		    writer.write(String.format("greedy,%.6f,%.6f%n", f1Greedy, f2Greedy));

		    System.out.println("Resultados guardados en: " + outputFile);
		} catch (IOException e) {
		    System.err.println("Error escribiendo archivo de resultados: " + e.getMessage());
		}
		// --------------------------------------

		// pruebas para ver en consola
		System.out.println("NSGA-II corrió sin romperse");
		System.out.println("Tamaño de la población final: " + poblacionFinal.size());

		for (int idx = 0; idx < Math.min(50, poblacionFinal.size()); idx++) {
			IntegerSolution s = poblacionFinal.get(idx);
			double f1 = -s.objectives()[0];
			double f2 = -s.objectives()[1];
			System.out.println("Solución " + idx + " -> f1 = " + f1 + "  f2 = " + f2);
		}

		System.out.println("------------------------- ");
		System.out.println("Solución Greedy:");
		double f1Greedy = -solGreedy.objectives()[0];
		double f2Greedy = -solGreedy.objectives()[1];
		System.out.println("Greedy -> f1 = " + f1Greedy + "  f2 = " + f2Greedy);

	}
}
