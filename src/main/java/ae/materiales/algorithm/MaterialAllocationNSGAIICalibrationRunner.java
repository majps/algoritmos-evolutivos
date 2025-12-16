package ae.materiales.algorithm;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

import ae.materiales.GreedySolver;
import ae.materiales.instances.Instance;
import ae.materiales.instances.ProblemInstances;
import ae.materiales.operadores.IncrementUnitMutation;
import ae.materiales.operadores.SwapMaterialesMutation;
import ae.materiales.operadores.ColumnCrossover;
import ae.materiales.operadores.FilasCrossover;
import ae.materiales.operadores.SubMatrizCrossover;
import ae.materiales.operadores.PosicionAPosicionCrossover;
import ae.materiales.operadores.CompositeCrossover;
import ae.materiales.operadores.CompositeMutation;
import ae.materiales.problem.MaterialAllocationProblem;

import org.uma.jmetal.component.algorithm.EvolutionaryAlgorithm;
import org.uma.jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import org.uma.jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.io.FileWriter;
import java.io.IOException;

public class MaterialAllocationNSGAIICalibrationRunner {

    public static void main(String[] args) {

        String nombreInstancia = (args.length > 0) ? args[0] : "mediana";
        String configId        = (args.length > 1) ? args[1] : "C0";
        String runId           = (args.length > 2) ? args[2] : "0";

        System.out.println("Instancia: " + nombreInstancia);
        System.out.println("Config: " + configId);
        System.out.println("Run id: " + runId);

        Instance instancia;

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
            default:
                System.out.println("Instancia desconocida, uso 'mediana'");
                instancia = ProblemInstances.instanciaMediana();
        }

        System.out.println("Familias: " + instancia.nFamilias +
                           ", Materiales: " + instancia.nMateriales);

        //    pm -> {0.10, 0.20, 0.30}
        //    pC -> {0.60, 0.75, 0.90}
        //    pop -> {50, 100, 150}
        double pm;
        double pC;
        int populationSize;
        int offspringPopulationSize;

        switch (configId) {
            // pop = 50
            case "C0":
                populationSize = 50; pm = 0.10; pC = 0.60; break;
            case "C1":
                populationSize = 50; pm = 0.10; pC = 0.75; break;
            case "C2":
                populationSize = 50; pm = 0.10; pC = 0.90; break;
            case "C3":
                populationSize = 50; pm = 0.20; pC = 0.60; break;
            case "C4":
                populationSize = 50; pm = 0.20; pC = 0.75; break;
            case "C5":
                populationSize = 50; pm = 0.20; pC = 0.90; break;
            case "C6":
                populationSize = 50; pm = 0.30; pC = 0.60; break;
            case "C7":
                populationSize = 50; pm = 0.30; pC = 0.75; break;
            case "C8":
                populationSize = 50; pm = 0.30; pC = 0.90; break;

            // pop = 100
            case "C9":
                populationSize = 100; pm = 0.10; pC = 0.60; break;
            case "C10":
                populationSize = 100; pm = 0.10; pC = 0.75; break;
            case "C11":
                populationSize = 100; pm = 0.10; pC = 0.90; break;
            case "C12":
                populationSize = 100; pm = 0.20; pC = 0.60; break;
            case "C13":
                populationSize = 100; pm = 0.20; pC = 0.75; break;
            case "C14":
                populationSize = 100; pm = 0.20; pC = 0.90; break;
            case "C15":
                populationSize = 100; pm = 0.30; pC = 0.60; break;
            case "C16":
                populationSize = 100; pm = 0.30; pC = 0.75; break;
            case "C17":
                populationSize = 100; pm = 0.30; pC = 0.90; break;

            // pop = 150
            case "C18":
                populationSize = 150; pm = 0.10; pC = 0.60; break;
            case "C19":
                populationSize = 150; pm = 0.10; pC = 0.75; break;
            case "C20":
                populationSize = 150; pm = 0.10; pC = 0.90; break;
            case "C21":
                populationSize = 150; pm = 0.20; pC = 0.60; break;
            case "C22":
                populationSize = 150; pm = 0.20; pC = 0.75; break;
            case "C23":
                populationSize = 150; pm = 0.20; pC = 0.90; break;
            case "C24":
                populationSize = 150; pm = 0.30; pC = 0.60; break;
            case "C25":
                populationSize = 150; pm = 0.30; pC = 0.75; break;
            case "C26":
                populationSize = 150; pm = 0.30; pC = 0.90; break;

            default:
                System.out.println("ConfigId desconocida (" + configId + "), uso C0");
                populationSize = 50; pm = 0.10; pC = 0.60;
        }

        offspringPopulationSize = populationSize;

        int maxEvaluations = 600_000;   // fijo para todas las configs
        int maxGenerations = maxEvaluations / populationSize;

        System.out.println("Parámetros:");
        System.out.println("  populationSize = " + populationSize);
        System.out.println("  offspringPopulationSize = " + offspringPopulationSize);
        System.out.println("  maxEvaluations = " + maxEvaluations);
        System.out.println("  maxGenerations = " + maxGenerations);
        System.out.println("  pm = " + pm);
        System.out.println("  pC = " + pC);

        MaterialAllocationProblem problema = new MaterialAllocationProblem(
                instancia.nFamilias,
                instancia.nMateriales,
                instancia.demanda,
                instancia.stock,
                instancia.peso,
                instancia.capacidad
        );

        problema.setPopulationSize(populationSize);
        problema.setMaxGenerations(maxGenerations);

        // pC entre los 4 operadores
        double pCColumn = pC * 0.4;
        double pCFilas  = pC * 0.4;
        double pCSub    = pC * 0.1;
        double pCPos    = pC * 0.1;

        CrossoverOperator<IntegerSolution> crossover1 =
                new ColumnCrossover(pCColumn, instancia.nFamilias,
                        instancia.nMateriales);
        CrossoverOperator<IntegerSolution> crossover2 =
                new FilasCrossover(pCFilas, instancia.nFamilias,
                        instancia.nMateriales);
        CrossoverOperator<IntegerSolution> crossover3 =
                new SubMatrizCrossover(pCSub, instancia.nFamilias,
                        instancia.nMateriales);
        CrossoverOperator<IntegerSolution> crossover4 =
                new PosicionAPosicionCrossover(pCPos, instancia.nFamilias,
                        instancia.nMateriales);

        List<CrossoverOperator<IntegerSolution>> cruzamientos = new ArrayList<>();
        cruzamientos.add(crossover1);
        cruzamientos.add(crossover2);
        cruzamientos.add(crossover3);
        cruzamientos.add(crossover4);

        CrossoverOperator<IntegerSolution> crossover =
                new CompositeCrossover(cruzamientos);

        MutationOperator<IntegerSolution> mutacion1 =
                new SwapMaterialesMutation(pm, instancia.nFamilias,
                        instancia.nMateriales, instancia.demanda);

        MutationOperator<IntegerSolution> mutacion2 =
                new IncrementUnitMutation(pm, instancia.nFamilias,
                        instancia.nMateriales, instancia.demanda,
                        instancia.stock, instancia.peso, instancia.capacidad);

        List<MutationOperator<IntegerSolution>> mutaciones = new ArrayList<>();
        mutaciones.add(mutacion1);
        mutaciones.add(mutacion2);

        MutationOperator<IntegerSolution> mutacion =
                new CompositeMutation(mutaciones);

        //greedy + seeding para grande
        GreedySolver greedy = new GreedySolver(instancia, problema);
        IntegerSolution solGreedy = greedy.allocate();
        problema.evaluate(solGreedy);

        if ("grande".equals(nombreInstancia)) {
            System.out.println("Usando solución Greedy como semilla en instancia grande");
            problema.setSeedFromSolution(solGreedy);
        }

        EvolutionaryAlgorithm<IntegerSolution> algoritmo =
                new NSGAIIBuilder<IntegerSolution>(
                        problema, populationSize, offspringPopulationSize,
                        crossover, mutacion
                )
                        .setTermination(new TerminationByEvaluations(maxEvaluations))
                        .build();

        long startTime = System.currentTimeMillis();
        algoritmo.run();
        long endTime = System.currentTimeMillis();
        double elapsedSeconds = (endTime - startTime) / 1000.0;

        System.out.println("Tiempo de ejecución NSGA-II: " + elapsedSeconds + " segundos");

        List<IntegerSolution> poblacionFinal = algoritmo.result();

        //csv
        String outputFile = String.format(
                "resultados_%s_%s_run%s.csv",
                nombreInstancia, configId, runId
        );

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("tipo,f1,f2\n");

            // poblacion final de NSGA-II
            for (IntegerSolution s : poblacionFinal) {
                double f1 = -s.objectives()[0];
                double f2 = -s.objectives()[1];

                writer.write(String.format(Locale.US,
                        "nsga2,%.6f,%.6f%n", f1, f2));
            }

            // solucion greedy
            double f1Greedy = -solGreedy.objectives()[0];
            double f2Greedy = -solGreedy.objectives()[1];

            writer.write(String.format(Locale.US,
                    "greedy,%.6f,%.6f%n", f1Greedy, f2Greedy));

            System.out.println("Resultados guardados en: " + outputFile);
        } catch (IOException e) {
            System.err.println("Error escribiendo archivo de resultados: " + e.getMessage());
        }

        //para consola
        System.out.println("NSGA-II corrió sin romperse");
        System.out.println("Tamaño de la población final: " + poblacionFinal.size());

        for (int idxSol = 0; idxSol < Math.min(10, poblacionFinal.size()); idxSol++) {
            IntegerSolution s = poblacionFinal.get(idxSol);
            double f1 = -s.objectives()[0];
            double f2 = -s.objectives()[1];
            System.out.println("Solución " + idxSol + " -> f1 = " + f1 + "  f2 = " + f2);
        }

        System.out.println("------------------------- ");
        System.out.println("Solución Greedy:");
        double f1GreedyPrint = -solGreedy.objectives()[0];
        double f2GreedyPrint = -solGreedy.objectives()[1];
        System.out.println("Greedy -> f1 = " + f1GreedyPrint + "  f2 = " + f2GreedyPrint);
    }
}
