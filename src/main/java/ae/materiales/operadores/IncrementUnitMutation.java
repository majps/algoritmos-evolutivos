package ae.materiales.operadores;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.Random;

import ae.materiales.solutionhelper;

@SuppressWarnings("serial")
public class IncrementUnitMutation implements MutationOperator<IntegerSolution> {

    private final double probability;
    private final int numFamilies;
    private final int numMaterials;

    private final int[][] demanda;   // d_ij
    private final int[] stock;       // s_j
    private final double[] peso;        // p_j
    private final double capacidad;     // C

    private final Random random = new Random();

    public IncrementUnitMutation(
            double probability,
            int numFamilies,
            int numMaterials,
            int[][] demanda,
            int[] stock,
            double[] peso,
            double capacidad
    ) {
        this.probability = probability;
        this.numFamilies = numFamilies;
        this.numMaterials = numMaterials;
        this.demanda = demanda;
        this.stock = stock;
        this.peso = peso;
        this.capacidad = capacidad;
    }

    @Override
    public IntegerSolution execute(IntegerSolution solution) {

        if (random.nextDouble() > probability)
            return solution;

        int[][] m = solutionhelper.toMatrix(solution, numFamilies, numMaterials);

        int i = random.nextInt(numFamilies);
        int j = random.nextInt(numMaterials);

        // 1️⃣ Chequeo demanda: x_ij + 1 <= d_ij
        if (m[i][j] + 1 > demanda[i][j])
            return solution;

        // 2️⃣ Chequeo stock del material j
        int totalMaterialJ = 0;
        for (int f = 0; f < numFamilies; f++)
            totalMaterialJ += m[f][j];

        if (totalMaterialJ + 1 > stock[j])
            return solution;

        // 3️⃣ Chequeo capacidad total
        int pesoTotal = 0;
        for (int f = 0; f < numFamilies; f++)
            for (int k = 0; k < numMaterials; k++)
                pesoTotal += m[f][k] * peso[k];

        if (pesoTotal + peso[j] > capacidad)
            return solution;

        // ✔ Incremento unitario válido
        m[i][j]++;

        solutionhelper.updateSolution(solution, m);
        return solution;
    }

    @Override
    public double mutationProbability() {
        return probability;
    }
}
