package sol;

import sol.expression.Expression;
import sol.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class Proof {
    //fields:

    public HashMap<Expression, Integer> hypotheses;
    public Expression statement;
    public ArrayList<Expression> chain;

    private final ArrayList<Pair<Integer, Pair<Integer, Integer>>> dataChain;

    //constructors:

    public Proof() {
        hypotheses = new HashMap<>();
        chain = new ArrayList<>();
        dataChain = new ArrayList<>();
    }

    //public methods:

    public boolean lineIsHypothesis(int lineNumber) {
        return dataChain.get(lineNumber).first < 0;
    }

    public Integer getHypothesisNumber(int lineNumber) {
        return -dataChain.get(lineNumber).first;
    }

    public boolean lineIsModusPonens(int lineNumber) {
        return dataChain.get(lineNumber).first == 100;
    }

    public Integer getModusPonensFrom(int lineNumber) {
        return dataChain.get(lineNumber).second.first;
    }

    public Integer getModusPonensTo(int lineNumber) {
        return dataChain.get(lineNumber).second.second;
    }

    public boolean lineIsAxiomScheme(int lineNumber) {
        return (dataChain.get(lineNumber).first > 0) && !lineIsModusPonens(lineNumber);
    }

    public Integer getAxiomSchemeNumber(int lineNumber) {
        return dataChain.get(lineNumber).first;
    }

    public void addToDataChain(Pair<Integer, Pair<Integer, Integer>> elem) {
        dataChain.add(elem);
    }

}