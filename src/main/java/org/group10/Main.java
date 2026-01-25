package org.group10;


import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.group10.cli.BenchmarkCommand;
import org.group10.cli.RepairCommand;
import org.group10.cli.TestCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "progrep",
        mixinStandardHelpOptions = true,
        version = "progrep_group10 1.0",
        description = "Automatic program repair and analysis tool",
        subcommands = {
                RepairCommand.class,
                TestCommand.class,
                BenchmarkCommand.class
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