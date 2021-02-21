package sol;

import sol.expression.Expression;
import sol.expression.impl.LogicalAND;
import sol.expression.impl.LogicalIMPLICATION;
import sol.expression.impl.LogicalNOT;
import sol.expression.impl.LogicalOR;

public enum Operation implements ExpressionOrOperation, BracketOrOperation {
    LOGICAL_AND("&", 9, false, true) {
        public Expression build(Expression firstArg, Expression secondArg) {
            return new LogicalAND(firstArg, secondArg);
        }
    },
    LOGICAL_OR("|", 8, false, true) {
        public Expression build(Expression firstArg, Expression secondArg) {
            return new LogicalOR(firstArg, secondArg);
        }
    },
    LOGICAL_IMPLICATION("->", 7, true, true) {
        public Expression build(Expression firstArg, Expression secondArg) {
            return new LogicalIMPLICATION(firstArg, secondArg);
        }
    },
    LOGICAL_NOT("!", 10, false, false) {
        public Expression build(Expression firstArg, Expression secondArg) {
            return new LogicalNOT(firstArg);
        }
    };

    private final String symbol;
    private final int priority;
    private final boolean rightAssociative;
    private final boolean binary;

    Operation(String symbol, int priority, boolean rightAssociative, boolean binary) {
        this.symbol = symbol;
        this.priority = priority;
        this.rightAssociative = rightAssociative;
        this.binary = binary;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isRightAssociative() {
        return rightAssociative;
    }

    public boolean isBinary() {
        return binary;
    }

    public abstract Expression build(Expression firstArg, Expression secondArg);
}