package ae.materiales.operadores;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import ae.materiales.solutionhelper;

import java.util.*;

@SuppressWarnings("serial")
public class PosicionAPosicionCrossover implements CrossoverOperator<IntegerSolution> {

    private final double probability;
    private final int numFamilies;
    private final int numMaterials;
    private final Random random = new Random();

    public PosicionAPosicionCrossover(double probability, int numFamilies, int numMaterials) {
        this.probability = probability;
        this.numFamilies = numFamilies;
        this.numMaterials = numMaterials;
    }

    @Override
    public List<IntegerSolution> execute(List<IntegerSolution> parents) {
        IntegerSolution father = parents.get(0);
        IntegerSolution mother = parents.get(1);

        if (random.nextDouble() > probability) {
            return List.of((IntegerSolution) father.copy());
        }

        int[][] A = solutionhelper.toMatrix(father, numFamilies, numMaterials);
        int[][] B = solutionhelper.toMatrix(mother, numFamilies, numMaterials);

        IntegerSolution child = (IntegerSolution) father.copy();
        int[][] C = new int[numFamilies][numMaterials];

        for (int i = 0; i < numFamilies; i++) {
            for (int j = 0; j < numMaterials; j++) {
                if (random.nextDouble() < 0.5) {
                    C[i][j] = A[i][j]; // padre
                } else {
                    C[i][j] = B[i][j]; // madre
                }
            }
        }

        solutionhelper.updateSolution(child, C);
        return List.of(child);
    }

    @Override
    public double crossoverProbability() {
        return probability;
    }

    @Override
    public int numberOfRequiredParents() {
        return 2;
    }

    @Override
    public int numberOfGeneratedChildren() {
        return 1;
    }
}