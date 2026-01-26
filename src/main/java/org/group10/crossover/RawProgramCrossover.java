package org.group10.crossover;

import org.group10.program.Program;
import org.group10.utils.Randomness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A crossover operator that performs a single-point crossover directly on the
 * raw source code lines of two {@link Program} instances. The other components
 * of the {@link Program} like mutator, fitness function, test suite, etc., are
 * preserved from the respective parent. <br>
 *
 * It called RawProgramCrossover because the class only attempt to perform crossover
 * between two {@link Program} on raw code-line level.
 */
public class RawProgramCrossover implements Crossover<Program> {
    /**
     * Performs a single-point crossover between two parent programs. <br>
     *
     * A random crossover point is chosen within the bounds of the shorter
     * parent program. Lines before this index are copied from each parent
     * into the corresponding child, while the remaining lines are swapped
     * between the parents.
     *
     * @param parent1 the first parent program
     * @param parent2 the second parent program
     * @return a {@link Map.Entry} containing the two newly created child programs
     */
    @Override
    public Map.Entry<Program, Program> crossover(Program parent1, Program parent2) {
        List<String> parentCodes1 = parent1.getCodes().stream()
                .filter(line -> line != null && !line.isBlank())
                .toList();
        List<String> parentCodes2 = parent2.getCodes().stream()
                .filter(line -> line != null && !line.isBlank())
                .toList();

        int minSize = Math.min(parentCodes1.size(), parentCodes2.size());
        List<String> childCodes1 = new ArrayList<>();
        List<String> childCodes2 = new ArrayList<>();
        int randomIndex = Randomness.getRandom().nextInt(minSize);

        Iterator<String> iterator1 = parentCodes1.iterator();
        Iterator<String> iterator2 = parentCodes2.iterator();
        for (int i = 0; i < randomIndex; i++) {
            childCodes1.add(iterator1.next());
            childCodes2.add(iterator2.next());
        }
        while (iterator1.hasNext()) {
            childCodes2.add(iterator1.next());
        }
        while (iterator2.hasNext()) {
            childCodes1.add(iterator2.next());
        }
        Program child1 = new Program(parent1.getClassName(), childCodes1, parent1.getTestSuite(), parent1.getMutator(), parent1.getCrossover(), parent1.getSuspiciousCalculator(), parent1.getFitnessFunction());
        Program child2 = new Program(parent2.getClassName(), childCodes2, parent2.getTestSuite(), parent2.getMutator(), parent2.getCrossover(), parent2.getSuspiciousCalculator(), parent2.getFitnessFunction());

        return Map.entry(child1, child2);
    }

    @Override
    public String toString() {
        return "RawProgramCrossover";
    }
}
