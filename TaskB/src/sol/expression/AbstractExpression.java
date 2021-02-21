package sol.expression;

public abstract class AbstractExpression implements Expression {
    //fields:

    protected int hash;

    //overridden object methods:

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == null)
            return false;
        if (ob instanceof AbstractExpression) {
            if (hashCode() != ob.hashCode())
                return false;
            return convertToString().equals(((AbstractExpression) ob).convertToString());
        }
        return false;
    }

}