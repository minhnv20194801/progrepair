package org.group10.crossover;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.Statement;
import org.group10.program.Program;
import org.group10.utils.Randomness;

import java.util.List;
import java.util.Map;

public class ASTProgramCrossover implements Crossover<Program> {
    public Map.Entry<Program, Program> crossover(Program parent1, Program parent2) {
        try {
            CompilationUnit ast1 = StaticJavaParser.parse(parent1.toString());
            CompilationUnit ast2 = StaticJavaParser.parse(parent2.toString());

            List<Statement> stmts1 = ast1.findAll(Statement.class);
            List<Statement> stmts2 = ast2.findAll(Statement.class);

            Node crossoverPoint1 = stmts1.get(Randomness.getRandom().nextInt(stmts1.size()));
            Node crossoverPoint2 = stmts2.get(Randomness.getRandom().nextInt(stmts2.size()));
            Node outerNode1 = crossoverPoint1.getParentNode().orElse(null);
            Node outerNode2 = crossoverPoint2.getParentNode().orElse(null);
            while (!outerNode1.getClass().equals(outerNode2.getClass())) {
                crossoverPoint1 = stmts1.get(Randomness.getRandom().nextInt(stmts1.size()));
                crossoverPoint2 = stmts2.get(Randomness.getRandom().nextInt(stmts2.size()));
                outerNode1 = crossoverPoint1.getParentNode().orElse(null);
                outerNode2 = crossoverPoint2.getParentNode().orElse(null);
            }

            Node temp = crossoverPoint1.clone();
            crossoverPoint1.replace(crossoverPoint2.clone());
            crossoverPoint2.replace(temp);

            Program child1 = new Program(parent1.getClassName(), ast1.toString().lines().toList(), parent1.getTestSuite(), parent1.getMutator(), parent1.getCrossover(), parent1.getSuspiciousCalculator(), parent1.getFitnessFunction());
            Program child2 = new Program(parent2.getClassName(), ast2.toString().lines().toList(), parent2.getTestSuite(), parent2.getMutator(), parent2.getCrossover(), parent2.getSuspiciousCalculator(), parent2.getFitnessFunction());

            return Map.entry(child1, child2);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.entry(parent1, parent2); // fallback
        }
    }

    @Override
    public String toString() {
        return "ASTBasedProgramCrossover";
    }
}
