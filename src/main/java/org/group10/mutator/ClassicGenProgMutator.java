package org.group10.mutator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.group10.program.Program;
import org.group10.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassicGenProgMutator implements Mutator<Program> {
    @Override
    public Program mutate(Program program) {
        if (program.isNotCompilable()) {
            return program;
        }
        CompilationUnit cu;
        // try to parse the program and return the original program if parsing is not possible
        try {
            cu = parseAST(program);
        } catch (Exception e) {
            return program;
        }
        Map<Integer, Double> suspiciousScores = program.getSuspiciousScore();

        List<Statement> stmtLst = cu.findAll(Statement.class).stream().filter(stmt -> !(stmt instanceof BlockStmt)).toList();
        List<Integer> suspiciousLineNumbers = new ArrayList<>(suspiciousScores.keySet());

        // filter only suspicious statement (because the class declaration will always get assigned with some suspicion)
        for (int lineNumber : suspiciousLineNumbers) {
            if (stmtLst.stream().noneMatch(stmt -> stmt.getRange().get().begin.line == lineNumber)) {
                suspiciousScores.remove(lineNumber);
            }
        }

        // if there are no suspicions (on statement level) then I guess there are no need for mutate
        if (suspiciousScores.isEmpty()) {
            return program;
        }
        int targetLine = Randomness.getRandomIntegerWithWeighted(suspiciousScores);
        CompilationUnit mutatedCu;
        int choice = Randomness.getRandom().nextInt(3);
//        switch (choice) {
//            case 0 -> System.out.println("Insert");
//            case 1 -> System.out.println("Replace");
//            case 2 -> System.out.println("Delete");
//            default -> mutatedCu = cu;
//        }
        switch (choice) {
            case 0 -> mutatedCu = insert(cu, targetLine);
            case 1 -> mutatedCu = swap(cu, targetLine);
            case 2 -> mutatedCu = delete(cu, targetLine);
            default -> mutatedCu = cu;
        }
        return new Program(program.getClassName(), mutatedCu.toString().lines().toList(), program.getTestSuite(), program.getMutator(), program.getCrossover(), program.getSuspiciousCalculator(), program.getFitnessFunction());
    }

    private CompilationUnit parseAST(Program program) {
        return StaticJavaParser.parse(program.toString());
    }

    private CompilationUnit insert(CompilationUnit cu, int lineNumber) {
        CompilationUnit result = cu.clone();

        // Find the first node that starts at insertTarget
        Optional<Node> targetNodeOpt = result.findAll(Node.class).stream()
                .filter(n -> n.getRange().map(r -> r.begin.line == lineNumber).orElse(false))
                .findFirst();

        if (targetNodeOpt.isPresent()) {
            Node targetNode = targetNodeOpt.get();
            Node insertTarget = getRandomStmt(result, targetNode);
            // if we can't find insert target then do nothing
            if (insertTarget == null) {
                return result;
            }
//            System.out.println("Insert " + insertTarget.getRange().get().begin.line + " before " + lineNumber);
            Optional<Node> parentOpt = targetNode.getParentNode();
            parentOpt.ifPresent(parent -> {
                if (parent instanceof NodeWithStatements nodeWithStatements) {
                    NodeList stmts = nodeWithStatements.getStatements();
                    int idx = stmts.indexOf(targetNode);
                    if (idx != -1) {
                        stmts.add(idx, (Statement) insertTarget.clone());
                    }
                }
            });
        }

        return result;
    }

    private CompilationUnit delete(CompilationUnit cu, int lineNumber) {
        CompilationUnit result = cu.clone();

//        System.out.println("Delete line " + lineNumber);
        result.findAll(Node.class).stream()
                .filter(n -> n.getRange().map(r -> r.begin.line == lineNumber).orElse(false))
                .findFirst()
                .ifPresent(Node::remove);

        return result;
    }

    private CompilationUnit swap(CompilationUnit cu, int lineNumber) {
        CompilationUnit result = cu.clone();

        Optional<Node> targetNodeOpt = result.findAll(Node.class).stream()
                .filter(n -> n.getRange().map(r -> r.begin.line == lineNumber).orElse(false))
                .findFirst();

        if (targetNodeOpt.isPresent()) {
            Node targetNode = targetNodeOpt.get();
            Node swappedNode = getRandomStmt(result, targetNode);
            // if we can't find swap target then do nothing
            if (swappedNode == null) {
                return result;
            }
            Node tmpNode = targetNode.clone();
//            System.out.println("Swap " + swappedNode.getRange().get().begin.line + " with " + lineNumber);

            try {
                targetNode.replace(swappedNode.clone());
                swappedNode.replace(tmpNode);
            } catch (Exception e) {
                // If we can't replace then try again
                return swap(cu, lineNumber);
            }
        }

        return result;
    }

    private Node getRandomStmt(CompilationUnit cu, Node targetNode) {
        ClassOrInterfaceDeclaration targetClass =
                targetNode.findAncestor(ClassOrInterfaceDeclaration.class).get();

        List<Statement> statements = targetClass.findAll(Statement.class)
                .stream()
                .filter(s -> !(s instanceof BlockStmt) && !(s.equals(targetNode)))
                .toList();

        if (statements.isEmpty()) {
            return null;
        }

        int idx = Randomness.getRandom().nextInt(statements.size());

        return statements.get(idx);
    }
}
