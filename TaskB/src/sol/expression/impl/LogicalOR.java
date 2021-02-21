package sol.expression.impl;

import sol.Operation;
import sol.expression.AbstractBinaryExpression;
import sol.expression.Expression;

public class LogicalOR extends AbstractBinaryExpression {
    public LogicalOR(Expression firstArg, Expression secondArg) {
        super(firstArg, secondArg, Operation.LOGICAL_OR.getSymbol());
    }
}