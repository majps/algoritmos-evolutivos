package ae.materiales.instances;

import java.util.Arrays;
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
            25.0, // arena 
            22.0  // pintura látex  
    };

    private static final int N_MAT_TOTAL = PESO.length; // 8

    public static Instance instanciaPequena() {
        int nFamilias = 15;
        int nMat = 4;                 // <-- solo 4 primeros materiales
        double capacidad = 2 * 5950.0;

        int[][] demanda = generarDemanda(nFamilias, nMat, 101L);

        double[] ratios8 = {0.65, 0.70, 0.55, 0.60, 0.60, 0.80, 0.55, 0.60};
        int[] stock = generarStock(demanda, Arrays.copyOf(ratios8, nMat), nMat);

        double[] peso = Arrays.copyOf(PESO, nMat);
        return new Instance(nFamilias, nMat, demanda, stock, peso, capacidad);
    }

    public static Instance instanciaMediana() {
        int nFamilias = 45;
        int nMat = 6;                 // <-- solo 6 primeros materiales
        double capacidad = 5 * 7600.0;

        int[][] demanda = generarDemanda(nFamilias, nMat, 202L);

        double[] ratios8 = {0.62, 0.68, 0.52, 0.58, 0.58, 0.78, 0.55, 0.60};
        int[] stock = generarStock(demanda, Arrays.copyOf(ratios8, nMat), nMat);

        double[] peso = Arrays.copyOf(PESO, nMat);
        return new Instance(nFamilias, nMat, demanda, stock, peso, capacidad);
    }

    public static Instance instanciaGrande() {
        int nFamilias = 100;
        int nMat = 8;                 // <-- los 8 materiales
        double capacidad = 10 * 8000.0;

        int[][] demanda = generarDemanda(nFamilias, nMat, 303L);

        double[] ratios8 = {0.60, 0.66, 0.50, 0.56, 0.56, 0.75, 0.55, 0.60};
        int[] stock = generarStock(demanda, Arrays.copyOf(ratios8, nMat), nMat);

        double[] peso = Arrays.copyOf(PESO, nMat);
        return new Instance(nFamilias, nMat, demanda, stock, peso, capacidad);
    }

    // ------------------------
    // Demanda reproducible (para nMat materiales)
    // ------------------------
    private static int[][] generarDemanda(int nFamilias, int nMat, long seed) {
        Random rnd = new Random(seed);
        int[][] d = new int[nFamilias][nMat];

        for (int i = 0; i < nFamilias; i++) {
            // 40% leve, 40% media, 20% severa
            double r = rnd.nextDouble();
            int nivel = (r < 0.40) ? 0 : (r < 0.80 ? 1 : 2);

            // bases definidas para 8 materiales, luego se recortan a nMat
            int[] base8;
            switch (nivel) {
                case 0:  base8 = new int[]{ 6,  4,  4,  0, 20, 1,  2, 1}; break;
                case 1:  base8 = new int[]{10,  7,  8,  4, 40, 2,  4, 2}; break;
                default: base8 = new int[]{16, 10, 12,  8, 70, 3,  6, 3}; break;
            }

            int[] base = Arrays.copyOf(base8, nMat);

            for (int j = 0; j < nMat; j++) {
                double factor = 0.8 + 0.4 * rnd.nextDouble();
                int val = (int) Math.round(base[j] * factor);

                // no siempre se necesita cal (j=3) ni ticholo (j=4) (si existen en esta instancia)
                if (j == 3 && nMat > 3 && rnd.nextDouble() < 0.25) val = 0;
                if (j == 4 && nMat > 4 && rnd.nextDouble() < 0.15) val = 0;

                d[i][j] = Math.max(0, val);
            }
        }
        return d;
    }

    // ------------------------
    // Stock como % de demanda total (para nMat materiales)
    // ------------------------
    private static int[] generarStock(int[][] demanda, double[] ratios, int nMat) {
        int[] stock = new int[nMat];

        for (int j = 0; j < nMat; j++) {
            int total = 0;
            for (int i = 0; i < demanda.length; i++) total += demanda[i][j];

            int s = (int) Math.round(total * ratios[j]);

            if (total > 0) s = Math.max(1, Math.min(s, total));
            stock[j] = s;
        }
        return stock;
    }
}
