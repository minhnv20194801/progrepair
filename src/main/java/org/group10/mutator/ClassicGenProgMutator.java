package org.group10.mutator;

import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.group10.program.Program;
import org.group10.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassicGenProgMutator implements Mutator<Program> {
    private boolean canGetFixFromDifferentClass = false;
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

        // Incase method or constructor is empty, add an empty statement inside it
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            method.getBody().ifPresent(body -> {
                if (body.isEmpty()) {
                    EmptyStmt empty = new EmptyStmt();
                    body.getRange().ifPresent(r ->
                        empty.setRange(new Range(r.end, r.end))
                    );
                    body.addStatement(empty);
                }
            });
        });
        cu.findAll(ConstructorDeclaration.class).forEach(constructor -> {
            BlockStmt body = constructor.getBody();
            if (body.isEmpty()) {
                EmptyStmt empty = new EmptyStmt();
                body.getRange().ifPresent(r ->
                    empty.setRange(new Range(r.end, r.end))
                );
                body.addStatement(empty);
            }
        });

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

        switch (choice) {
            case 0 -> mutatedCu = insert(cu, targetLine);
            case 1 -> mutatedCu = delete(cu, targetLine);
            case 2 -> mutatedCu = swap(cu, targetLine);
            default -> mutatedCu = cu;
        }
        // Clean all inserted EmptyStmt
        mutatedCu.findAll(EmptyStmt.class).forEach(Node::remove);

        return new Program(program.getClassName(), mutatedCu.toString().lines().toList(), program.getTestSuite(), program.getMutator(), program.getCrossover(), program.getSuspiciousCalculator(), program.getFitnessFunction());
    }

    private CompilationUnit parseAST(Program program) {
        return StaticJavaParser.parse(program.toString());
    }

    private CompilationUnit insert(CompilationUnit cu, int lineNumber) {
        // Find the first node that starts at insertTarget
        Optional<Node> targetNodeOpt = cu.findAll(Node.class).stream()
                .filter(n -> n.getRange().map(r -> r.begin.line == lineNumber).orElse(false))
                .findFirst();

        if (targetNodeOpt.isPresent()) {
            Node targetNode = targetNodeOpt.get();
            Node insertTarget = getRandomStmt(cu, targetNode);
            // if we can't find insert target then do nothing
            if (insertTarget == null) {
                return cu;
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

        return cu;
    }

    private CompilationUnit delete(CompilationUnit cu, int lineNumber) {
//        System.out.println("Delete line " + lineNumber);
        cu.findAll(Node.class).stream()
                .filter(n -> n.getRange().map(r -> r.begin.line == lineNumber).orElse(false))
                .findFirst()
                .ifPresent(Node::remove);

        return cu;
    }

    private CompilationUnit swap(CompilationUnit cu, int lineNumber) {
        Optional<Node> targetNodeOpt = cu.findAll(Node.class).stream()
                .filter(n -> n.getRange().map(r -> r.begin.line == lineNumber).orElse(false))
                .findFirst();

        if (targetNodeOpt.isPresent()) {
            Node targetNode = targetNodeOpt.get();
            Node swappedNode = getRandomStmt(cu, targetNode);
            // if we can't find swap target then do nothing
            if (swappedNode == null) {
                return cu;
            }
            Node tmpNode = targetNode.clone();
//            System.out.println("Swap " + swappedNode.getRange().get().begin.line + " with " + lineNumber);

            try {
                targetNode.replace(swappedNode.clone());
                swappedNode.replace(tmpNode);
            } catch (Exception e) {
                // If we can't replace then return the same program
                return cu;
            }
        }

        return cu;
    }

    private Node getRandomStmt(CompilationUnit cu, Node targetNode) {
        ClassOrInterfaceDeclaration targetClass =
                targetNode.findAncestor(ClassOrInterfaceDeclaration.class).get();

        List<Statement> statements = targetClass.findAll(Statement.class)
                .stream()
                .filter(s -> !(s instanceof BlockStmt) && !(s.equals(targetNode) && !(s instanceof EmptyStmt)))
                .toList();

        if (statements.isEmpty()) {
            return null;
        }

        int idx = Randomness.getRandom().nextInt(statements.size());

        return statements.get(idx);
    }

    @Override
    public String toString() {
        return "ClassicGenProgMutator get fixes from different classes: " + canGetFixFromDifferentClass;
    }

    public void setCanGetFixFromDifferentClass(boolean canGetFixFromDifferentClass) {
        this.canGetFixFromDifferentClass = canGetFixFromDifferentClass;
    }
}
