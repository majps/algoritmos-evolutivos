package ae.materiales.instances;

public class ProblemInstances {
	public static Instance instanciaPequena() {
        int nFamilias = 15;
        int nMateriales = 4;
        int[][] demanda = {
                // C   L   Ce  M
                { 5,  8,  0,  4},
                { 3,  0,  4,  2},
                { 0, 10,  3,  0},
                { 2,  4,  5,  3},
                { 6,  0,  2,  4},
                { 4,  6,  0,  5},
                { 1,  3,  2,  0},
                { 0,  5,  4,  2},
                { 3,  0,  6,  3},
                { 2,  2,  0,  4},
                { 0,  4,  3,  1},
                { 5,  6,  2,  0},
                { 4,  0,  5,  3},
                { 1,  2,  0,  2},
                { 3,  4,  2,  1}
            };

            // pesos por unidad: chapas, ladrillos, cemento, madera
            double[] peso = {15.0, 30.0, 40.0, 25.0};

            // stock total por material (aprox 70–80% de demanda total)
            int[] stock = {60, 80, 50, 55};

            // capacidad del camión (ej: 8000 kg aprox)
            double capacidad = 8000.0;

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
