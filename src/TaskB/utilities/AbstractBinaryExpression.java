package TaskB.utilities;

import java.util.HashMap;

public abstract class AbstractBinaryExpression extends AbstractExpression {
    //fields:

    protected Expression firstArg, secondArg;
    protected String operation;

    //constructors:

    public AbstractBinaryExpression(Expression firstArg, Expression secondArg, String operation) {
        this.firstArg = firstArg;
        this.secondArg = secondArg;
        this.operation = operation;
        this.hash = convertToString().hashCode();
    }

    //getters and setters:

    public Expression getFirstArg() {
        return firstArg;
    }

    public Expression getSecondArg() {
        return secondArg;
    }

    public String getOperation() {
        return operation;
    }

    //overridden expression methods:

    public String convertToString() {
        return "(" + firstArg.convertToString() + " " + operation + " " + secondArg.convertToString() + ")";
    }

    public boolean tryMatch(Expression expr) {
        if (expr instanceof AbstractBinaryExpression) {
            return (operation.equals(((AbstractBinaryExpression) expr).getOperation())) &&
                    (firstArg.tryMatch(((AbstractBinaryExpression) expr).getFirstArg())) &&
                    (secondArg.tryMatch(((AbstractBinaryExpression) expr).getSecondArg()));
        }
        return false;
    }

    public Pair<Boolean, HashMap<String, String>> check() {
        Pair<Boolean, HashMap<String, String>> leftResult = firstArg.check();
        Pair<Boolean, HashMap<String, String>> rightResult = secondArg.check();
        if (!leftResult.first)
            return leftResult;
        if (!rightResult.first)
            return rightResult;
        rightResult.second.forEach((name, value) -> {
            if (leftResult.second.containsKey(name)) {
                if (!leftResult.second.get(name).equals(value)) {
                    leftResult.first = false;
                }
            } else {
                leftResult.second.put(name, value);
            }
        });
        return leftResult;
    }

}