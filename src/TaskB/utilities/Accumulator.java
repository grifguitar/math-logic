package TaskB.utilities;

import java.util.ArrayList;
import java.util.HashMap;

public class Accumulator {
    //fields:

    private final HashMap<Expression, Integer> proven;
    private final HashMap<Expression, ArrayList<Pair<Expression, Integer>>> supposed;

    //constructors:

    public Accumulator() {
        this.proven = new HashMap<>();
        this.supposed = new HashMap<>();
    }

    //public methods:

    public void putProven(Expression expr, int lineNum) {
        proven.put(expr, lineNum);
    }

    public Integer getProven(Expression expr) {
        return proven.get(expr);
    }

    public boolean isProven(Expression expr) {
        return proven.containsKey(expr);
    }

    public void tryPutForMP(Expression expr, int lineNum) {
        if (expr instanceof AbstractBinaryExpression) {
            AbstractBinaryExpression node = (AbstractBinaryExpression) expr;
            if (node.getOperation().equals(Operation.LOGICAL_IMPLICATION.getSymbol())) {
                if (proven.containsKey(node.getSecondArg())) {
                    supposed.remove(node.getSecondArg());
                    return;
                }
                ArrayList<Pair<Expression, Integer>> list;
                if (supposed.containsKey(node.getSecondArg())) {
                    list = supposed.get(node.getSecondArg());
                } else {
                    list = new ArrayList<>();
                }
                list.add(new Pair<>(node.getFirstArg(), lineNum));
                supposed.put(node.getSecondArg(), list);
            }
        }
    }

    public Pair<Integer, Integer> isMP(Expression expr) {
        if (supposed.containsKey(expr)) {
            ArrayList<Pair<Expression, Integer>> list = supposed.get(expr);
            for (Pair<Expression, Integer> elem : list) {
                if (proven.containsKey(elem.first)) {
                    supposed.remove(expr);
                    return new Pair<>(elem.second, proven.get(elem.first));
                }
            }
        }
        return null;
    }

}