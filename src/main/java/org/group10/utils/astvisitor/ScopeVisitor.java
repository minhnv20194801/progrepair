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

public class ScopeVisitor extends VoidVisitorAdapter<Void> {
    private final int targetLine;
    private final Deque<Map<String, Type>> scopeStack = new ArrayDeque<>();
    private final Map<String, Type> variablesInScope = new LinkedHashMap<>();
    private int lastSnapshotLine = 0;

    public ScopeVisitor(int targetLine) {
        this.targetLine = targetLine;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration node, Void arg) {
        enterScope();
        System.out.println("Visit class " + node.getName());
        for (FieldDeclaration f : node.getFields()) {
            f.getVariables().forEach(field -> {
                scopeStack.peek().put("this." + field.getNameAsString(), field.getType());
            });
        }
        super.visit(node, arg);
        trySnapshot(node);
        System.out.println("Exit class " + node.getName());
        exitScope();
    }

    @Override
    public void visit(BlockStmt block, Void arg) {
        enterScope();
        System.out.println("Visit block " + block.getRange().get().begin.line);
        super.visit(block, arg);
        trySnapshot(block);
        System.out.println("Exit block " + block.getRange().get().begin.line);
        exitScope();
    }

    @Override
    public void visit(VariableDeclarationExpr n, Void arg) {
        n.getVariables().forEach(var -> {
            var.getRange().ifPresent(r -> {
                if (r.begin.line <= targetLine) {
                    scopeStack.peek().put(var.getNameAsString(), var.getType());
                }
            });
        });
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration f, Void arg) {
        f.getVariables().forEach(field -> {
            scopeStack.peek().put("this." + field.getNameAsString(), field.getType());
        });

        super.visit(f, arg);
    }

    @Override
    public void visit(MethodDeclaration m, Void arg) {
        enterScope();
        System.out.println("Visit method " + m.getName());
        for (Parameter param : m.getParameters()) {
            param.getRange().ifPresent(r -> {
                if (r.begin.line <= targetLine) {
                    scopeStack.peek().put(param.getNameAsString(), param.getType());
                }
            });
        }
        super.visit(m, arg);
        trySnapshot(m);
        System.out.println("Exit method " + m.getName());
        exitScope();
    }

    @Override
    public void visit(ConstructorDeclaration c, Void arg) {
        enterScope();
        System.out.println("Visit constructor " + c.getName());
        for (Parameter param : c.getParameters()) {
            param.getRange().ifPresent(r -> {
                if (r.begin.line <= targetLine) {
                    scopeStack.peek().put(param.getNameAsString(), param.getType());
                }
            });
        }
        super.visit(c, arg);
        trySnapshot(c);
        System.out.println("Exit constructor " + c.getName());
        exitScope();
    }

    public Map<String, Type> getVariablesInScope() {
        return variablesInScope;
    }

    private void enterScope() {
        scopeStack.push(new LinkedHashMap<>());
    }

    private void exitScope() {
        scopeStack.pop();
    }

    private void trySnapshot(Node n) {
        int startLine = n.getRange().get().begin.line;
        if (startLine <= targetLine && startLine > lastSnapshotLine) {
            System.out.println(scopeStack);
            snapshotScopeStack();
            lastSnapshotLine = startLine;
        }
    }

    private void snapshotScopeStack() {
        variablesInScope.clear();
        for (Map<String, Type> scope : scopeStack) {
            variablesInScope.putAll(scope);
        }
    }
}
