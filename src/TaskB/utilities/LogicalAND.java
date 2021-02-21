package TaskB.utilities;

public class LogicalAND extends AbstractBinaryExpression {
    public LogicalAND(Expression firstArg, Expression secondArg) {
        super(firstArg, secondArg, Operation.LOGICAL_AND.getSymbol());
    }
}