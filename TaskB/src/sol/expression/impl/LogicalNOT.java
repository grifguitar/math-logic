package sol.expression.impl;

import sol.Operation;
import sol.expression.AbstractUnaryExpression;
import sol.expression.Expression;

public class LogicalNOT extends AbstractUnaryExpression {
    public LogicalNOT(Expression argument) {
        super(argument, Operation.LOGICAL_NOT.getSymbol());
    }
}