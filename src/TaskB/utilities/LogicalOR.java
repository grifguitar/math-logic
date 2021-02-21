package TaskB.utilities;

public class LogicalOR extends AbstractBinaryExpression {
    public LogicalOR(Expression firstArg, Expression secondArg) {
        super(firstArg, secondArg, Operation.LOGICAL_OR.getSymbol());
    }
}