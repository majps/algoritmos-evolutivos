package ae.materiales.instances;

public class ProblemInstances {
	public static Instance instanciaPequena() {
        int nFamilias = 3;
        int nMateriales = 3;

        int[][] demanda = {
                {10,  5,  0},
                { 4,  2,  6},
                { 0,  3,  8}
        };

        int[] stock = {20, 10, 15};
        double[] peso = {1.0, 2.0, 3.0};
        double capacidad = 100.0;

        return new Instance(nFamilias, nMateriales, demanda, stock, peso, capacidad);
    }

    public static Instance instanciaMediana() {
        int nFamilias = 3;
        int nMateriales = 3;

        int[][] demanda = {
                {10,  5,  0},
                { 4,  2,  6},
                { 0,  3,  8}
        };

        int[] stock = {20, 10, 15};
        double[] peso = {1.0, 2.0, 3.0};
        double capacidad = 100.0;

        return new Instance(nFamilias, nMateriales, demanda, stock, peso, capacidad);
    }

    public static Instance instanciaGrande() {
        int nFamilias = 3;
        int nMateriales = 3;

        int[][] demanda = {
                {10,  5,  0},
                { 4,  2,  6},
                { 0,  3,  8}
        };

        int[] stock = {20, 10, 15};
        double[] peso = {1.0, 2.0, 3.0};
        double capacidad = 100.0;

        return new Instance(nFamilias, nMateriales, demanda, stock, peso, capacidad);
    }
}
