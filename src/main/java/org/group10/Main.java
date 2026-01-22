package org.group10;


import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.group10.crossover.ASTProgramCrossover;
import org.group10.crossover.RawProgramCrossover;
import org.group10.fitness.WeightedFitnessFunction;
import org.group10.mutator.ClassicGenProgMutator;
import org.group10.program.Program;
import org.group10.selection.ProgramBinaryTournamentSlection;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.TarantulaSuspiciousCalculator;
import org.group10.utils.Randomness;
import org.group10.utils.astvisitor.ScopeVisitor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    private static List<Program> initializePopulation(Program originalProgram, int populationSize) {
        List<Program> population = new ArrayList<>();
        population.add(originalProgram);
        while (population.size() < populationSize) {
            Program mutated = originalProgram.mutate();
            population.add(mutated);
        }
        return population;
    }

    public static void main(String[] args) {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_18);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        config.setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(config);

        double mutationWeight = 0.06;
//        double mutationWeight = 1;
        int populationSize = 40;
        int maxGenerations = 20;
        try {
            WeightedFitnessFunction fitnessFunction = new WeightedFitnessFunction(1, 10);
            Program program = new Program("./benchmark/Shop_buggy/", "Shop", new ClassicGenProgMutator(), new RawProgramCrossover() , new OchiaiSuspiciousCalculator(), fitnessFunction);
////            Program program = new Program("./benchmark/IntCalculator_buggy/", "IntCalculator", new ClassicGenProgMutator(), new RawProgramCrossover(), new OchiaiSuspiciousCalculator(), fitnessFunction);
            Program targetProgram = new Program("./benchmark/Shop_fixed/", "Shop", new ClassicGenProgMutator(), new RawProgramCrossover(), new TarantulaSuspiciousCalculator(), fitnessFunction);
            try {
                program.executeTestSuiteWithLog();
                targetProgram.executeTestSuiteWithLog();
            } catch (Exception e) {
                return;
            }
//
            fitnessFunction.setOriginalProgram(program);
//
            Program fixedProgram = null;
            Program bestProgram = program;
            long start = System.nanoTime();

            List<Program> population = initializePopulation(program, populationSize);
//            System.out.println("Contain?: " + population.contains(targetProgram));
//            while (population.contains(targetProgram)) {
//                population.remove(targetProgram);
//            }
            ProgramBinaryTournamentSlection selector = new ProgramBinaryTournamentSlection();
            int i = 0;
            while (true) {
                System.out.println("Start generation #" + i);
                for (Program prog: population) {
                    if (prog.isMaxFitness()) {
                        fixedProgram = prog;
                        break;
                    }
                }
                System.out.println("Finish check max fitness");

                if (fixedProgram != null) {
                    break;
                }

                population = population.stream()
                        .filter(p -> p.getFitness() != 0)
                        .toList();
//                if (population.getFirst().getFitness() > bestProgram.getFitness()) {
//                    bestProgram = population.get(0);
//                    System.out.println(bestProgram);
//                    System.out.println("New best patch found with fitness: " + bestProgram.getFitness());
//                }
//                System.out.println("Finish filter and sort population");
//                double bestFitness = bestProgram.getFitness();
//                if (population.stream().anyMatch(p -> p.getFitness() > bestFitness)) {
//                    List<Program> better = population.stream().filter(p -> p.getFitness() > bestFitness).toList();
//                    bestProgram = better.get(0);
//                    System.out.println("New best patch found with fitness: " + bestProgram.getFitness());
//                }
                List<Program> newPopulation = new ArrayList<>();
//                newPopulation.add(bestProgram);
                while (newPopulation.size() < populationSize) {
                    Program parent1 = selector.select(population);
                    Program parent2 = selector.select(population);
                    while (parent1.equals(parent2)) {
                        parent2 = selector.select(population);
                    }
                    Map.Entry<Program, Program> offsprings = parent1.crossover(parent2);
                    Program child1 = offsprings.getKey();
                    Program child2 = offsprings.getValue();

//                    if (!newPopulation.contains(parent1)) {
                        newPopulation.add(parent1);
//                    }
//                    if (!newPopulation.contains(parent2)) {
                        newPopulation.add(parent2);
//                    }
//                    if (!newPopulation.contains(child1)) {
                    if (!child1.isNotCompilable()) {
                        newPopulation.add(child1);
                    }
//                    }
//                    if (!newPopulation.contains(child2)) {
                    if (!child2.isNotCompilable()) {
                        newPopulation.add(child2);
                    }
//                    }
                }
                System.out.println("Finish crossover");

                newPopulation = newPopulation.stream()
                        .filter(p -> p.getFitness() != 0)
                        .toList();
                System.out.println("Compilable genome count before mutation: " + newPopulation.stream().filter(p -> p.getFitness() != 0).count());

                population = new ArrayList<>();
                for (Program prog: newPopulation) {
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
                for (Program prog: population) {
                    double fitness = prog.getFitness();
                    if (fitness > populationBestFitness) {
                        populationBestFitness = fitness;
                    }
                    if (fitness > bestProgram.getFitness()) {
                        System.out.println(prog);
                        System.out.println("New best found!!!");
                        bestProgram = prog;
                    }
                }
                i++;
                System.out.println("Still contain original program: " + population.contains(program));
                System.out.println("Current population size: " + population.size());
                System.out.println("Compilable genome count: " + population.stream().filter(p -> p.getFitness() != 0).count());
                System.out.println("This population best fitness: " + populationBestFitness);
            }

//            while (true) {
//                System.out.println("Start generation #" + i);
//                for (Program prog: population) {
//                    if (prog.isMaxFitness()) {
//                        fixedProgram = prog;
//                        break;
//                    }
//                }
//                System.out.println("Finish check max fitness");
//
//                if (fixedProgram != null) {
//                    break;
//                }
//
//                population = population.stream()
//                        .filter(p -> p.getFitness() != 0)
//                        .toList();
////                if (population.getFirst().getFitness() > bestProgram.getFitness()) {
////                    bestProgram = population.get(0);
////                    System.out.println(bestProgram);
////                    System.out.println("New best patch found with fitness: " + bestProgram.getFitness());
////                }
////                System.out.println("Finish filter and sort population");
////                double bestFitness = bestProgram.getFitness();
////                if (population.stream().anyMatch(p -> p.getFitness() > bestFitness)) {
////                    List<Program> better = population.stream().filter(p -> p.getFitness() > bestFitness).toList();
////                    bestProgram = better.get(0);
////                    System.out.println("New best patch found with fitness: " + bestProgram.getFitness());
////                }
//                List<Program> newPopulation = new ArrayList<>();
////                newPopulation.add(bestProgram);
//                while (newPopulation.size() < populationSize) {
//                    Program parent = selector.select(population);
//
//                    Program child = parent.mutate();
//                    while (child.isNotCompilable()) {
//                        child = parent.mutate();
//                    }
//                    newPopulation.add(parent);
//                    newPopulation.add(child);
//                }
//                System.out.println("Finish populating new population");
//
//                population = newPopulation;
////                for (Program prog: population) {
////                    if (prog.getFitness() > bestProgram.getFitness()) {
////                        System.out.println(prog);
////                        System.out.println("New best found!!!");
////                        bestProgram = prog;
////                    }
////                }
//                i++;
//                System.out.println("Still contain original program: " + population.contains(program));
//                System.out.println("Current population size: " + population.size());
//                System.out.println("Compilable genome count: " + population.stream().filter(p -> p.getFitness() != 0).count());
//            }

            if (fixedProgram != null) {
                System.out.println(fixedProgram);
                System.out.println("Patch found!!!");
            }

            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.println("Time took to find the patch is " + elapsedMs + "ms");
//            for (int i = 0; i < 10000; i++) {
//                System.out.println("Start iteration #" + i);
//                Program mutatedProgram = program.mutate();
//                if (fitnessFunction.isMax(mutatedProgram)) {
//                    System.out.println(mutatedProgram);
//                    System.out.println("Fixed found in iteration #" + i);
//                    break;
//                }
//            }
//            program = program.mutate();
//            Map.Entry<Program, Program> crossoverPair = new RawProgramCrossover().crossover(program, fixedProgram);
//            System.out.println("============================================");
//            System.out.println(program);
//            System.out.println("============================================");
//            System.out.println(crossoverPair.getKey());
//            System.out.println("============================================");
//            System.out.println(crossoverPair.getValue());
//            System.out.println("============================================");
//            System.out.println(crossoverPair.getValue().mutate());
//            System.out.println("============================================");
//            System.out.println(crossoverPair.getValue().isCompilable());
//            System.out.println(program.isCompilable());
            //            System.out.println(program);
//            try {
//                program.executeTestSuiteWithLog();
//
//                int lineNumber = 12;
//                CompilationUnit cu = StaticJavaParser.parse(program.toString());
//                ScopeVisitor visitor = new ScopeVisitor(lineNumber);
//                cu.accept(visitor, null);
//
//                Map<String, Type> varsInScope = visitor.getVariablesInScope();
//
//                System.out.println("Variables in scope at line " + lineNumber + ":");
//                varsInScope.forEach((name, type) -> System.out.println(name + " : " + type));
//
//                Type from = varsInScope.get("this.object");
//                Type to = varsInScope.get("this.counter");
//                ResolvedType fromResolved = from.resolve();
//                ResolvedType toResolved   = to.resolve();
//                System.out.println(toResolved.isAssignableBy(fromResolved));
//                System.out.println(fromResolved.isAssignableBy(toResolved));
////                Optional<Node> targetNodeOpt = cu.findAll(Node.class).stream()
////                        .filter(n -> n.getRange().map(r -> r.begin.line == lineNumber).orElse(false))
////                        .findFirst();
////
////                if (targetNodeOpt.isPresent()) {
////
////                }
////                long startTime = System.currentTimeMillis();
////                for (int i = 0; i <= 10000; i++) {
////                    Program mutated = program.mutate();
////                    mutated.executeTestSuite();
//////                    if (mutated.getTestFailedCount() == 0) {
//////                        System.out.println(mutated);
//////                        System.out.println("FIXED FOUND in iteration " + i + "!!!");
//////                    }
////                }
////                long endTime = System.currentTimeMillis();
////                System.out.println("System took " + (endTime - startTime) + "ms");
////                System.out.println(program.getTestSuccessfulCount());
////                System.out.println(program.getTestFailedCount());
//
////                System.out.println(program.getEfs());
////                System.out.println(program.getNfs());
////                System.out.println(program.getEps());
////                System.out.println(program.getNps());
////                System.out.println(mutated.isCompilable());
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Can't find the program");
        }
    }
}