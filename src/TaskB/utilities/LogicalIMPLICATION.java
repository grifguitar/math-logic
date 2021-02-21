package TaskB.utilities;

public class LogicalIMPLICATION extends AbstractBinaryExpression {
    public LogicalIMPLICATION(Expression firstArg, Expression secondArg) {
        super(firstArg, secondArg, Operation.LOGICAL_IMPLICATION.getSymbol());
    }
}