package sol.expression;

public abstract class AbstractVariable extends AbstractExpression {
    //fields:

    protected String name;

    //constructors:

    public AbstractVariable(String name) {
        this.name = name;
        this.hash = convertToString().hashCode();
    }

    //getters and setters:

    public String getName() {
        return name;
    }

    //overridden expression methods:

    public String convertToString() {
        return name;
    }

}