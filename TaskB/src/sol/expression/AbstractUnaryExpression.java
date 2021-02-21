package sol.expression;

import sol.util.Pair;

import java.util.HashMap;

public abstract class AbstractUnaryExpression extends AbstractExpression {
    //fields:

    protected Expression argument;
    protected String operation;

    //constructors:

    public AbstractUnaryExpression(Expression argument, String operation) {
        this.argument = argument;
        this.operation = operation;
        this.hash = convertToString().hashCode();
    }

    //getters and setters:

    public Expression getArgument() {
        return argument;
    }

    public String getOperation() {
        return operation;
    }

    //overridden expression methods:

    public String convertToString() {
        return operation + argument.convertToString();
    }

    public boolean tryMatch(Expression expr) {
        if (expr instanceof AbstractUnaryExpression) {
            return (operation.equals(((AbstractUnaryExpression) expr).getOperation())) &&
                    (argument.tryMatch(((AbstractUnaryExpression) expr).getArgument()));
        }
        return false;
    }

    public Pair<Boolean, HashMap<String, String>> check() {
        return argument.check();
    }

}