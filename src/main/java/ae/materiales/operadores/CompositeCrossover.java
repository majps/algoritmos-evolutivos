package ae.materiales.operadores;


import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class CompositeCrossover
        implements CrossoverOperator<IntegerSolution> {

    private final List<CrossoverOperator<IntegerSolution>> crossovers;
    private final Random random = new Random();

    public CompositeCrossover(
            List<CrossoverOperator<IntegerSolution>> crossovers
    ) {
        if (crossovers == null || crossovers.isEmpty())
            throw new IllegalArgumentException("Lista de crossovers vacía");

        int parents = crossovers.get(0).numberOfRequiredParents();
        for (CrossoverOperator<IntegerSolution> c : crossovers) {
            if (c.numberOfRequiredParents() != parents)
                throw new IllegalArgumentException("Todos los crossovers deben usar el mismo número de padres");
        }

        this.crossovers = crossovers;
    }

    @Override
    public List<IntegerSolution> execute(List<IntegerSolution> parents) {
        int idx = random.nextInt(crossovers.size());
        return crossovers.get(idx).execute(parents);
    }

    @Override
    public int numberOfRequiredParents() {
        return crossovers.get(0).numberOfRequiredParents();
    }

    @Override
    public int numberOfGeneratedChildren() {
        return crossovers.get(0).numberOfGeneratedChildren();
    }

    @Override
    public double crossoverProbability() {
        // no se usa acá, la maneja cada crossover interno
        return 1.0;
    }
}
