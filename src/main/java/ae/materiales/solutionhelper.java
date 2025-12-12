package ae.materiales;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import ae.materiales.instances.Instance;

public class solutionhelper {

	public static int idx(int family, int material, int numMaterials) {
        return family * numMaterials + material;
    }

    public static int getValue(IntegerSolution sol, int family, int material, int numMaterials) {
        return sol.variables().get(idx(family, material, numMaterials));
    }

    public static void setValue(IntegerSolution sol, int family, int material, int numMaterials, int value) {
        sol.variables().set(idx(family, material, numMaterials), value);
    }

    public static int sumColumn(IntegerSolution sol, int material, Instance data) {
        int s = 0;
        for (int i = 0; i < data.nFamilias; i++) s += getValue(sol, i, material, data.nMateriales);
        return s;
    }

    public static int sumRow(IntegerSolution sol, int family, Instance data) {
        int s = 0;
        for (int j = 0; j < data.nMateriales; j++) s += getValue(sol, family, j, data.nMateriales);
        return s;
    }

    public static double totalWeight(IntegerSolution sol, Instance data) {
        double w = 0.0;
        for (int j = 0; j < data.nMateriales; j++) {
            int colSum = sumColumn(sol, j, data);
            w += colSum * data.peso[j];
        }
        return w;
    }

    public static boolean respectDemand(IntegerSolution sol, Instance data) {
        for (int i = 0; i < data.nFamilias; i++) {
            for (int j = 0; j < data.nMateriales; j++) {
                if (getValue(sol, i, j, data.nMateriales) > data.demanda[i][j]) return false;
            }
        }
        return true;
    }

    public static boolean respectStock(IntegerSolution sol, Instance data) {
        for (int j = 0; j < data.nMateriales; j++) {
            if (sumColumn(sol, j, data) > data.stock[j]) return false;
        }
        return true;
    }

    public static boolean respectCapacity(IntegerSolution sol, Instance data) {
        return totalWeight(sol, data) <= data.capacidad + 1e-9;
    }

    public static double[] familySatisfactionVector(IntegerSolution sol, Instance data) {
        double[] u = new double[data.nFamilias];
        for (int i = 0; i < data.nFamilias; i++) {
            int got = sumRow(sol, i, data);
            int demandSum = 0;
            for (int j = 0; j < data.nMateriales; j++) demandSum += data.demanda[i][j];
            if (demandSum == 0) u[i] = 1.0;
            else u[i] = ((double) got) / ((double) demandSum);
        }
        return u;
    }
    public static int[][] toMatrix(IntegerSolution solution, int numFamilies, int numMaterials) {
        int[][] m = new int[numFamilies][numMaterials];
        int index = 0;
        for (int i = 0; i < numFamilies; i++) {
            for (int j = 0; j < numMaterials; j++) {
                m[i][j] = solution.variables().get(index);
                index++;
            }
        }
        return m;
    }
    public static void updateSolution(IntegerSolution sol, int[][] m) {
        int index = 0;

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                sol.variables().set(index, m[i][j]);
                index++;
            }
        }
    }
}
