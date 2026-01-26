package org.group10.selection;

import org.group10.program.Program;
import org.group10.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implements a binary tournament selection strategy for selecting a {@link Program}
 * from a population. <br>
 *
 * Two programs are randomly selected from the population, and the one with the higher
 * fitness is returned as the selected parent. If the population has only one program,
 * that program is returned. Both selected programs are cloned before returning to avoid
 * modifying the original population.
 */
public class ProgramBinaryTournamentSlection implements Selection<Program> {
    /**
     * Selects a program from the given population using binary tournament selection.
     *
     * @param population the list of programs to select from
     * @return the program with the higher fitness among two randomly chosen programs (cloned)
     * @throws NullPointerException if the population is {@code null}
     * @throws NoSuchElementException if the population is empty
     */
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

    @Override
    public String toString() {
        return "BinaryTournamentSelection";
    }
}
