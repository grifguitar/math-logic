package sol.expression.impl;

import sol.Operation;
import sol.expression.AbstractBinaryExpression;
import sol.expression.Expression;

public class LogicalAND extends AbstractBinaryExpression {
    public LogicalAND(Expression firstArg, Expression secondArg) {
        super(firstArg, secondArg, Operation.LOGICAL_AND.getSymbol());
    }
}