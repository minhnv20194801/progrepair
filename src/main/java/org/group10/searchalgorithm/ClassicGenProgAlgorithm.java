package org.group10.searchalgorithm;

import org.group10.program.Program;
import org.group10.selection.Selection;
import org.group10.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * An implementation based on classic GenProg search algorithm for automated program repair. <br>
 *
 * The algorithm stops when either a program reaches maximum fitness (all tests pass) or
 * the maximum number of generations is reached. <br>
 *
 * NOTE: a notable difference from classic GenProg search algorithm is that non-compilable
 * crossover/mutation results are throw immediately without getting chance in the next
 * generation
 *
 */
public class ClassicGenProgAlgorithm implements SearchAlgorithm<Program> {
    private final int populationSize;
    private final int maxGeneration;
    private final double mutationWeight;
    private final Selection<Program> selector;

    /**
     * Constructs a ClassicGenProgAlgorithm with the given parameters. <br>
     *
     * Default values are used if the provided values are non-positive:
     * <ul>
     *     <li>populationSize: 40</li>
     *     <li>maxGeneration: 20</li>
     *     <li>mutationWeight: 0.06</li>
     * </ul>
     *
     * @param populationSize number of programs in each generation
     * @param maxGeneration maximum number of generations to evolve
     * @param mutationWeight probability of mutating an individual program
     * @param selector selection strategy used to pick parents for crossover
     */
    public ClassicGenProgAlgorithm(final int populationSize, final int maxGeneration, final double mutationWeight, final Selection<Program> selector) {
        this.populationSize = (populationSize > 0) ? populationSize : 40;
        this.maxGeneration = (maxGeneration > 0) ? maxGeneration : 20;
        this.mutationWeight = (mutationWeight > 0) ? mutationWeight : 0.06;
        this.selector = selector;
    }

    /**
     * Initializes the population of programs for the first generation. <br>
     *
     * The initial population consists of the original program and mutated versions
     * of it until the population reaches {@link #populationSize}. <br>
     *
     * NOTE: There can be duplicate programs in the initial population, so the tool
     * does not stuck here because the degree of freedom in the mutation operation is
     * less than the {@code populationSize}.
     *
     * @param originalProgram the program to base the initial population on
     * @return a list of programs representing the initial population
     */
    private List<Program> initializePopulation(Program originalProgram) {
        List<Program> population = new ArrayList<>();
        population.add(originalProgram);
        while (population.size() < populationSize) {
            Program mutated = originalProgram.mutate();
            population.add(mutated);
        }
        return population;
    }

    /**
     * Performs the GenProg search starting from the given program. <br>
     *
     * The search stops early if a program achieves maximum fitness,
     * otherwise it returns the best program after reaching {@link #maxGeneration}.
     *
     * @param startPoint the initial program to start the search
     * @return the program with the highest fitness found during the search
     */
    @Override
    public Program search(Program startPoint) {
        System.out.println("*********************************************************");
        System.out.println("Start searching with settings:");
        System.out.println("Population size: " + populationSize);
        System.out.println("Max generation: " + maxGeneration);
        System.out.println("Mutation weight: " + mutationWeight);
        System.out.println("Selector: " + selector);
        System.out.println("Mutation: " + startPoint.getMutator());
        System.out.println("Crossover: " + startPoint.getCrossover());
        System.out.println("Fault Localization: " + startPoint.getSuspiciousCalculator());
        System.out.println("Fitness Function: : " + startPoint.getFitnessFunction());
        System.out.println("*********************************************************");

        System.out.println("Initializing the population");
        List<Program> population = initializePopulation(startPoint);
        System.out.println("Finish initialize population");

        for (Program prog : population) {
            if (prog.isMaxFitness()) {
                System.out.println("Patches Found!!!");
                return prog;
            }
        }

        Program bestProgram = startPoint;
        for (int i = 0; i < maxGeneration; i++) {
            System.out.println("Start generation #" + i);

            population = population.stream()
                    .filter(p -> !p.isNotCompilable())
                    .toList();

            List<Program> newPopulation = new ArrayList<>();
            System.out.println("Start crossover");
            while (newPopulation.size() < populationSize) {
                List<Program> tmpPopulation = new ArrayList<>(population);
                Program parent1 = selector.select(tmpPopulation);
                // if parent 1 the only one left then we should not remove it
                if (tmpPopulation.size() > 1) {
                    tmpPopulation.remove(parent1);
                }
                Program parent2 = selector.select(tmpPopulation);
                tmpPopulation.remove(parent2);
                while (parent1.equals(parent2) && !tmpPopulation.isEmpty()) {
                    parent2 = selector.select(tmpPopulation);
                    tmpPopulation.remove(parent2);
                }
                Map.Entry<Program, Program> offsprings = parent1.crossover(parent2);
                Program child1 = offsprings.getKey();
                Program child2 = offsprings.getValue();

                newPopulation.add(parent1);
                newPopulation.add(parent2);
                if (!child1.isNotCompilable()) {
                    newPopulation.add(child1);
                }
                if (!child2.isNotCompilable()) {
                    newPopulation.add(child2);
                }
            }
            System.out.println("Finish crossover");

            population = new ArrayList<>();
            System.out.println("Start mutation");
            for (Program prog : newPopulation) {
                if (Randomness.getRandom().nextDouble() < mutationWeight) {
                    Program mutated = prog.mutate();
                    int maxTries = 100;
                    int tryCount = 0;
                    // In case none of mutation can be compiled
                    while (mutated.isNotCompilable() && tryCount < maxTries) {
                        mutated = prog.mutate();
                        tryCount++;
                    }
                    population.add(mutated);
                } else {
                    population.add(prog);
                }
            }
            System.out.println("Finish mutation");
            double populationBestFitness = 0.0;
            for (Program prog : population) {
                if (prog.isMaxFitness()) {
                    System.out.println("Patches Found on generation #" + i + "!!!");
                    return prog;
                }
                double fitness = prog.getFitness();
                if (fitness > populationBestFitness) {
                    populationBestFitness = fitness;
                }
                if (fitness > bestProgram.getFitness()) {
                    bestProgram = prog;
                }
            }
            System.out.println("Population #" + i + " best fitness: " + populationBestFitness);
        }

        System.out.println("Max generation reached, return with best patch");
        return bestProgram;
    }
}
