package TaskB.utilities;

import java.util.HashMap;

public class Variable extends AbstractVariable {
    public Variable(String name) {
        super(name);
    }

    public boolean tryMatch(Expression expr) {
        if (expr instanceof Variable) {
            return name.equals(((Variable) expr).getName());
        }
        return false;
    }

    public Pair<Boolean, HashMap<String, String>> check() {
        return new Pair<>(true, new HashMap<>());
    }
}