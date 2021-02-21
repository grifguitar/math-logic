package sol.expression.impl;

import sol.Operation;
import sol.expression.AbstractBinaryExpression;
import sol.expression.Expression;

public class LogicalIMPLICATION extends AbstractBinaryExpression {
    public LogicalIMPLICATION(Expression firstArg, Expression secondArg) {
        super(firstArg, secondArg, Operation.LOGICAL_IMPLICATION.getSymbol());
    }
}