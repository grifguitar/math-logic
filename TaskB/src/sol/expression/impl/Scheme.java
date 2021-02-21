package sol.expression.impl;

import sol.util.Pair;
import sol.expression.AbstractVariable;
import sol.expression.Expression;

import java.util.HashMap;

public class Scheme extends AbstractVariable {
    private String value;

    public Scheme(String name) {
        super(name);
        this.value = null;
    }

    public boolean tryMatch(Expression expr) {
        if (value == null) {
            value = expr.convertToString();
            return true;
        }
        return value.equals(expr.convertToString());
    }

    public Pair<Boolean, HashMap<String, String>> check() {
        HashMap<String, String> map = new HashMap<>();
        map.put(name, value);
        return new Pair<>(true, map);
    }
}