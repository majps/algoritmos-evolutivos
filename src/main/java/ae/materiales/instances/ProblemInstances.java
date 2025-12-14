package ae.materiales.instances;

import java.util.Random;

public class ProblemInstances {

    // 8 materiales
    // (en el informe: chapa, madera, cemento, cal, ticholo, clavos, arena, pintura)
    // pesos por unidad (kg)
    private static final double[] PESO = {
            19.0, // chapa (estimado)
            8.0,  // madera
            25.0, // cemento
            25.0, // cal
            4.0,  // ticholo
            1.0,  // clavos
            25.0, // arena bolsa 25 kg (Sodimac)
            22.0  // pintura látex balde 20 L (Sodimac)
    };

    private static final int N_MAT = PESO.length;

    public static Instance instanciaPequena() {
        int nFamilias = 15;
        double capacidad = 2 * 5950.0;

        int[][] demanda = generarDemanda(nFamilias, 101L);

        double[] ratios = {0.65, 0.70, 0.55, 0.60, 0.60, 0.80, 0.55, 0.60};
        int[] stock = generarStock(demanda, ratios);

        return new Instance(nFamilias, N_MAT, demanda, stock, PESO, capacidad);
    }

    public static Instance instanciaMediana() {
        int nFamilias = 45;
        double capacidad = 5 * 7600.0;

        int[][] demanda = generarDemanda(nFamilias, 202L);

        double[] ratios = {0.62, 0.68, 0.52, 0.58, 0.58, 0.78, 0.55, 0.60};
        int[] stock = generarStock(demanda, ratios);

        return new Instance(nFamilias, N_MAT, demanda, stock, PESO, capacidad);
    }

    public static Instance instanciaGrande() {
        int nFamilias = 100;
        double capacidad = 10 * 8000.0;

        int[][] demanda = generarDemanda(nFamilias, 303L);

        double[] ratios = {0.60, 0.66, 0.50, 0.56, 0.56, 0.75, 0.55, 0.60};
        int[] stock = generarStock(demanda, ratios);

        return new Instance(nFamilias, N_MAT, demanda, stock, PESO, capacidad);
    }

    // ------------------------
    // Demanda reproducible
    // ------------------------
    private static int[][] generarDemanda(int nFamilias, long seed) {
        Random rnd = new Random(seed);
        int[][] d = new int[nFamilias][N_MAT];

        for (int i = 0; i < nFamilias; i++) {
            // 40% leve, 40% media, 20% severa
            double r = rnd.nextDouble();
            int nivel = (r < 0.40) ? 0 : (r < 0.80 ? 1 : 2);

            // demandas base por nivel (8 materiales)
            int[] base;
            switch (nivel) {
                case 0: base = new int[]{ 6,  4,  4,  0, 20, 1,  2, 1}; break;
                case 1: base = new int[]{10,  7,  8,  4, 40, 2,  4, 2}; break;
                default: base = new int[]{16, 10, 12,  8, 70, 3,  6, 3}; break;
            }

            for (int j = 0; j < N_MAT; j++) {
                double factor = 0.8 + 0.4 * rnd.nextDouble();
                int val = (int) Math.round(base[j] * factor);

                // no siempre se necesita cal ni ticholo
                if (j == 3 && rnd.nextDouble() < 0.25) val = 0;
                if (j == 4 && rnd.nextDouble() < 0.15) val = 0;

                d[i][j] = Math.max(0, val);
            }
        }
        return d;
    }

    // ------------------------
    // Stock como % de demanda total
    // ------------------------
    private static int[] generarStock(int[][] demanda, double[] ratios) {
        int[] stock = new int[N_MAT];

        for (int j = 0; j < N_MAT; j++) {
            int total = 0;
            for (int i = 0; i < demanda.length; i++) total += demanda[i][j];

            int s = (int) Math.round(total * ratios[j]);

            if (total > 0) s = Math.max(1, Math.min(s, total));
            stock[j] = s;
        }
        return stock;
    }
}
