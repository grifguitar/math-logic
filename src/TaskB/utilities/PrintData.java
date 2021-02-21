package TaskB.utilities;

public class PrintData {
    public boolean use;
    public int prefix;
    public Expression expr;
    public int from = -1;
    public int to = -1;
    public boolean isMP = false;

    public PrintData(int prefix, Expression expr) {
        this.prefix = prefix;
        this.expr = expr;
    }

    public PrintData(int prefix, int from, int to, Expression expr) {
        this.prefix = prefix;
        this.expr = expr;
        this.from = from;
        this.to = to;
        this.isMP = true;
    }

    public PrintData markUse(boolean value) {
        this.use = value;
        return this;
    }
}