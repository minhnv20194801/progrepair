package org.group10.searchalgorithm;

import org.group10.program.Program;
import org.group10.selection.Selection;
import org.group10.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassicGenProgAlgorithm implements SearchAlgorithm<Program> {
    private final int populationSize;
    private final int maxGeneration;
    private final double mutationWeight;
    private final Selection<Program> selector;

    public ClassicGenProgAlgorithm(final int populationSize, final int maxGeneration, final double mutationWeight, final Selection<Program> selector) {
        this.populationSize = (populationSize > 0) ? populationSize : 40;
        this.maxGeneration = (maxGeneration > 0) ? maxGeneration : 20;
        this.mutationWeight = (mutationWeight > 0) ? mutationWeight : 0.06;
        this.selector = selector;
    }

    private List<Program> initializePopulation(Program originalProgram) {
        List<Program> population = new ArrayList<>();
        population.add(originalProgram);
        while (population.size() < populationSize) {
            Program mutated = originalProgram.mutate();
            population.add(mutated);
        }
        return population;
    }

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
                    .filter(p -> p.getFitness() != 0)
                    .toList();

            List<Program> newPopulation = new ArrayList<>();
            System.out.println("Start crossover");
            while (newPopulation.size() < populationSize) {
                List<Program> tmpPopulation = new ArrayList<>(population);
                Program parent1 = selector.select(tmpPopulation);
                tmpPopulation.remove(parent1);
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
                    while (mutated.isNotCompilable()) {
                        mutated = prog.mutate();
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
