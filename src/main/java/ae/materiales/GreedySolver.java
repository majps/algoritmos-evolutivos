package ae.materiales;

import ae.materiales.solutionhelper;
import ae.materiales.instances.*;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.problem.integerproblem.IntegerProblem; // o tu MyIntegerProblem
import java.util.*;

/**
 * Greedy allocator que construye una solución factible para el problema de asignación.
 * - Fase A: reparto equitativo por rondas (una unidad por familia en cada ronda)
 * - Fase B: relleno proporcional maximizando satisfacción por unidad de peso
 *
 * Devuelve una IntegerSolution construida via problem.createSolution().
 */
public class GreedySolver {

    private final Instance data;
    private final IntegerProblem problem; // para crear soluciones

    public GreedySolver(Instance data, IntegerProblem problem) {
        this.data = data;
        this.problem = problem;
    }

    /**
     * Ejecuta greedily la asignación y devuelve una solución factible.
     */
    public IntegerSolution allocate() {
        // Creamos solución inicial vacía
        IntegerSolution sol = (IntegerSolution) problem.createSolution(); // copia con variables iniciales (suponemos zeros)
        // Aseguramos que esté en 0
        zeroSolution(sol);

        // Variables auxiliares
        int F = data.nFamilias;
        int M = data.nMateriales;
        int[][] x = solutionhelper.toMatrix(sol, F, M); // verás que estamos usando helper
        int[] stockRem = Arrays.copyOf(data.stock, M);
        double capacityRem = data.capacidad;

        int[] familyDemandSum = new int[F];
        for (int i = 0; i < F; i++) {
            int s = 0;
            for (int j = 0; j < M; j++) s += data.demanda[i][j];
            familyDemandSum[i] = s;
        }

        // Helper para calcular satisfacción actual
        java.util.function.Supplier<double[]> satisfactionSupplier = () -> {
            double[] u = new double[F];
            for (int i = 0; i < F; i++) {
                if (familyDemandSum[i] == 0) { u[i] = 1.0; continue; }
                double sumMinFrac = 0.0;
                int Ki = 0;
                for (int j = 0; j < M; j++) {
                    if (data.demanda[i][j] > 0) {
                        Ki++;
                        sumMinFrac += Math.min(1.0, ((double) x[i][j]) / ((double) data.demanda[i][j]));
                    }
                }
                if (Ki == 0) u[i] = 1.0;
                else u[i] = sumMinFrac / (double) Ki;
            }
            return u;
        };

        // -----------------------------------
        // FASE A: reparto equitativo por rondas
        // -----------------------------------
        boolean anyAssignedThisRound = true;
        while (anyAssignedThisRound) {
            anyAssignedThisRound = false;

            double[] u = satisfactionSupplier.get();

            // familias ordenadas por menor satisfacción (priorizar quienes menos recibieron)
            Integer[] famOrder = new Integer[F];
            for (int i = 0; i < F; i++) famOrder[i] = i;
            Arrays.sort(famOrder, Comparator.comparingDouble(i -> u[i]));

            // por cada familia, intentar asignar 1 unidad de algún material factible
            for (int idx = 0; idx < F; idx++) {
                int i = famOrder[idx];
                // buscar material j tal que: x[i][j] < demand[i][j], stockRem[j] > 0, capacityRem - weight_j >= 0
                // elegimos el material de menor peso posible (favorece empaquetar mas unidades)
                int chosenJ = -1;
                double bestWeight = Double.POSITIVE_INFINITY;
                for (int j = 0; j < M; j++) {
                    if (data.demanda[i][j] <= x[i][j]) continue;       // ya satisfecho para ese material
                    if (stockRem[j] <= 0) continue;                  // sin stock
                    if (capacityRem - data.peso[j] < -1e-9) continue; // no entra por peso
                    // preferir menor peso
                    if (data.peso[j] < bestWeight) {
                        bestWeight = data.peso[j];
                        chosenJ = j;
                    }
                }
                if (chosenJ != -1) {
                    // asignar 1 unidad
                    x[i][chosenJ] += 1;
                    stockRem[chosenJ] -= 1;
                    capacityRem -= data.peso[chosenJ];
                    anyAssignedThisRound = true;
                }
            }
        }

        // -----------------------------------
        // FASE B: Relleno proporcional / greedy por prioridad
        // -----------------------------------
        // Creamos lista de candidatos (i,j) con demanda restante
        class Pair { int i; int j; double priority; }
        List<Pair> candidates = new ArrayList<>();
        for (int i = 0; i < F; i++) {
            int famSum = familyDemandSum[i];
            for (int j = 0; j < M; j++) {
                int rem = data.demanda[i][j] - x[i][j];
                if (rem <= 0) continue;
                Pair p = new Pair();
                p.i = i; p.j = j;
                // priority: (fraction of family demand remaining) / weight
                double frac = (famSum > 0) ? ((double) rem) / (double) famSum : (double) rem;
                p.priority = frac / (data.peso[j] + 1e-9);
                candidates.add(p);
            }
        }

        // ordenar descendente por prioridad
        candidates.sort((a,b) -> Double.compare(b.priority, a.priority));

        // recorrer e intentar asignar la mayor cantidad posible por candidato
        for (Pair p : candidates) {
            int i = p.i;
            int j = p.j;
            while (true) {
                int rem = data.demanda[i][j] - x[i][j];
                if (rem <= 0) break;
                if (stockRem[j] <= 0) break;
                if (capacityRem - data.peso[j] < -1e-9) break;
                // asignar 1 unidad
                x[i][j] += 1;
                stockRem[j] -= 1;
                capacityRem -= data.peso[j];
            }
        }

        // finalmente escribimos x en la solución
        solutionhelper.updateSolution(sol, x);

        return sol;
    }

    // pone todas las variables en 0
    private void zeroSolution(IntegerSolution sol) {
        for (int k = 0; k < sol.variables().size(); k++) {
            sol.variables().set(k, 0);
        }
    }
}
