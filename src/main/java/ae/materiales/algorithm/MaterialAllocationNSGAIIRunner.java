package ae.materiales.algorithm;
import java.util.Locale;
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
		
		int populationSize = 150; //C25
		int offspringPopulationSize = populationSize;

		int maxEvaluations = 600_000;
		
		//CAMBIAR ACA nombreInstancia PARA ELEGIR CUAL EJECUTAR CON EL RUNNER:
		String nombreInstancia = (args.length > 0) ? args[0] : "pequena";
		String runId = (args.length > 1) ? args[1] : "0";

		
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
		problema.setPopulationSize(populationSize);

		int maxGenerations = maxEvaluations / populationSize;
		problema.setMaxGenerations(maxGenerations);
		
		//cruzamientos:
		
		double pC = 0.75; // C25

		CrossoverOperator<IntegerSolution> crossover1 =
		    new ColumnCrossover(pC * 0.4, instancia.nFamilias, instancia.nMateriales);
		CrossoverOperator<IntegerSolution> crossover2 =
		    new FilasCrossover(pC * 0.4, instancia.nFamilias, instancia.nMateriales);
		CrossoverOperator<IntegerSolution> crossover3 =
		    new SubMatrizCrossover(pC * 0.1, instancia.nFamilias, instancia.nMateriales);
		CrossoverOperator<IntegerSolution> crossover4 =
		    new PosicionAPosicionCrossover(pC * 0.1, instancia.nFamilias, instancia.nMateriales);

		
		List<CrossoverOperator<IntegerSolution>>cruzamientos = new ArrayList<>();
		cruzamientos.add(crossover1);
		cruzamientos.add(crossover2);
		cruzamientos.add(crossover3);
		cruzamientos.add(crossover4);
		
		CrossoverOperator<IntegerSolution> crossover = new CompositeCrossover(cruzamientos);
		
		//--------------------------------------------------------
		
		//mutaciones:
		
		double pm = 0.30; // C25

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

		EvolutionaryAlgorithm<IntegerSolution> algoritmo =
		    new NSGAIIBuilder<IntegerSolution>(problema, populationSize, offspringPopulationSize, crossover, mutacion)
		        .setTermination(new TerminationByEvaluations(maxEvaluations))
		        .build();

		long startTime = System.currentTimeMillis();

		// Ejecutar el algoritmo
		long start = System.nanoTime();
		algoritmo.run();
		long end = System.nanoTime();
		double timeMs = (end - start) / 1e6;
		System.out.println(timeMs);


		List<IntegerSolution> poblacionFinal = algoritmo.result();
		
		long endTime = System.currentTimeMillis();
		long elapsedMillis = endTime - startTime;
		double elapsedSeconds = elapsedMillis / 1000.0;

		System.out.println("Tiempo de ejecución NSGA-II: " + elapsedSeconds + " segundos");
		
		// exportar resultados a CSV para graficar 
		String outputFile = String.format("final_%s_run%s.csv", nombreInstancia, runId);


		try (FileWriter writer = new FileWriter(outputFile)) {
		    writer.write("tipo,f1,f2\n");

		    // población final de NSGA-II
		    for (IntegerSolution s : poblacionFinal) {
		        double f1 = -s.objectives()[0]; 
		        double f2 = -s.objectives()[1];
		        
		      //agregue el Locale.US para que me guarde bien el csv , si te llega a complicar sacale eso nomas
		        writer.write(String.format(Locale.US,"nsga2,%.6f,%.6f%n", f1, f2));
		    }

		    // solución greedy 
		    double f1Greedy = -solGreedy.objectives()[0];
		    double f2Greedy = -solGreedy.objectives()[1];
		    
		    //agregue el Locale.US para que me guarde bien el csv , si te llega a complicar sacale eso nomas
		    writer.write(String.format(Locale.US,"greedy,%.6f,%.6f%n", f1Greedy, f2Greedy));

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
