package org.group10.utils.astvisitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A visitor for analyzing variable scopes at a specific line in a Java source file.
 * <p>
 * This class traverses an Abstract Syntax Tree (AST) of a Java class and tracks
 * variables in the current scope, including class fields, method parameters,
 * local variables, and variables in nested blocks.
 * <p>
 * The visitor maintains a stack of scopes and records a snapshot of all variables
 * visible at a specific target line. This is useful for tools that need to
 * understand which variables are accessible at a certain point in the code.
 */
public class ScopeVisitor extends VoidVisitorAdapter<Void> {
    private final int targetLine;
    private final Deque<Map<String, Type>> scopeStack = new ArrayDeque<>();
    private final Map<String, Type> variablesInScope = new LinkedHashMap<>();
    private int lastSnapshotLine = 0;

    /**
     * Constructs a ScopeVisitor for a specific target line.
     *
     * @param targetLine the line number at which to capture the visible variables
     */
    public ScopeVisitor(int targetLine) {
        this.targetLine = targetLine;
    }

    /**
     * Visits a class or interface declaration, adding its fields to the current scope.
     *
     * @param node the class or interface declaration node
     * @param arg  additional argument (unused)
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration node, Void arg) {
        enterScope();
        for (FieldDeclaration f : node.getFields()) {
            f.getVariables().forEach(field -> scopeStack.peek().put("this." + field.getNameAsString(), field.getType()));
        }
        super.visit(node, arg);
        trySnapshot(node);
        exitScope();
    }

    /**
     * Visits a block statement, creating a new scope for local variables declared inside it.
     *
     * @param block the block statement node
     * @param arg   additional argument (unused)
     */
    @Override
    public void visit(BlockStmt block, Void arg) {
        enterScope();
        super.visit(block, arg);
        trySnapshot(block);
        exitScope();
    }

    /**
     * Visits a variable declaration expression, adding variables to the current scope
     * if they are declared on or before the target line.
     *
     * @param n   the variable declaration expression
     * @param arg additional argument (unused)
     */
    @Override
    public void visit(VariableDeclarationExpr n, Void arg) {
        n.getVariables().forEach(var -> var.getRange().ifPresent(r -> {
            if (r.begin.line <= targetLine) {
                scopeStack.peek().put(var.getNameAsString(), var.getType());
            }
        }));
        super.visit(n, arg);
    }

    /**
     * Visits a field declaration, adding class-level fields to the current scope.
     *
     * @param f   the field declaration node
     * @param arg additional argument (unused)
     */
    @Override
    public void visit(FieldDeclaration f, Void arg) {
        f.getVariables().forEach(field -> scopeStack.peek().put("this." + field.getNameAsString(), field.getType()));

        super.visit(f, arg);
    }


    /**
     * Visits a method declaration, creating a new scope and adding method parameters
     * if they appear before or on the target line.
     *
     * @param m   the method declaration node
     * @param arg additional argument (unused)
     */
    @Override
    public void visit(MethodDeclaration m, Void arg) {
        enterScope();
        for (Parameter param : m.getParameters()) {
            param.getRange().ifPresent(r -> {
                if (r.begin.line <= targetLine && scopeStack.peek() != null) {
                    scopeStack.peek().put(param.getNameAsString(), param.getType());
                }
            });
        }
        super.visit(m, arg);
        trySnapshot(m);
        exitScope();
    }

    /**
     * Visits a constructor declaration, creating a new scope and adding constructor parameters
     * if they appear before or on the target line.
     *
     * @param c   the constructor declaration node
     * @param arg additional argument (unused)
     */
    @Override
    public void visit(ConstructorDeclaration c, Void arg) {
        enterScope();
        for (Parameter param : c.getParameters()) {
            param.getRange().ifPresent(r -> {
                if (r.begin.line <= targetLine && scopeStack.peek() != null) {
                    scopeStack.peek().put(param.getNameAsString(), param.getType());
                }
            });
        }
        super.visit(c, arg);
        trySnapshot(c);
        exitScope();
    }

    /**
     * Returns the variables visible at the target line.
     *
     * @return a map of variable names to their types
     */
    public Map<String, Type> getVariablesInScope() {
        return variablesInScope;
    }

    /**
     * Creates a new scope by pushing a fresh variable map onto the scope stack.
     */
    private void enterScope() {
        scopeStack.push(new LinkedHashMap<>());
    }

    /**
     * Exits the current scope by popping the top map from the scope stack.
     */
    private void exitScope() {
        scopeStack.pop();
    }

    /**
     * Takes a snapshot of the current scopes if the node starts at or before the target line
     * and after the last snapshot line.
     *
     * @param n the AST node to consider
     */
    private void trySnapshot(Node n) {
        int startLine = n.getRange().get().begin.line;
        if (startLine <= targetLine && startLine > lastSnapshotLine) {
            snapshotScopeStack();
            lastSnapshotLine = startLine;
        }
    }

    /**
     * Aggregates all variables in the current scope stack into {@link #variablesInScope}.
     */
    private void snapshotScopeStack() {
        variablesInScope.clear();
        for (Map<String, Type> scope : scopeStack) {
            variablesInScope.putAll(scope);
        }
    }
}
