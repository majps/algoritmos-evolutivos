package ae.materiales.operadores;
import ae.materiales.solutionhelper;
import org.uma.jmetal.operator.mutation.*;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.Random;
@SuppressWarnings("serial")
public class SwapMaterialesMutation implements MutationOperator<IntegerSolution> {

    private final double probability;
    private final int numFamilies;
    private final int numMaterials;
    private final int[][] demandaPorFamilia;   
    private final Random random = new Random();

    public SwapMaterialesMutation(
            double probability,
            int numFamilies,
            int numMaterials,
            int[][] demandaPorFamiliaAplanada 
    ) {
        this.probability = probability;
        this.numFamilies = numFamilies;
        this.numMaterials = numMaterials;
        this.demandaPorFamilia = demandaPorFamiliaAplanada;
    }

    public  IntegerSolution execute(IntegerSolution solution) {
        if (random.nextDouble() > probability)
            return solution;

        int[][] m = solutionhelper.toMatrix(solution, numFamilies, numMaterials);

        // Selección heurística:
        // Familia A = más cargada
        // Familia B = menos cargada
        
        int maxFam = 0, minFam = 0;
        int maxCarga = -1, minCarga = Integer.MAX_VALUE;

        for (int i = 0; i < numFamilies; i++) {
            int carga = 0;
            for (int j = 0; j < numMaterials; j++)
                carga += m[i][j];

            if (carga > maxCarga) {
                maxCarga = carga;
                maxFam = i;
            }
            if (carga < minCarga) {
                minCarga = carga;
                minFam = i;
            }
        }

        // Elegir material aleatorio
        int j = random.nextInt(numMaterials);

        // Debe tener demanda en destino
        int demandaDestino = demandaPorFamilia[minFam][j];
        if (demandaDestino == 0) {
            return solution;
        }

        // Intercambio
        int temp = m[maxFam][j];
        m[maxFam][j] = m[minFam][j];
        m[minFam][j] = temp;

        // ajusto dependiendo las demandas para coherencia
        if (m[minFam][j] > demandaPorFamilia[minFam][j]) {
            m[minFam][j] = demandaPorFamilia[minFam][j];
        }
        if (m[maxFam][j] > demandaPorFamilia[maxFam][j]) {
            m[maxFam][j] = demandaPorFamilia[maxFam][j];
        }

        solutionhelper.updateSolution(solution, m);
        return solution;
    }

	@Override
	public double mutationProbability() {
		return probability;
	}
}
