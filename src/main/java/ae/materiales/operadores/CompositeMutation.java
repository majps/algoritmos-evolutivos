package ae.materiales.operadores;

import java.util.List;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

@SuppressWarnings("serial")
public class CompositeMutation  implements MutationOperator<IntegerSolution> {

    private final List<MutationOperator<IntegerSolution>> mutationOperators;

    public CompositeMutation(List<MutationOperator<IntegerSolution>> mutationOperators) {
        this.mutationOperators = mutationOperators;
    }

    @Override
    public IntegerSolution execute(IntegerSolution solution) {
        // Aplica cada operador de mutación en cadena hasta que muta una vez
    	boolean mutado = false;
        IntegerSolution current = solution;
        
        for (MutationOperator<IntegerSolution> op : mutationOperators) {
        	if(mutado)
        		return current;
        	IntegerSolution mutated = op.execute(current);

            // Detectamos si hubo cambio
            if (!isSameSolution(current, mutated)) {
                current = mutated;
                mutado = true;
            }
           
            
        }
        return current;
    }
    
    //auxiliar para ver si ya mutó o no---------------------
    private boolean isSameSolution(IntegerSolution a, IntegerSolution b) {
        if (a == b) return true;
        for (int i = 0; i < a.variables().size(); i++) {
            if (!a.variables().get(i).equals(b.variables().get(i)))
                return false;
        }
        return true;
    }
    //------------------------------------------
	@Override
	public double mutationProbability() {
		// tiene 0 porque la probabilidad depende de las sub-mutaciones
		return 0;
	}

	
}
