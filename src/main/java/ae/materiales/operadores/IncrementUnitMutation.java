package ae.materiales.operadores;
import org.uma.jmetal.operator.mutation.*;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.Random;

import ae.materiales.solutionhelper;

@SuppressWarnings("serial")
public class IncrementUnitMutation implements MutationOperator<IntegerSolution> {

    private final double probability;
    private final int numFamilies;
    private final int numMaterials;

    private final int[] demanda;
    private final int[] stock;
    private final int[] cargaMax;

    private final Random random = new Random();

    public IncrementUnitMutation(
            double probability,
            int numFamilies,
            int numMaterials,
            int[] demandaAplanada,
            int[] stock,
            int[] cargaMax
    ) {
        this.probability = probability;
        this.numFamilies = numFamilies;
        this.numMaterials = numMaterials;
        this.demanda = demandaAplanada;
        this.stock = stock;
        this.cargaMax = cargaMax;
    }

    @Override
    public IntegerSolution execute(IntegerSolution solution) {
        if (random.nextDouble() > probability)
            return solution;

        int[][] m = solutionhelper.toMatrix(solution, numFamilies, numMaterials);

        int i = random.nextInt(numFamilies);
        int j = random.nextInt(numMaterials);

        // Chequeo demanda
        int current = m[i][j];
        int demandaMax = demanda[i * numMaterials + j];
        if (current + 1 > demandaMax)
            return solution;

        // Chequeo stock global del material j
        int sumMaterial = 0;
        for (int f = 0; f < numFamilies; f++)
            sumMaterial += m[f][j];
        if (sumMaterial + 1 > stock[j])
            return solution;

        // Chequeo carga familia
        int carga = 0;
        for (int f = 0; f < numMaterials; f++)
            carga += m[i][f];
        if (carga + 1 > cargaMax[i])
            return solution;

        // Si todo OK, incrementar
        m[i][j]++;

        solutionhelper.updateSolution(solution, m);
        return solution;
    }

	@Override
	public double mutationProbability() {
		return probability;
	}
}
