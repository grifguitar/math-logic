package TaskB.utilities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class Minimizer {
    //fields:

    private Proof proof;

    //constructors:

    public Minimizer(Proof proof) {
        this.proof = proof;
    }

    //getters and setters:

    public Proof getProof() {
        return proof;
    }

    //public methods:

    public boolean minimize() {
        Accumulator accumulator = new Accumulator();
        ArrayList<PrintData> printQueue = new ArrayList<>();
        printQueue.add(null);

        boolean lastIsStatement = false;
        int lineNum = 0;
        for (Expression expr : proof.chain) {
            lastIsStatement = expr.equals(proof.statement);

            if (accumulator.isProven(expr)) {
                continue;
            }

            boolean isFound = false;
            lineNum++;

            for (Axioms axiom : Axioms.values()) {
                Expression pattern = axiom.getPattern();
                if (pattern.tryMatch(expr)) {
                    Pair<Boolean, HashMap<String, String>> result = pattern.check();
                    if (result.first) {
                        printQueue.add(new PrintData(axiom.ordinal() + 1, expr).markUse(false));
                        accumulator.putProven(expr, lineNum);
                        accumulator.tryPutForMP(expr, lineNum);
                        isFound = true;
                        break;
                    }
                }
            }

            if (!isFound) {
                if (proof.hypotheses.containsKey(expr)) {
                    printQueue.add(new PrintData(-proof.hypotheses.get(expr), expr).markUse(false));
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                    isFound = true;
                }
            }

            if (!isFound) {
                Pair<Integer, Integer> result = accumulator.isMP(expr);
                if (result != null) {
                    printQueue.add(new PrintData(100, result.first, result.second, expr).markUse(false));
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                } else {
                    return false;
                }
            }
        }

        Integer num = accumulator.getProven(proof.statement);
        if (!lastIsStatement) {
            return false;
        }

        ArrayDeque<Integer> deque = new ArrayDeque<>();
        deque.addLast(num);

        while (!deque.isEmpty()) {
            num = deque.removeLast();
            PrintData result = printQueue.get(num);
            printQueue.set(num, result.markUse(true));
            if (result.isMP) {
                deque.addLast(result.from);
                deque.addLast(result.to);
            }
        }

        Proof outProof = new Proof();
        outProof.hypotheses = proof.hypotheses;
        outProof.statement = proof.statement;

        int[] newNumber = new int[printQueue.size()];

        int skippedLines = 0;
        for (int i = 1; i < printQueue.size(); i++) {
            PrintData data = printQueue.get(i);
            if (!data.use) {
                skippedLines++;
                continue;
            }

            newNumber[i] = i - skippedLines;

            outProof.chain.add(data.expr);
            if (data.isMP) {
                outProof.addToDataChain(new Pair<>(data.prefix, new Pair<>(newNumber[data.from], newNumber[data.to])));
            } else {
                outProof.addToDataChain(new Pair<>(data.prefix, new Pair<>(-1, -1)));
            }
        }

        proof = outProof;
        return true;
    }
}