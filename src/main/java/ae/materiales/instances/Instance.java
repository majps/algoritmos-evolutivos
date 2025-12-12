package ae.materiales.instances;

public class Instance {
	  public final int nFamilias;
	  public final int nMateriales;
	  public final int[][] demanda;
	  public final int[] stock;
	  public final double[] peso;
	  public final double capacidad;

	  public Instance(int nFamilias,
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
	    }
}
