import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            FastScanner input = new FastScanner(System.in);
            Writer output = new PrintWriter(System.out);
            solve(input, output);
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void solve(FastScanner input, Writer output) throws IOException {
        String statement = input.nextLine();
        if (statement == null) {
            output.write(":(");
            return;
        }

        Parser parser = new Parser();

        Expression mainExpr = parser.parseExpression(statement);

        ArrayList<String> variables = new ArrayList<>();
        HashMap<String, Boolean> trueVariables = new HashMap<>();
        HashMap<String, Boolean> falseVariables = new HashMap<>();
        parser.getMapVariable().forEach((name, expression) -> {
            variables.add(name);
            trueVariables.put(name, true);
            falseVariables.put(name, false);
        });

        if (variables.size() > 3) {
            throw new RuntimeException("The number of variables should not exceed three");
        }

        boolean boolResult;
        if (mainExpr.evaluate(trueVariables)) {
            boolResult = true;
        } else if (!mainExpr.evaluate(falseVariables)) {
            boolResult = false;
        } else {
            output.write(":(");
            return;
        }

        ArrayList<Pair<String, Troika<Integer, Integer, Integer>>> hypothesesList = new ArrayList<>();
        HashMap<String, Troika<Integer, Integer, Integer>> hypothesesMap = new HashMap<>();
        for (int mask = 0; mask < Math.pow(2, variables.size()); mask++) {
            HashMap<String, Boolean> variableValues = new HashMap<>();
            int[] bit = new int[]{1, 2, 4};
            StringBuilder binaryString = new StringBuilder();
            for (int i = 0; i < variables.size(); i++) {
                binaryString.append((mask & bit[i]) != 0 ? "1" : "0");
                variableValues.put(variables.get(i), ((mask & bit[i]) != 0));
            }
            if (mainExpr.evaluate(variableValues) == boolResult) {
                Troika<Integer, Integer, Integer> troika = new Troika<>(-1, -1,
                        hypothesesList.size());
                hypothesesList.add(new Pair<>(binaryString.toString(), troika));
                hypothesesMap.put(binaryString.toString(), troika);
            }
        }

        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < hypothesesList.size() && !flag; i++) {
                for (int j = i + 1; j < hypothesesList.size() && !flag; j++) {
                    int pos;
                    if ((pos = isOneDifference(hypothesesList.get(i).first, hypothesesList.get(j).first)) != -1) {
                        StringBuilder binaryString = new StringBuilder(hypothesesList.get(i).first);
                        binaryString.setCharAt(pos, '2');
                        if (hypothesesMap.containsKey(binaryString.toString())) {
                            continue;
                        }
                        Troika<Integer, Integer, Integer> troika = new Troika<>(i, j,
                                hypothesesList.size());
                        hypothesesList.add(new Pair<>(binaryString.toString(), troika));
                        hypothesesMap.put(binaryString.toString(), troika);
                        flag = true;
                    }
                }
            }
        }

//        hypothesesMap.forEach((name, value) -> {
//            try {
//                output.write(name + " " + value.first + " " + value.second);
//                output.write('\n');
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        output.write("#\n");

        String[][] patterns;
        if (boolResult) {
            patterns = new String[][]{new String[]{"2", "1"},
                    new String[]{"22", "21", "12", "11"},
                    new String[]{"222", "221", "212", "122", "211", "121", "112", "111"}};
        } else {
            patterns = new String[][]{new String[]{"2", "0"},
                    new String[]{"22", "20", "02", "00"},
                    new String[]{"222", "220", "202", "022", "200", "020", "002", "000"}};
        }

        int answer = -1;

        LinkedList<Pair<String, Troika<Integer, Integer, Integer>>> queue = new LinkedList<>();
        for (String pattern : patterns[variables.size() - 1]) {
            if (hypothesesMap.containsKey(pattern)) {
                Troika<Integer, Integer, Integer> troika = hypothesesMap.get(pattern);
//                output.write(pattern + " " + troika.first + " " + troika.second + " " + troika.third);
//                output.write('\n');
                queue.addLast(new Pair<>(pattern, troika));
                answer = troika.third;
                break;
            }
        }

        ArrayList<Pair<String, Troika<Integer, Integer, Integer>>> orderedHypotheses = new ArrayList<>();
        while (!queue.isEmpty()) {
            orderedHypotheses.add(queue.removeFirst());
            Troika<Integer, Integer, Integer> troika = orderedHypotheses.get(orderedHypotheses.size() - 1).second;
            if (troika.first != -1 && troika.second != -1) {
                queue.addLast(hypothesesList.get(troika.first));
                queue.addLast(hypothesesList.get(troika.second));
            }
        }

//        output.write("##\n");

        HashMap<Integer, Proof> proofsMap = new HashMap<>();
        if (!boolResult) {
            mainExpr = Operations.LOGICAL_NOT.getOperation(mainExpr, null);
        }

        for (int i = 0; i < orderedHypotheses.size(); i++) {
            Pair<String, Troika<Integer, Integer, Integer>> elem =
                    orderedHypotheses.get(orderedHypotheses.size() - 1 - i);
//            output.write(elem.first + " " + elem.second.first + " " + elem.second.second + " " + elem.second.third);
//            output.write('\n');
            if (elem.second.first == -1 && elem.second.second == -1) {
                HashMap<String, Boolean> map = new HashMap<>();
                for (int j = 0; j < variables.size(); j++) {
                    if (elem.first.charAt(j) == '0') {
                        map.put(variables.get(j), false);
                        continue;
                    }
                    if (elem.first.charAt(j) == '1') {
                        map.put(variables.get(j), true);
                    }
                }
                Pair<Proof, Boolean> outPair = mainExpr.evaluateProof(map);
                proofsMap.put(elem.second.third, outPair.first);
            } else {
                Proof first = proofsMap.get(elem.second.first);
                Proof second = proofsMap.get(elem.second.second);
                proofsMap.put(elem.second.third, proveRemoveHypothesis(first, second, mainExpr));
            }
        }

//        output.write("###\n");

        proofsMap.get(answer).print(output);
    }

    private static int isOneDifference(String first, String second) {
        if (first.length() != second.length()) {
            return -1;
        }
        int cnt = 0;
        int res = -1;
        for (int i = 0; i < first.length(); i++) {
            if (first.charAt(i) != second.charAt(i)) {
                cnt++;
                res = i;
            }
        }
        return (cnt == 1) ? res : -1;
    }

    private static Pair<Expression, Expression> isOneDifference(HashMap<Expression, Integer> first,
                                                                HashMap<Expression, Integer> second) {
        if (first.size() != second.size()) {
            return null;
        }
        ArrayList<Expression> diffList = new ArrayList<>();
        first.forEach((expr, i) -> {
            if (!second.containsKey(expr)) {
                diffList.add(expr);
            }
        });
        if (diffList.size() != 1) {
            return null;
        }
        second.forEach((expr, i) -> {
            if (!first.containsKey(expr)) {
                diffList.add(expr);
            }
        });
        return new Pair<>(diffList.get(0), diffList.get(1));
    }

    private static Proof proveOperation(String symbol, boolean x, boolean y, Expression first, Expression second) {
        Proof proof = new Proof();
        HashMap<String, Expression> variableReplacement = new HashMap<>();
        variableReplacement.put("x", first);
        variableReplacement.put("y", second);
        proof.parser.setVariableReplacement(variableReplacement);
        if (symbol.equals(Operations.LOGICAL_NOT.getSymbol())) {
            proof.hypotheses.put(proof.parser.parseExpression("x"), -1);
            proof.addLines(Proofs.PROOF_DOUBLE_NOT_1.getText());
            return proof;
        }
        if (!x && !y) {
            proof.hypotheses.put(proof.parser.parseExpression("!x"), -1);
            proof.hypotheses.put(proof.parser.parseExpression("!y"), -1);
            if (symbol.equals(Operations.LOGICAL_AND.getSymbol())) {
                proof.addLines(Proofs.PROOF_AND_0_0.getText());
            }
            if (symbol.equals(Operations.LOGICAL_OR.getSymbol())) {
                proof.addLines(Proofs.PROOF_OR_0_0.getText());
            }
            if (symbol.equals(Operations.LOGICAL_IMPLICATION.getSymbol())) {
                proof.addLines(Proofs.PROOF_IMPL_0_0.getText());
            }
            return proof;
        }
        if (!x) {
            proof.hypotheses.put(proof.parser.parseExpression("!x"), -1);
            proof.hypotheses.put(proof.parser.parseExpression("y"), -1);
            if (symbol.equals(Operations.LOGICAL_AND.getSymbol())) {
                proof.addLines(Proofs.PROOF_AND_0_1.getText());
            }
            if (symbol.equals(Operations.LOGICAL_OR.getSymbol())) {
                proof.addLines(Proofs.PROOF_OR_0_1.getText());
            }
            if (symbol.equals(Operations.LOGICAL_IMPLICATION.getSymbol())) {
                proof.addLines(Proofs.PROOF_IMPL_0_1.getText());
            }
            return proof;
        }
        if (!y) {
            proof.hypotheses.put(proof.parser.parseExpression("x"), -1);
            proof.hypotheses.put(proof.parser.parseExpression("!y"), -1);
            if (symbol.equals(Operations.LOGICAL_AND.getSymbol())) {
                proof.addLines(Proofs.PROOF_AND_1_0.getText());
            }
            if (symbol.equals(Operations.LOGICAL_OR.getSymbol())) {
                proof.addLines(Proofs.PROOF_OR_1_0.getText());
            }
            if (symbol.equals(Operations.LOGICAL_IMPLICATION.getSymbol())) {
                proof.addLines(Proofs.PROOF_IMPL_1_0.getText());
            }
            return proof;
        }
        proof.hypotheses.put(proof.parser.parseExpression("x"), -1);
        proof.hypotheses.put(proof.parser.parseExpression("y"), -1);
        if (symbol.equals(Operations.LOGICAL_AND.getSymbol())) {
            proof.addLines(Proofs.PROOF_AND_1_1.getText());
        }
        if (symbol.equals(Operations.LOGICAL_OR.getSymbol())) {
            proof.addLines(Proofs.PROOF_OR_1_1.getText());
        }
        if (symbol.equals(Operations.LOGICAL_IMPLICATION.getSymbol())) {
            proof.addLines(Proofs.PROOF_IMPL_1_1.getText());
        }
        return proof;
    }

    private static Proof proveRemoveHypothesis(Proof firstProof, Proof secondProof, Expression provableExpr) {
        if (firstProof.alphaHypothesis != null) {
            firstProof.hypotheses.put(firstProof.alphaHypothesis, -1);
            firstProof.alphaHypothesis = null;
        }
        if (secondProof.alphaHypothesis != null) {
            secondProof.hypotheses.put(secondProof.alphaHypothesis, -1);
            secondProof.alphaHypothesis = null;
        }
        Pair<Expression, Expression> diffPair = isOneDifference(firstProof.hypotheses, secondProof.hypotheses);
        if (diffPair == null) {
            return null;
        }
        firstProof.alphaHypothesis = diffPair.first;
        firstProof.hypotheses.remove(diffPair.first);
        secondProof.alphaHypothesis = diffPair.second;
        secondProof.hypotheses.remove(diffPair.second);

        Proof outProof = new Proof();
        HashMap<String, Expression> variableReplacement = new HashMap<>();
        variableReplacement.put("x", firstProof.alphaHypothesis);
        variableReplacement.put("y", secondProof.alphaHypothesis);
        variableReplacement.put("f", provableExpr);
        outProof.parser.setVariableReplacement(variableReplacement);

        outProof.addAll(deductionTheorem(firstProof));
        outProof.addAll(deductionTheorem(secondProof));
        outProof.addAll(proveExcludedThird(outProof.parser.parseExpression("x")));
        outProof.addAll(proveExcludedThird(outProof.parser.parseExpression("y")));
        outProof.output.add(outProof.parser.parseExpression(
                "(x -> f) -> (y -> f) -> (x | y -> f)"));
        outProof.output.add(outProof.parser.parseExpression(
                "(y -> f) -> (x | y -> f)"));
        outProof.output.add(outProof.parser.parseExpression(
                "(x | y -> f)"));
        outProof.output.add(outProof.parser.parseExpression(
                "(y -> f) -> (x -> f) -> (y | x -> f)"));
        outProof.output.add(outProof.parser.parseExpression(
                "(x -> f) -> (y | x -> f)"));
        outProof.output.add(outProof.parser.parseExpression(
                "(y | x -> f)"));
        outProof.output.add(outProof.parser.parseExpression(
                "f"));
        outProof.hypotheses = firstProof.hypotheses;
        return outProof;
    }

    private static Proof proveContrast(Expression first, Expression second) {
        Proof proof = new Proof();
        HashMap<String, Expression> variableReplacement = new HashMap<>();
        variableReplacement.put("x", first);
        variableReplacement.put("y", second);
        proof.parser.setVariableReplacement(variableReplacement);
        proof.hypotheses.put(proof.parser.parseExpression("x -> y"), -1);
        proof.alphaHypothesis = proof.parser.parseExpression("!y");
        proof.output.add(proof.parser.parseExpression(
                "(x -> y) -> (x -> !y) -> !x"));
        proof.output.add(proof.parser.parseExpression(
                "x -> y"));
        proof.output.add(proof.parser.parseExpression(
                "(x -> !y) -> !x"));
        proof.output.add(proof.parser.parseExpression(
                "!y -> (x -> !y)"));
        proof.output.add(proof.parser.parseExpression(
                "!y"));
        proof.output.add(proof.parser.parseExpression(
                "x -> !y"));
        proof.output.add(proof.parser.parseExpression(
                "!x"));
        proof = deductionTheorem(proof);
        proof.parser.setVariableReplacement(variableReplacement);
        proof.hypotheses.remove(proof.parser.parseExpression("x -> y"));
        proof.alphaHypothesis = proof.parser.parseExpression("x -> y");
        proof = deductionTheorem(proof);
        return proof;
    }

    private static Proof proveExcludedThird(Expression expr) {
        Proof proof = new Proof();
        HashMap<String, Expression> variableReplacement = new HashMap<>();
        variableReplacement.put("x", expr);
        proof.parser.setVariableReplacement(variableReplacement);
        proof.output.add(proof.parser.parseExpression(
                "x -> x | !x"));
        proof.addAll(proveContrast(proof.parser.parseExpression("x"),
                proof.parser.parseExpression("x | !x")));
        proof.output.add(proof.parser.parseExpression(
                "!(x | !x) -> !x"));
        proof.output.add(proof.parser.parseExpression(
                "!x -> x | !x"));
        proof.addAll(proveContrast(proof.parser.parseExpression("!x"),
                proof.parser.parseExpression("x | !x")));
        proof.output.add(proof.parser.parseExpression(
                "!(x | !x) -> !!x"));
        proof.output.add(proof.parser.parseExpression(
                "(!(x | !x) -> !x) -> (!(x | !x) -> !!x) -> (!!(x | !x))"));
        proof.output.add(proof.parser.parseExpression(
                "(!(x | !x) -> !!x) -> (!!(x | !x))"));
        proof.output.add(proof.parser.parseExpression(
                "(!!(x | !x))"));
        proof.output.add(proof.parser.parseExpression(
                "(!!(x | !x)) -> (x | !x)"));
        proof.output.add(proof.parser.parseExpression(
                "x | !x"));
        return proof;
    }

    private static Proof deductionTheorem(Proof inProof) {
        Proof outProof = new Proof();
        int lineNum = 1;
        for (Expression expr : inProof.output) {
            ExprData data = inProof.whatIsIt(expr, lineNum++);
            if (data.isAxiom() || data.isHypothesis()) {
                HashMap<String, Expression> variableReplacement = new HashMap<>();
                variableReplacement.put("x", inProof.alphaHypothesis);
                variableReplacement.put("y", data.expr);
                inProof.parser.setVariableReplacement(variableReplacement);
                outProof.output.add(inProof.parser.parseExpression(
                        "y"));
                outProof.output.add(inProof.parser.parseExpression(
                        "(y -> (x -> y))"));
                outProof.output.add(inProof.parser.parseExpression(
                        "(x -> y)"));
                continue;
            }
            if (data.isAlphaHypothesis()) {
                HashMap<String, Expression> variableReplacement = new HashMap<>();
                variableReplacement.put("x", inProof.alphaHypothesis);
                inProof.parser.setVariableReplacement(variableReplacement);
                outProof.output.add(inProof.parser.parseExpression(
                        "(x -> (x -> x))"));
                outProof.output.add(inProof.parser.parseExpression(
                        "(x -> (x -> x)) -> (x -> (x -> x) -> x) -> (x -> x)"));
                outProof.output.add(inProof.parser.parseExpression(
                        "(x -> (x -> x) -> x) -> (x -> x)"));
                outProof.output.add((inProof.parser.parseExpression(
                        "(x -> (x -> x) -> x)")));
                outProof.output.add((inProof.parser.parseExpression(
                        "(x -> x)")));
                continue;
            }
            if (data.isMP()) {
                HashMap<String, Expression> variableReplacement = new HashMap<>();
                variableReplacement.put("x", inProof.alphaHypothesis);
                variableReplacement.put("y", inProof.output.get(data.numbers.second - 1));
                variableReplacement.put("z", data.expr);
                inProof.parser.setVariableReplacement(variableReplacement);
                outProof.output.add(inProof.parser.parseExpression(
                        "(x -> y) -> (x -> y -> z) -> (x -> z)"));
                outProof.output.add(inProof.parser.parseExpression(
                        "(x -> y -> z) -> (x -> z)"));
                outProof.output.add(inProof.parser.parseExpression(
                        "(x -> z)"));
                continue;
            }
            throw new RuntimeException("Incorrect proof in the deduction theorem");
        }
        outProof.hypotheses = inProof.hypotheses;
        return outProof;
    }

    public static class Proof {
        public HashMap<Expression, Integer> hypotheses = new HashMap<>();
        public Expression alphaHypothesis = null;
        public ArrayList<Expression> output = new ArrayList<>();

        public Parser parser = new Parser();
        public Accumulator accumulator = new Accumulator();

        public void addLines(String text) {
            String[] lines = text.split(",");
            for (String line : lines) {
                output.add(parser.parseExpression(line));
            }
        }

        public void addAll(Proof proof) {
            output.addAll(proof.output);
        }

        public ExprData whatIsIt(Expression expr, int lineNum) {
            for (Axioms axiom : Axioms.values()) {
                Expression pattern = parser.parseExpression(axiom.getText());
                Pair<Boolean, HashMap<String, Expression>> result = pattern.tryMatch(expr);
                if (result.first) {
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                    return new ExprData(0, expr);
                }
            }

            if (hypotheses.containsKey(expr)) {
                accumulator.putProven(expr, lineNum);
                accumulator.tryPutForMP(expr, lineNum);
                return new ExprData(1, expr);
            }

            if (alphaHypothesis.equals(expr)) {
                accumulator.putProven(expr, lineNum);
                accumulator.tryPutForMP(expr, lineNum);
                return new ExprData(3, expr);
            }

            Pair<Integer, Integer> result = accumulator.isMP(expr);
            if (result != null) {
                accumulator.putProven(expr, lineNum);
                accumulator.tryPutForMP(expr, lineNum);
                return new ExprData(2, expr, result);
            }

            System.out.println("alpha-hyp");
            System.out.println(alphaHypothesis.getString());
            System.out.println("hyp");
            hypotheses.forEach((a, b) -> System.out.println(a.getString()));
            System.out.println("dok-vo");
            for (Expression ex : output) {
                System.out.println(ex.getString());
            }
            throw new RuntimeException("Unproven line of evidence in " + lineNum + " line: " + expr.getString());
        }

        public void print(Writer output) throws IOException {
            if (this.alphaHypothesis != null) {
                throw new RuntimeException("Alpha hypothesis is not null");
            }
            ArrayList<Expression> hyp = new ArrayList<>();
            this.hypotheses.forEach((expr, i) -> hyp.add(expr));
            for (int i = 0; i < hyp.size(); i++) {
                output.write(hyp.get(i).getString());
                if (i != hyp.size() - 1) {
                    output.write(", ");
                } else {
                    output.write(" ");
                }
            }
            output.write("|- " + this.output.get(this.output.size() - 1).getString());
            output.write('\n');

            for (Expression expr : this.output) {
                output.write(expr.getString());
                output.write('\n');
            }
        }
    }

    public static class Parser {
        private HashMap<String, Expression> mapVariable;
        private HashMap<String, Expression> mapScheme;
        private HashMap<String, Expression> variableReplacement;

        public Parser() {
            this.mapVariable = new HashMap<>();
            this.mapScheme = new HashMap<>();
        }

        public Map<String, Expression> getMapVariable() {
            return mapVariable;
        }

        public Expression getVariable(String name) {
            if (!mapVariable.containsKey(name)) {
                mapVariable.put(name, new Variable(name));
            }
            return mapVariable.get(name);
        }

        public Expression getScheme(String name) {
            if (!mapScheme.containsKey(name)) {
                mapScheme.put(name, new Scheme(name));
            }
            return mapScheme.get(name);
        }

        public void setVariableReplacement(HashMap<String, Expression> variableReplacement) {
            this.variableReplacement = variableReplacement;
        }

        private Expression parseExpression(String line) {
            ArrayList<ExpressionOrOperation> list = new ArrayList<>();
            LinkedList<BracketOrOperation> stack = new LinkedList<>();
            for (int i = 0; i < line.length(); i++) {
                if (Character.isSpaceChar(line.charAt(i))) {
                    continue;
                }
                if (line.charAt(i) >= 'A' && line.charAt(i) <= 'Z') {
                    int j = i;
                    while ((j < line.length()) && (
                            (line.charAt(j) >= 'A' && line.charAt(j) <= 'Z') ||
                                    (line.charAt(j) >= '0' && line.charAt(j) <= '9') ||
                                    (line.charAt(j) == '\''))) j++;
                    list.add(getVariable(line.substring(i, j)));
                    i = j - 1;
                    continue;
                }
                if (line.charAt(i) >= 'a' && line.charAt(i) <= 'c') {
                    list.add(getScheme(line.substring(i, i + 1)));
                    continue;
                }
                if (line.charAt(i) >= 'd' && line.charAt(i) <= 'z') {
                    list.add(new NoOperation(variableReplacement.get(line.substring(i, i + 1))));
                    continue;
                }
                Operations op = null;
                if (line.charAt(i) == '!') {
                    op = Operations.LOGICAL_NOT;
                }
                if (line.charAt(i) == '&') {
                    op = Operations.LOGICAL_AND;
                }
                if (line.charAt(i) == '|') {
                    op = Operations.LOGICAL_OR;
                }
                if (line.charAt(i) == '-' && line.charAt(i + 1) == '>') {
                    op = Operations.LOGICAL_IMPLICATION;
                    i += 1;
                }
                if (op != null && !op.isBinary()) {
                    stack.push(op);
                    continue;
                }
                if (op != null) {
                    while (!stack.isEmpty() && (stack.peek() instanceof Operations) && (op.isRightAssociative()
                            ? (((Operations) Objects.requireNonNull(stack.peek())).getPriority() > op.getPriority())
                            : (((Operations) Objects.requireNonNull(stack.peek())).getPriority() >= op.getPriority()))) {
                        list.add((Operations) stack.pop());
                    }
                    stack.push(op);
                    continue;
                }
                if (line.charAt(i) == '(') {
                    stack.push(Bracket.OPEN);
                    continue;
                }
                if (line.charAt(i) == ')') {
                    while (!stack.isEmpty() && stack.peek() instanceof Operations) {
                        list.add((Operations) stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new RuntimeException("Opening bracket is missing in expression: " + line);
                    }
                    stack.pop();
                    continue;
                }
                throw new RuntimeException("Invalid character in expression: " + line + " in position: " + i);
            }
            while (!stack.isEmpty()) {
                if (stack.peek() instanceof Bracket) {
                    throw new RuntimeException("Closing bracket is missing in expression: " + line);
                }
                list.add((Operations) stack.pop());
            }
            LinkedList<Expression> expressions = new LinkedList<>();
            for (ExpressionOrOperation elem : list) {
                if (elem instanceof Expression) {
                    expressions.addLast((Expression) elem);
                } else {
                    Expression firstArg, secondArg = null;
                    if (((Operations) elem).isBinary()) {
                        secondArg = expressions.removeLast();
                    }
                    firstArg = expressions.removeLast();
                    expressions.addLast(((Operations) elem).getOperation(firstArg, secondArg));
                }
            }
            if (expressions.size() != 1) {
                throw new RuntimeException("Incorrect postfix notation: " + list.toString());
            }
            return expressions.removeLast();
        }
    }

    public static class Accumulator {
        private HashMap<Expression, Integer> proven;
        private HashMap<Expression, ArrayList<Pair<Expression, Integer>>> supposed;

        public Accumulator() {
            this.proven = new HashMap<>();
            this.supposed = new HashMap<>();
        }

        public void putProven(Expression expr, int lineNum) {
            proven.put(expr, lineNum);
        }

//        public Integer getProven(Expression expr) {
//            return proven.get(expr);
//        }
//
//        public boolean isProven(Expression expr) {
//            return proven.containsKey(expr);
//        }

        public void tryPutForMP(Expression expr, int lineNum) {
            if (expr instanceof AbstractBinaryExpression) {
                AbstractBinaryExpression node = (AbstractBinaryExpression) expr;
                if (node.getOperation().equals(Operations.LOGICAL_IMPLICATION.getSymbol())) {
                    if (proven.containsKey(node.getSecondArg())) {
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
                        return new Pair<>(elem.second, proven.get(elem.first));
                    }
                }
            }
            return null;
        }
    }

    public interface BracketOrOperation {
    }

    public interface ExpressionOrOperation {
    }

    public interface Expression extends ExpressionOrOperation {
        String getString();

        Pair<Boolean, HashMap<String, Expression>> tryMatch(Expression expr);

        boolean evaluate(HashMap<String, Boolean> variableValues);

        Pair<Proof, Boolean> evaluateProof(HashMap<String, Boolean> variableValues);
    }

    public static abstract class AbstractExpression implements Expression {
        protected int hash;

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object ob) {
            if (ob == null)
                return false;
            if (ob == this)
                return true;
            if (ob instanceof AbstractExpression) {
                return getString().equals(((AbstractExpression) ob).getString());
            }
            return false;
        }
    }

    public static class NoOperation extends AbstractExpression {
        private Expression argument;

        public NoOperation(Expression argument) {
            this.argument = argument;
            this.hash = getString().hashCode();
        }

        public Expression getArgument() {
            return argument;
        }

        public String getString() {
            return argument.getString();
        }

        public Pair<Boolean, HashMap<String, Expression>> tryMatch(Expression expr) {
            return argument.tryMatch(expr);
        }

        public boolean evaluate(HashMap<String, Boolean> variableValues) {
            return argument.evaluate(variableValues);
        }

        public Pair<Proof, Boolean> evaluateProof(HashMap<String, Boolean> variableValues) {
            return argument.evaluateProof(variableValues);
        }
    }

    public static abstract class AbstractBinaryExpression extends AbstractExpression {
        protected Expression firstArg, secondArg;
        protected String operation;

        public AbstractBinaryExpression(Expression firstArg, Expression secondArg, String operation) {
            this.firstArg = firstArg;
            this.secondArg = secondArg;
            this.operation = operation;
            this.hash = getString().hashCode();
        }

        public Expression getFirstArg() {
            return firstArg;
        }

        public Expression getSecondArg() {
            return secondArg;
        }

        public String getOperation() {
            return operation;
        }

        public String getString() {
            return "(" + firstArg.getString() + " " + operation + " " + secondArg.getString() + ")";
        }

        public Pair<Boolean, HashMap<String, Expression>> tryMatch(Expression expr) {
            if (expr instanceof NoOperation) {
                return this.tryMatch(((NoOperation) expr).getArgument());
            }
            if (expr instanceof AbstractBinaryExpression) {
                if ((operation.equals(((AbstractBinaryExpression) expr).getOperation()))) {
                    Pair<Boolean, HashMap<String, Expression>> leftResult = firstArg.tryMatch(
                            ((AbstractBinaryExpression) expr).getFirstArg());
                    Pair<Boolean, HashMap<String, Expression>> rightResult = secondArg.tryMatch(
                            ((AbstractBinaryExpression) expr).getSecondArg());
                    if (!leftResult.first)
                        return leftResult;
                    if (!rightResult.first)
                        return rightResult;
                    rightResult.second.forEach((name, value) -> {
                        if (leftResult.second.containsKey(name)) {
                            if (!leftResult.second.get(name).equals(value)) {
                                leftResult.first = false;
                            }
                        } else {
                            leftResult.second.put(name, value);
                        }
                    });
                    return leftResult;
                }
            }
            return new Pair<>(false, new HashMap<>());
        }

        public boolean evaluate(HashMap<String, Boolean> variableValues) {
            return applyOperation(operation, firstArg.evaluate(variableValues), secondArg.evaluate(variableValues));
        }

        public Pair<Proof, Boolean> evaluateProof(HashMap<String, Boolean> variableValues) {
            Proof proof = new Proof();
            Pair<Proof, Boolean> leftRes = firstArg.evaluateProof(variableValues);
            Pair<Proof, Boolean> rightRes = secondArg.evaluateProof(variableValues);
            proof.hypotheses.putAll(leftRes.first.hypotheses);
            proof.hypotheses.putAll(rightRes.first.hypotheses);
            proof.addAll(leftRes.first);
            proof.addAll(rightRes.first);
            proof.addAll(proveOperation(operation,
                    leftRes.second, rightRes.second, firstArg, secondArg));
            return new Pair<>(proof, applyOperation(operation, leftRes.second, rightRes.second));
        }
    }

    public static boolean applyOperation(String symbol, boolean first, boolean second) {
        if (symbol.equals(Operations.LOGICAL_AND.getSymbol())) {
            return first && second;
        }
        if (symbol.equals(Operations.LOGICAL_OR.getSymbol())) {
            return first || second;
        }
        if (symbol.equals(Operations.LOGICAL_IMPLICATION.getSymbol())) {
            return !first || second;
        }
        throw new RuntimeException("Invalid symbol");
    }

    public static class LogicalAND extends AbstractBinaryExpression {
        public LogicalAND(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operations.LOGICAL_AND.getSymbol());
        }
    }

    public static class LogicalOR extends AbstractBinaryExpression {
        public LogicalOR(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operations.LOGICAL_OR.getSymbol());
        }
    }

    public static class LogicalIMPLICATION extends AbstractBinaryExpression {
        public LogicalIMPLICATION(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operations.LOGICAL_IMPLICATION.getSymbol());
        }
    }

    public static abstract class AbstractUnaryExpression extends AbstractExpression {
        protected Expression argument;
        protected String operation;

        public AbstractUnaryExpression(Expression argument, String operation) {
            this.argument = argument;
            this.operation = operation;
            this.hash = getString().hashCode();
        }

        public Expression getArgument() {
            return argument;
        }

        public String getOperation() {
            return operation;
        }

        public String getString() {
            return operation + argument.getString();
        }

        public Pair<Boolean, HashMap<String, Expression>> tryMatch(Expression expr) {
            if (expr instanceof NoOperation) {
                return this.tryMatch(((NoOperation) expr).getArgument());
            }
            if (expr instanceof AbstractUnaryExpression) {
                if (operation.equals(((AbstractUnaryExpression) expr).getOperation())) {
                    return (argument.tryMatch(((AbstractUnaryExpression) expr).getArgument()));
                }
            }
            return new Pair<>(false, new HashMap<>());
        }
    }

    public static class LogicalNOT extends AbstractUnaryExpression {
        public LogicalNOT(Expression argument) {
            super(argument, Operations.LOGICAL_NOT.getSymbol());
        }

        public boolean evaluate(HashMap<String, Boolean> variableValues) {
            return !argument.evaluate(variableValues);
        }

        public Pair<Proof, Boolean> evaluateProof(HashMap<String, Boolean> variableValues) {
            Proof proof = new Proof();
            Pair<Proof, Boolean> result = argument.evaluateProof(variableValues);
            proof.hypotheses.putAll(result.first.hypotheses);
            proof.addAll(result.first);
            if (result.second) {
                proof.addAll(proveOperation(operation,
                        false, false, argument, null));
            }
            return new Pair<>(proof, !result.second);
        }
    }

    public static abstract class AbstractWord extends AbstractExpression {
        protected String name;

        public AbstractWord(String name) {
            this.name = name;
            this.hash = getString().hashCode();
        }

        public String getName() {
            return name;
        }

        public String getString() {
            return name;
        }

        public boolean evaluate(HashMap<String, Boolean> variableValues) {
            if (variableValues.containsKey(name)) {
                return variableValues.get(name);
            }
            throw new RuntimeException("Undefined value of variable " + name);
        }

        public Pair<Proof, Boolean> evaluateProof(HashMap<String, Boolean> variableValues) {
            Proof proof = new Proof();
            if (this.evaluate(variableValues)) {
                proof.hypotheses.put(proof.parser.parseExpression(name), -1);
                proof.output.add(proof.parser.parseExpression(name));
                return new Pair<>(proof, true);
            } else {
                proof.hypotheses.put(proof.parser.parseExpression("!" + name), -1);
                proof.output.add(proof.parser.parseExpression("!" + name));
                return new Pair<>(proof, false);
            }
        }
    }

    public static class Variable extends AbstractWord {
        public Variable(String name) {
            super(name);
        }

        public Pair<Boolean, HashMap<String, Expression>> tryMatch(Expression expr) {
            if (expr instanceof NoOperation) {
                return this.tryMatch(((NoOperation) expr).getArgument());
            }
            if (expr instanceof Variable) {
                return new Pair<>(name.equals(((Variable) expr).getName()), new HashMap<>());
            }
            return new Pair<>(false, new HashMap<>());
        }
    }

    public static class Scheme extends AbstractWord {
        public Scheme(String name) {
            super(name);
        }

        public Pair<Boolean, HashMap<String, Expression>> tryMatch(Expression expr) {
            if (expr instanceof NoOperation) {
                return this.tryMatch(((NoOperation) expr).getArgument());
            }
            HashMap<String, Expression> map = new HashMap<>();
            map.put(name, expr);
            return new Pair<>(true, map);
        }
    }

    enum Bracket implements BracketOrOperation {
        OPEN()
    }

    enum Operations implements ExpressionOrOperation, BracketOrOperation {
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

        private String symbol;
        private int priority;
        private boolean rightAssociative;
        private boolean binary;
        private HashMap<String, Expression> mapOperation;

        Operations(String symbol, int priority, boolean rightAssociative, boolean binary) {
            this.symbol = symbol;
            this.priority = priority;
            this.rightAssociative = rightAssociative;
            this.binary = binary;
            this.mapOperation = new HashMap<>();
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

        public Expression getOperation(Expression firstArg, Expression secondArg) {
            String name;
            if (secondArg != null) {
                name = "(" + firstArg.getString() + " " + getSymbol() + " " + secondArg.getString() + ")";
            } else {
                name = getSymbol() + firstArg.getString();
            }
            if (!mapOperation.containsKey(name)) {
                mapOperation.put(name, build(firstArg, secondArg));
            }
            return mapOperation.get(name);
        }
    }

    enum Axioms {
        AXIOM_1("a -> b -> a"),
        AXIOM_2("(a -> b) -> (a -> b -> c) -> (a -> c)"),
        AXIOM_3("a -> b -> a & b"),
        AXIOM_4("a & b -> a"),
        AXIOM_5("a & b -> b"),
        AXIOM_6("a -> a | b"),
        AXIOM_7("b -> a | b"),
        AXIOM_8("(a -> c) -> (b -> c) -> (a | b -> c)"),
        AXIOM_9("(a -> b) -> (a -> !b) -> !a"),
        AXIOM_10("!!a -> a");

        private String text;

        Axioms(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

//        public String getDisplayName() {
//            return "Ax. sch. " + (this.ordinal() + 1);
//        }
    }

    enum Proofs {
        PROOF_AND_0_0(
                "!x->(x&y)->!x," +
                        "!x," +
                        "(x&y)->!x," +
                        "(x&y)->x," +
                        "((x&y)->x)->((x&y)->!x)->!(x&y)," +
                        "((x&y)->!x)->!(x&y)," +
                        "!(x&y)"),
        PROOF_AND_0_1(PROOF_AND_0_0.getText()),
        PROOF_AND_1_0(
                "!y->(x&y)->!y," +
                        "!y," +
                        "(x&y)->!y," +
                        "(x&y)->y," +
                        "((x&y)->y)->((x&y)->!y)->!(x&y)," +
                        "((x&y)->!y)->!(x&y)," +
                        "!(x&y)"),
        PROOF_AND_1_1(
                "x," +
                        "y," +
                        "x->y->x&y," +
                        "y->x&y," +
                        "x&y"),
        PROOF_OR_0_0(
                "!x," +
                        "!y," +
                        "!x->!y->!x&!y," +
                        "!y->!x&!y," +
                        "!x&!y," +
                        "!x&!y->x|y->!x&!y," +
                        "x|y->!x&!y," +
                        "!x&!y->!x," +
                        "(!x&!y->x)->(!x&!y->!x)->!(!x&!y)," +
                        "x->(!x&!y)->x," +
                        "(!x&!y->!x)->x->(!x&!y->!x)," +
                        "x->(!x&!y->!x)," +
                        "((!x&!y->x)->(!x&!y->!x)->!(!x&!y))->x->((!x&!y->x)->(!x&!y->!x)->!(!x&!y))," +
                        "x->((!x&!y->x)->(!x&!y->!x)->!(!x&!y))," +
                        "(x->(!x&!y)->x)->(x->((!x&!y->x)->(!x&!y->!x)->!(!x&!y)))->(x->((!x&!y->!x)->!(!x&!y)))," +
                        "(x->((!x&!y->x)->(!x&!y->!x)->!(!x&!y)))->(x->((!x&!y->!x)->!(!x&!y)))," +
                        "(x->((!x&!y->!x)->!(!x&!y)))," +
                        "(x->!x&!y->!x)->(x->(!x&!y->!x)->!(!x&!y))->(x->!(!x&!y))," +
                        "(x->(!x&!y->!x)->!(!x&!y))->(x->!(!x&!y))," +
                        "(x->!(!x&!y))," +
                        "!x&!y->!y," +
                        "(!x&!y->y)->(!x&!y->!y)->!(!x&!y)," +
                        "y->!x&!y->y," +
                        "(!x&!y->!y)->y->(!x&!y->!y)," +
                        "y->(!x&!y->!y)," +
                        "((!x&!y->y)->(!x&!y->!y)->!(!x&!y))->y->((!x&!y->y)->(!x&!y->!y)->!(!x&!y))," +
                        "y->((!x&!y->y)->(!x&!y->!y)->!(!x&!y))," +
                        "(y->(!x&!y->y))->(y->(!x&!y->y)->((!x&!y->!y)->!(!x&!y)))->(y->((!x&!y->!y)->!(!x&!y)))," +
                        "(y->(!x&!y->y)->(!x&!y->!y)->!(!x&!y))->y->(!x&!y->!y)->!(!x&!y)," +
                        "y->(!x&!y->!y)->!(!x&!y)," +
                        "(y->(!x&!y->!y))->(y->(!x&!y->!y)->!(!x&!y))->(y->!(!x&!y))," +
                        "(y->(!x&!y->!y)->!(!x&!y))->(y->!(!x&!y))," +
                        "(y->!(!x&!y))," +
                        "(x->!(!x&!y))->(y->!(!x&!y))->x|y->!(!x&!y)," +
                        "(y->!(!x&!y))->x|y->!(!x&!y)," +
                        "x|y->!(!x&!y)," +
                        "(x|y->!x&!y)->(x|y->!(!x&!y))->!(x|y)," +
                        "(x|y->!(!x&!y))->!(x|y)," +
                        "!(x|y)"),
        PROOF_OR_0_1(
                "y," +
                        "y->(x|y)," +
                        "x|y"),
        PROOF_OR_1_0(
                "x," +
                        "x->(x|y)," +
                        "x|y"),
        PROOF_OR_1_1(PROOF_OR_1_0.getText()),
        PROOF_IMPL_0_0(
                "!x," +
                        "!x->!y->!x," +
                        "!y->!x," +
                        "(!y->!x)->x->(!y->!x)," +
                        "x->(!y->!x)," +
                        "x->!y->x," +
                        "(!y->x)->(!y->!x)->!!y," +
                        "((!y->x)->(!y->!x)->!!y)->x->((!y->x)->(!y->!x)->!!y)," +
                        "x->((!y->x)->(!y->!x)->!!y)," +
                        "(x->!y->x)->(x->(!y->x)->(!y->!x)->!!y)->(x->(!y->!x)->!!y)," +
                        "(x->(!y->x)->(!y->!x)->!!y)->(x->(!y->!x)->!!y)," +
                        "(x->(!y->!x)->!!y)," +
                        "(x->!y->!x)->(x->(!y->!x)->!!y)->(x->!!y)," +
                        "(x->(!y->!x)->!!y)->(x->!!y)," +
                        "x->!!y," +
                        "!!y->y," +
                        "(!!y->y)->x->(!!y->y)," +
                        "x->(!!y->y)," +
                        "(x->!!y)->(x->!!y->y)->(x->y)," +
                        "(x->!!y->y)->(x->y)," +
                        "(x->y)"),
        PROOF_IMPL_0_1(
                "y," +
                        "y->x->y," +
                        "x->y"),
        PROOF_IMPL_1_0(
                "x," +
                        "!y," +
                        "(x->y)->(x->y)->(x->y)," +
                        "(x->y)->((x->y)->(x->y))->(x->y)," +
                        "((x->y)->(x->y)->(x->y))->((x->y)->((x->y)->(x->y))->(x->y))->((x->y)->(x->y))," +
                        "((x->y)->((x->y)->(x->y))->(x->y))->((x->y)->(x->y))," +
                        "((x->y)->(x->y))," +
                        "x->(x->y)->x," +
                        "(x->y)->x," +
                        "((x->y)->x)->((x->y)->x->y)->((x->y)->y)," +
                        "((x->y)->x->y)->((x->y)->y)," +
                        "((x->y)->y)," +
                        "!y->(x->y)->!y," +
                        "(x->y)->!y," +
                        "((x->y)->y)->((x->y)->!y)->!(x->y)," +
                        "((x->y)->!y)->!(x->y)," +
                        "!(x->y)"),
        PROOF_IMPL_1_1(PROOF_IMPL_0_1.getText()),
        PROOF_DOUBLE_NOT_1(
                "x," +
                        "x->(!x->x)," +
                        "!x->x," +
                        "!x->(!x->!x)," +
                        "!x->(!x->!x)->!x," +
                        "(!x->(!x->!x))->(!x->(!x->!x)->!x)->(!x->!x)," +
                        "(!x->(!x->!x)->!x)->(!x->!x)," +
                        "(!x->!x)," +
                        "(!x->x)->(!x->!x)->!!x," +
                        "(!x->!x)->!!x," +
                        "!!x");

        private String text;

        Proofs(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public static class FastScanner {
        private BufferedReader reader;

        public FastScanner(InputStream inputStream) {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }

        public String nextLine() {
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Pair<S, T> {
        public S first;
        public T second;

        public Pair(S first, T second) {
            this.first = first;
            this.second = second;
        }
    }

    public static class Troika<S, T, R> {
        public S first;
        public T second;
        public R third;

        public Troika(S first, T second, R third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

    public static class ExprData {
        public int type;
        public Expression expr;
        public Pair<Integer, Integer> numbers;

        public ExprData(int type, Expression expr) {
            this.type = type;
            this.expr = expr;
        }

        public ExprData(int type, Expression expr, Pair<Integer, Integer> numbers) {
            this.type = type;
            this.expr = expr;
            this.numbers = numbers;
        }

        public boolean isAxiom() {
            return (type == 0);
        }

        public boolean isHypothesis() {
            return (type == 1);
        }

        public boolean isAlphaHypothesis() {
            return (type == 3);
        }

        public boolean isMP() {
            return (type == 2);
        }
    }
}