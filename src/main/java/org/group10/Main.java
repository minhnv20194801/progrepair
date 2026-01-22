package org.group10;


import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.group10.cli.RepairCommand;
import org.group10.cli.TestCommand;
import org.group10.crossover.Crossover;
import org.group10.crossover.RawProgramCrossover;
import org.group10.fitness.FitnessFunction;
import org.group10.fitness.WeightedFitnessFunction;
import org.group10.mutator.ClassicGenProgMutator;
import org.group10.mutator.Mutator;
import org.group10.program.Program;
import org.group10.searchalgorithm.ClassicGenProgAlgorithm;
import org.group10.searchalgorithm.SearchAlgorithm;
import org.group10.selection.ProgramBinaryTournamentSlection;
import org.group10.selection.Selection;
import org.group10.suspiciouscalculator.OchiaiSuspiciousCalculator;
import org.group10.suspiciouscalculator.SuspiciousCalculator;
import org.group10.suspiciouscalculator.TarantulaSuspiciousCalculator;
import org.group10.utils.Randomness;
import picocli.CommandLine;

import java.io.IOException;
import java.util.*;

@CommandLine.Command(
        name = "progrep",
        mixinStandardHelpOptions = true,
        version = "progrep_group10 1.0",
        description = "Automatic program repair and analysis tool",
        subcommands = {
                RepairCommand.class,
                TestCommand.class
        }
)
public class Main {
    private static void setupJavaParser() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_18);
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        config.setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(config);
    }

    public static void main(String[] args) {
        setupJavaParser();
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}