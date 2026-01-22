package org.group10.selection;

import org.group10.program.Program;
import org.group10.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ProgramBinaryTournamentSlection implements Selection<Program> {
    @Override
    public Program select(List<Program> population) throws NullPointerException, NoSuchElementException {
        if (population == null) {
            throw new NullPointerException();
        }
        if (population.isEmpty()) {
            throw new NoSuchElementException();
        }
        if (population.size() == 1) {
            return population.getFirst();
        }

        List<Program> copy = new ArrayList<>(population);
        Program parent1 = copy.get(Randomness.getRandom().nextInt(copy.size()));
        copy.remove(parent1);
        Program parent2 = copy.get(Randomness.getRandom().nextInt(copy.size()));

        if (parent1.getFitness() > parent2.getFitness()) {
            return parent1.clone();
        }
        return parent2.clone();
    }
}
