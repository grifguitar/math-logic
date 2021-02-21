package TaskB.utilities;

public class LogicalNOT extends AbstractUnaryExpression {
    public LogicalNOT(Expression argument) {
        super(argument, Operation.LOGICAL_NOT.getSymbol());
    }
}