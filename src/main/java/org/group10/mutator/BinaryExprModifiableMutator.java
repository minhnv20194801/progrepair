package org.group10.mutator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import org.group10.program.Program;
import org.group10.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A mutation operator that extends the functionality of a classical GenProg mutator to
 * be able to also modify binary expressions. <br>
 *
 */
public class BinaryExprModifiableMutator extends ClassicGenProgMutator {
    /**
     * Applies a mutation to the given program. <br>
     *
     * Extends the functionality of {@link ClassicGenProgMutator} by calling
     * its mutate method 3/4 time, while 1/4 chance will it choose a suspicious
     * binary expression to modify it
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
            e.printStackTrace();
            return program;
        }

        Map<Integer, Double> suspiciousScores = program.getSuspiciousScore();
        List<BinaryExpr> binaryExprList = cu.findAll(BinaryExpr.class).stream().toList();
        List<Integer> suspiciousLineNumbers = new ArrayList<>(suspiciousScores.keySet());
        List<BinaryExpr> mutatiableCandidateLst = new ArrayList<>();

        for (BinaryExpr binaryExpr : binaryExprList) {
            int begin = binaryExpr.getRange().get().begin.line;
            if (suspiciousLineNumbers.contains(begin)) {
                mutatiableCandidateLst.add(binaryExpr);
            }
        }

        // 4 choices: swap, delete, insert, modify binary expression
        int numberOfChoices = 4;
        // If there are no modifiable binary expression then we lose the mutation choice
        // of modifying expression
        if (mutatiableCandidateLst.isEmpty()) {
            numberOfChoices--;
        }

        int choice = Randomness.getRandom().nextInt(numberOfChoices);
        if (choice < 3) {
//            System.out.println("Use classic mutation");
            return super.mutate(program);
        } else {
//            System.out.println("Use binary expression mutation");
            int randomIndex = Randomness.getRandom().nextInt(mutatiableCandidateLst.size());
            BinaryExpr targetExpr = mutatiableCandidateLst.get(randomIndex);
            BinaryExpr.Operator targetOperator = targetExpr.getOperator();
            BinaryExpr.Operator newOperator = getRandomOperator(targetOperator);
            targetExpr.setOperator(newOperator);

            return new Program(program.getClassName(), cu.toString().lines().toList(), program.getTestSuite(), program.getMutator(), program.getCrossover(), program.getSuspiciousCalculator(), program.getFitnessFunction());
        }
    }

    private BinaryExpr.Operator getRandomOperator(BinaryExpr.Operator operator) {
        List<BinaryExpr.Operator> arithmetic = new ArrayList<>(List.of(
                BinaryExpr.Operator.PLUS,
                BinaryExpr.Operator.MINUS,
                BinaryExpr.Operator.MULTIPLY,
                BinaryExpr.Operator.DIVIDE,
                BinaryExpr.Operator.REMAINDER
        ));

        List<BinaryExpr.Operator> conditional = new ArrayList<>(List.of(
                BinaryExpr.Operator.LESS,
                BinaryExpr.Operator.LESS_EQUALS,
                BinaryExpr.Operator.GREATER,
                BinaryExpr.Operator.GREATER_EQUALS,
                BinaryExpr.Operator.EQUALS,
                BinaryExpr.Operator.NOT_EQUALS
        ));

        List<BinaryExpr.Operator> logical = new ArrayList<>(List.of(
                BinaryExpr.Operator.AND,
                BinaryExpr.Operator.OR
        ));

        List<BinaryExpr.Operator> candidate;

        if (arithmetic.contains(operator)) {
            arithmetic.remove(operator);
            candidate = arithmetic;
        } else if (conditional.contains(operator)) {
            conditional.remove(operator);
            candidate = conditional;
        } else if (logical.contains(operator)) {
            logical.remove(operator);
            candidate = logical;
        } else {
            // Fall back to union of all lists
            candidate = new ArrayList<>();
            candidate.addAll(arithmetic);
            candidate.addAll(conditional);
            candidate.addAll(logical);
        }

        return candidate.get(Randomness.getRandom().nextInt(candidate.size()));
    }
    @Override
    public String toString() {
        return "BinaryExprModifiableMutator get fixes from different classes: " + canGetFixFromDifferentClass;
    }
}
