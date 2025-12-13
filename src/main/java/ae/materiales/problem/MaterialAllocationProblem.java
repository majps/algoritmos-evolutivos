package ae.materiales.problem;

import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.ArrayList;
import java.util.List;

public class MaterialAllocationProblem extends AbstractIntegerProblem {

    private int nFamilias;
    private int nMateriales;

    private int[][] demanda;   // d_ij
    private int[] stock;      // s_j
    private double[] peso;  // p_j
    private double capacidad;  // C

    public MaterialAllocationProblem(
            int nFamilias,
            int nMateriales,
            int[][] demanda,
            int[] stock,
            double[] peso,
            double capacidad) {

        this.nFamilias = nFamilias;
        this.nMateriales = nMateriales;
        this.demanda = demanda;
        this.stock = stock;
        this.peso = peso;
        this.capacidad = capacidad;

        int numberOfVariables = nFamilias * nMateriales;

        //bounds
        List<Integer> lower = new ArrayList<>(numberOfVariables);
        List<Integer> upper = new ArrayList<>(numberOfVariables);

        for (int i = 0; i < nFamilias; i++) {
            for (int j = 0; j < nMateriales; j++) {
                lower.add(0);
                upper.add(demanda[i][j]); // no puede asignar más que la demanda
            }
        }


        variableBounds(lower, upper);

        // cantidad de objetivos / restricciones y nombre
        numberOfObjectives(2);        // f1 y f2
        numberOfConstraints(0);       // por ahora sin constraints explícitas
        name("MaterialAllocationProblem");
    }

    private int index(int i, int j) {
        return i * nMateriales + j;
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
    	int[][] x = new int[nFamilias][nMateriales];
    	List<Integer> vars = solution.variables();
    	
    	//armamos nuevamente la matriz a partir del vector de la solucion
    	for (int i = 0; i < nFamilias; i++) {
    		for (int j = 0; j < nMateriales; j++) {
    			int k = index(i, j); //k es la posicion en el vector
    			x[i][j] = vars.get(k);
    		}
    	}
    	
    	//repararStock(x);
    	
    	//copiamos los valores de x reparado hacia la solution 
    	for (int i = 0; i < nFamilias; i++) {
    	    for (int j = 0; j < nMateriales; j++) {
    	        int k = index(i, j);
    	        vars.set(k, x[i][j]);
    	    }
    	}
    	
    	//calculo ui para cada familia
    	double[] u = new double[nFamilias];
    	
    	for (int i = 0; i < nFamilias; i++) { //voy recorriendo cada familia
    		double sumaRatios = 0.0;
    		int cantidadMaterialesPedidos = 0;
    		
    		for (int j = 0; j < nMateriales; j++) {
    			int demandaIJ = demanda[i][j];
    			
    			if (demandaIJ>0) {
    				cantidadMaterialesPedidos++;
    				
    				double ratio = (double) x[i][j] / (double) demandaIJ;
    				if (ratio>1.0) {
    					ratio = 1.0;
    				} else if (ratio<0.0) {
    					ratio = 0.0; //por si alguna mutacion deja cosas que no
    				}
    				
    				sumaRatios+=ratio;
    			}
    		}
    		
    		if (cantidadMaterialesPedidos == 0) {
    			u[i] = 0.0;
    		}else {
    			u[i] = sumaRatios / cantidadMaterialesPedidos;
    		}
    	}
    	
    	//promedio de las satisfacciones, f1
    	double f1 = 0.0;
    	for (int i = 0; i < nFamilias; i++) {
    		f1 += u[i];
    	}
    	f1 = f1/nFamilias;
    	
    	//varianza de ui, var en el informe
    	double var = 0.0;
    	for (int i = 0; i < nFamilias; i++) {
    		double diff = u[i] - f1;
    		var += diff * diff;
    	}
    	var = var/nFamilias;
    	
    	//Vmax 
    	double vmax = 0.0;
    	if (nFamilias>1) {
    		vmax = (double) (nFamilias-1) / (double) nFamilias;
    	}
    	
    	//f2 = 1 - var /Vmax
    	double f2;
    	if (vmax == 0.0) {
    		//si hay unica familia la equidad es 1;
    		f2 = 1.0;
    	} else {
    		f2 = 1.0 - var/vmax;
    	}
    	
    	if (f2 < 0.0) f2 = 0.0;
    	if (f2 > 1.0) f2 = 1.0;
    	
    	//ver si queremos agregarle condicion para que no siempre repare / penalice
    	
    	double W = 0.0;
    	for (int j = 0; j < nMateriales; j++) {
    		for (int i = 0; i < nFamilias; i++) {
    			W += peso[j]* x[i][j];
    		}
    	}
    	
    	double exceso = 0.0;
    	if (W > capacidad) {
    		exceso = (W-capacidad)/capacidad;
    		if(exceso < 0) {
    			exceso = 0;
    		}
    	}
    	
    	//elegir el parametro lambda de penalizacion
    	double lambda = 0.1;
    	
    	double f1Penalizada = f1 - lambda * exceso;
    	double f2Penalizada = f2 - lambda * exceso;

    	//para que el algoritmo maximice f1 y f2
    	solution.objectives()[0] = -f1Penalizada;
    	solution.objectives()[1] = -f2Penalizada;
    	
        return solution;
    }

	private void repararStock(int[][] x) {
		for (int j = 0; j < nMateriales; j++) {
			int sumaColumna = 0;
			for (int i = 0; i< nFamilias; i++) {
				sumaColumna += x[i][j];
			}
			
			int stockJ = stock[j];
			
			if (sumaColumna > stockJ && sumaColumna > 0) {
				double factor = (double) stockJ / (double) sumaColumna;
			
				for (int i = 0; i < nFamilias; i++) {
					int nuevoValor = (int) Math.floor(x[i][j] * factor);
					
					if (nuevoValor > demanda[i][j]) {
						nuevoValor = demanda[i][j];
					} else if (nuevoValor < 0) {
						nuevoValor = 0;
					}
					
					x[i][j] = nuevoValor;
				}
			}	
		}
		
	}
}
