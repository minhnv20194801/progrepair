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

/**
 * A mutation operator based on the classic GenProg approach. <br>
 *
 * This mutator operates on the abstract syntax tree (AST) of a
 * {@link Program} and applies one of three mutation actions at evenly distributions:
 * <ul>
 *   <li><b>Insert</b>: inserts a randomly selected statement before a
 *       suspicious statement</li>
 *   <li><b>Delete</b>: removes a suspicious statement</li>
 *   <li><b>Swap</b>: swaps a suspicious statement with another randomly
 *       selected statement</li>
 * </ul>
 * <br>
 * The target statement is selected using weighted randomness based on
 * suspiciousness scores computed at the line level. <br>
 *
 * If the program is not compilable, cannot be parsed, or contains no
 * suspicious statements, the original program is returned unchanged (no mutation possible).
 */
public class ClassicGenProgMutator implements Mutator<Program> {
    /**
     * Boolean flag to determine if the donors can come from different classes.
     */
    protected boolean canGetFixFromDifferentClass = false;

    /**
     * Applies a mutation to the given program. <br>
     *
     * The program is first parsed into an AST. Suspicious statements are
     * identified using the program's suspiciousness scores, and one of the
     * mutation operations (insert, delete, or swap) is selected at random
     * distribution. <br>
     *
     * Empty statements may be temporarily inserted to allow mutation of
     * empty method or constructor bodies; these placeholders are removed
     * before returning the mutated program. <br>
     *
     * @param program the program to mutate
     * @return a mutated version of the program, or the original program if
     *         mutation is not possible
     */
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

        List<Integer> insertedEmptyStmtLines = new ArrayList<>();
        // Incase method or constructor is empty, add an empty statement inside it
        cu.findAll(MethodDeclaration.class).forEach(method -> method.getBody().ifPresent(body -> {
            if (body.isEmpty()) {
                EmptyStmt empty = new EmptyStmt();
                body.getRange().ifPresent(r -> {
                    insertedEmptyStmtLines.add(r.end.line);
                    empty.setRange(new Range(r.end, r.end));
                });
                body.addStatement(empty);
            }
        }));
        cu.findAll(ConstructorDeclaration.class).forEach(constructor -> {
            BlockStmt body = constructor.getBody();
            if (body.isEmpty()) {
                EmptyStmt empty = new EmptyStmt();
                body.getRange().ifPresent(r -> {
                    insertedEmptyStmtLines.add(r.end.line);
                    empty.setRange(new Range(r.end, r.end));
                });
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
        // 3 choices: insert, delete, swap
        int maxChoices = 3;
        // If the target line is our inserted empty statement then the
        // delete mutation will changes nothing and thus be removed
        if (insertedEmptyStmtLines.contains(targetLine)) {
            maxChoices--;
        }
        int choice = Randomness.getRandom().nextInt(maxChoices);

        switch (choice) {
            case 0 -> mutatedCu = insert(cu, targetLine);
            case 1 -> mutatedCu = swap(cu, targetLine);
            case 2 -> mutatedCu = delete(cu, targetLine);
            default -> mutatedCu = cu;
        }
        // Clean all inserted EmptyStmt
        mutatedCu.findAll(EmptyStmt.class).forEach(Node::remove);

        return new Program(program.getClassName(), mutatedCu.toString().lines().toList(), program.getTestSuite(), program.getMutator(), program.getCrossover(), program.getSuspiciousCalculator(), program.getFitnessFunction());
    }

    /**
     * Parses the given program into a {@link CompilationUnit}.
     *
     * @param program the program to parse
     * @return the parsed compilation unit
     */
    protected CompilationUnit parseAST(Program program) {
        return StaticJavaParser.parse(program.toString());
    }

    /**
     * Inserts a randomly selected statement (donor) before the statement located
     * at the given line number. <br>
     *
     * NOTE: If insert throws any exception, or no possible insert can be made,
     * then it silently return the original program.
     *
     * @param cu the compilation unit to modify
     * @param lineNumber the target line number
     * @return the modified compilation unit
     */
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

            Optional<Node> parentOpt = targetNode.getParentNode();
            parentOpt.ifPresent(parent -> {
                if (parent instanceof NodeWithStatements nodeWithStatements) {
                    NodeList<Statement> stmts = nodeWithStatements.getStatements();
                    int idx = stmts.indexOf(targetNode);
                    if (idx != -1) {
                        stmts.add(idx, (Statement) insertTarget.clone());
                    }
                }
            });
        }

        return cu;
    }

    /**
     * Deletes the statement located at the given line number.
     *
     * @param cu the compilation unit to modify
     * @param lineNumber the line number of the statement to delete
     * @return the modified compilation unit
     */
    private CompilationUnit delete(CompilationUnit cu, int lineNumber) {
        cu.findAll(Node.class).stream()
                .filter(n -> n.getRange().map(r -> r.begin.line == lineNumber).orElse(false))
                .findFirst()
                .ifPresent(Node::remove);

        return cu;
    }

    /**
     * Swaps the statement located at the given line number with another
     * randomly selected statement (donor). <br>
     *
     * NOTE: If swap throws any exception, or no possible swap can be made,
     * then it silently return the original program.
     *
     * @param cu the compilation unit to modify
     * @param lineNumber the target line number
     * @return the modified compilation unit
     */
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

    /**
     * Selects a random statement in the program. <br>
     *
     * Block statements are excluded, and the target node itself is not
     * selected. </br>
     *
     * NOTE: the field {@code canGetFixFromDifferentClass} will affect this method.
     * If it is {@code true} then the result of the method can come from different class
     * of the {@code targetNode}. <br>
     * Otherwise, the result of the method can only come from within the same class
     * as the {@code targetNode}. <br>
     *
     * @param cu the compilation unit
     * @param targetNode the node being mutated
     * @return a randomly selected statement, or {@code null} if none exist
     */
    private Node getRandomStmt(CompilationUnit cu, Node targetNode) {
        List<Statement> statements;
        if (canGetFixFromDifferentClass) {
            statements = cu.findAll(Statement.class)
                    .stream()
                    .filter(s -> !(s instanceof BlockStmt) && !s.equals(targetNode) && !(s instanceof EmptyStmt))
                    .toList();
        } else {
            ClassOrInterfaceDeclaration targetClass =
                    targetNode.findAncestor(ClassOrInterfaceDeclaration.class).get();

            statements = targetClass.findAll(Statement.class)
                    .stream()
                    .filter(s -> !(s instanceof BlockStmt) && !s.equals(targetNode) && !(s instanceof EmptyStmt))
                    .toList();
        }

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

    /**
     * Configures whether fixes may be taken from different classes.
     *
     * @param canGetFixFromDifferentClass {@code true} to allow cross-class donors;
     *                                    {@code false} otherwise
     */
    public void setCanGetFixFromDifferentClass(boolean canGetFixFromDifferentClass) {
        this.canGetFixFromDifferentClass = canGetFixFromDifferentClass;
    }
}
