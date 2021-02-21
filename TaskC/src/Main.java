import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static boolean myDebug = false;

    public static void main(String[] args) {
        if (myDebug) {

            try {
                Writer output2 = new BufferedWriter(new FileWriter("logs.txt"));

                for (int i = 0; i < 1000; i++) {
                    FastScanner input = new FastScanner(new FileInputStream("in/" + i + ".txt"));
                    Writer output = new BufferedWriter(new FileWriter("my_out/" + i + ".txt"));
                    solve(input, output);
                    output.close();
                }

                for (int i = 0; i < 1000; i++) {
                    FastScanner input1 = new FastScanner(new FileInputStream("out/" + i + ".txt"));
                    FastScanner input2 = new FastScanner(new FileInputStream("my_out/" + i + ".txt"));

                    String line1 = input1.nextLine();
                    String line2 = input2.nextLine();
                    int lineNum = 1;

                    while (line1 != null && line2 != null) {
                        if (!line1.equals(line2)) {
                            output2.write(i + " " + lineNum + '\n');
                            break;
                        }

                        lineNum++;
                        line1 = input1.nextLine();
                        line2 = input2.nextLine();
                    }
                }

                output2.close();
            } catch (FileNotFoundException e) {
                //ignored
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {

            try {
                FastScanner input = new FastScanner(System.in);
                Writer output = new PrintWriter(System.out);
                solve(input, output);
                output.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static void solve(FastScanner input, Writer output) throws IOException {

        String line = input.nextLine();
        if (line == null) {
            output.write("Proof is incorrect");
            return;
        }

        String[] parts = line.split("\\|-");
        if (parts.length != 2) {
            throw new RuntimeException("Incorrect statement");
        }

        Expression statement = parseExpression(parts[1]);

        if (myDebug) {
            output.write("./parser");
            output.write('\n');
        }

        output.write("|-");
        output.write(statement.getString());
        output.write('\n');

        Accumulator accumulator = new Accumulator();

        boolean lastIsStatement = false;
        int lineNum = 0;

        for (line = input.nextLine(); line != null; line = input.nextLine()) {
            Expression expr = parseExpression(line);

            lastIsStatement = expr.equals(statement);

            boolean isFound = false;
            lineNum++;

            HashMap<Integer, String> errors = new HashMap<>();

            int axiomNum = 20;
            int axiomType = 20;

            for (Axioms axiom : Axioms.values()) {
                Expression pattern = axiom.getPattern();
                Triple<Boolean, HashMap<Expression, Expression>, Boolean> result = pattern.tryCompareAndCheck(
                        expr, false, false, null, new HashMap<>());
                if (result.first) {
                    axiomNum = (axiom.ordinal() + 1);
                    axiomType = 1;
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                Triple<Boolean, String, String> tmpRes = isAxiom11(expr);
                if (tmpRes.first) {
                    axiomNum = 11;
                    axiomType = 1;
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                    isFound = true;
                } else {
                    if (tmpRes.second != null) {
                        errors.put(2, "Expression " + lineNum + ": variable " + tmpRes.second + " is not free for term " + tmpRes.third + " in ?@-axiom.");
                    }
                }
            }

            if (!isFound) {
                Triple<Boolean, String, String> tmpRes = isAxiom12(expr);
                if (tmpRes.first) {
                    axiomNum = 12;
                    axiomType = 1;
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                    isFound = true;
                } else {
                    if (tmpRes.second != null) {
                        errors.put(2, "Expression " + lineNum + ": variable " + tmpRes.second + " is not free for term " + tmpRes.third + " in ?@-axiom.");
                    }
                }
            }

            for (ArithmeticAxioms axiom : ArithmeticAxioms.values()) {
                Expression pattern = axiom.getPattern();
                Triple<Boolean, HashMap<Expression, Expression>, Boolean> result = pattern.tryCompareAndCheck(
                        expr, false, true, null, new HashMap<>());
                if (result.first) {
                    if (axiomNum > (axiom.ordinal() + 1)) {
                        axiomNum = (axiom.ordinal() + 1);
                        axiomType = 2;
                    }
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                    isFound = true;
                    break;
                }
            }

            if (isAxiom9(expr)) {
                if (axiomNum > 9) {
                    axiomNum = 9;
                    axiomType = 3;
                }
                accumulator.putProven(expr, lineNum);
                accumulator.tryPutForMP(expr, lineNum);
                isFound = true;
            }

            if (isFound) {
                if (axiomType == 1) {
                    output.write("[" + lineNum + ". Ax. sch. " + axiomNum + "] ");
                    output.write(expr.getString());
                    output.write('\n');
                }
                if (axiomType == 2) {
                    output.write("[" + lineNum + ". Ax. A" + axiomNum + "] ");
                    output.write(expr.getString());
                    output.write('\n');
                }
                if (axiomType == 3) {
                    output.write("[" + lineNum + ". Ax. sch. A" + axiomNum + "] ");
                    output.write(expr.getString());
                    output.write('\n');
                }
            }

            if (!isFound) {
                Pair<Integer, Integer> result = accumulator.isMP(expr);
                if (result.first != -1) {
                    output.write("[" + lineNum + ". M.P. " + result.second + ", " + result.first + "] ");
                    output.write(expr.getString());
                    output.write('\n');
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                    isFound = true;
                }
            }

            if (!isFound) {
                Triple<Boolean, Integer, String> result = accumulator.isQuantifiedRule(expr);
                if (result.first) {
                    output.write("[" + lineNum + ". ?@-intro " + result.second + "] ");
                    output.write(expr.getString());
                    output.write('\n');
                    accumulator.putProven(expr, lineNum);
                    accumulator.tryPutForMP(expr, lineNum);
                    isFound = true;
                } else {
                    if (result.third != null) {
                        errors.put(1, "Expression " + lineNum + ": " + result.third);
                    }
                }
            }

            if (!isFound) {
                errors.put(3, "Expression " + lineNum + " is not proved.");
                for (Map.Entry<Integer, String> pair : errors.entrySet()) {
                    output.write(pair.getValue());
                    output.write('\n');
                    return;
                }
            }
        }

        if (!lastIsStatement) {
            output.write("The proof proves different expression.");
            output.write('\n');
        }
    }

    private static Expression parseExpression(String line) {
        ArrayList<ExpressionOrOperation> list = new ArrayList<>();
        ArrayDeque<BracketOrOperation> stack = new ArrayDeque<>();
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) >= 'A' && line.charAt(i) <= 'Z') {
                int j = i;
                while ((j < line.length()) && (
                        (line.charAt(j) >= 'A' && line.charAt(j) <= 'Z') ||
                                (line.charAt(j) >= '0' && line.charAt(j) <= '9') ||
                                (line.charAt(j) == '\''))) j++;
                list.add(new Variable(line.substring(i, j)));
                i = j - 1;
                continue;
            }
            if (line.charAt(i) == '0' || (line.charAt(i) >= 'a' && line.charAt(i) <= 'z')) {
                list.add(new Variable(line.substring(i, i + 1)));
                continue;
            }
            if (line.charAt(i) == '$' && line.charAt(i + 1) >= 'a' && line.charAt(i + 1) <= 'z') {
                list.add(new Variable(line.substring(i, i + 2)));
                i++;
                continue;
            }
            Operation op = null;
            if (line.charAt(i) == '\'') {
                op = Operation.INCREMENT;
            }
            if (line.charAt(i) == '*') {
                op = Operation.MULTIPLICATION;
            }
            if (line.charAt(i) == '+') {
                op = Operation.SUMMATION;
            }
            if (line.charAt(i) == '=') {
                op = Operation.EQUALITY;
            }
            if (line.charAt(i) == '!') {
                op = Operation.LOGICAL_NOT;
            }
            if (line.charAt(i) == '&') {
                op = Operation.LOGICAL_AND;
            }
            if (line.charAt(i) == '|') {
                op = Operation.LOGICAL_OR;
            }
            if (line.charAt(i) == '-' && line.charAt(i + 1) == '>') {
                op = Operation.LOGICAL_IMPLICATION;
                i += 1;
            }
            if (op != null && !op.isBinary()) {
                if (op.rightAssociativeOrUnaryPostfix) {
                    list.add(op);
                } else {
                    stack.push(op);
                }
                continue;
            }
            if (op != null) {
                while (!stack.isEmpty() && (stack.peek() instanceof Operation) && (op.isRightAssociativeOrUnaryPostfix()
                        ? (((Operation) Objects.requireNonNull(stack.peek())).getPriority() > op.getPriority())
                        : (((Operation) Objects.requireNonNull(stack.peek())).getPriority() >= op.getPriority()))) {
                    list.add((Operation) stack.pop());
                }
                stack.push(op);
                continue;
            }
            if (line.charAt(i) == '@') {
                stack.push(new Quantifier("@", line.substring(i + 1, i + 2)));
                i += 2;
                continue;
            }
            if (line.charAt(i) == '?') {
                stack.push(new Quantifier("?", line.substring(i + 1, i + 2)));
                i += 2;
                continue;
            }
            if (line.charAt(i) == '(') {
                stack.push(Bracket.OPEN);
                continue;
            }
            if (line.charAt(i) == ')') {
                while (!stack.isEmpty() && (stack.peek() instanceof Operation || stack.peek() instanceof Quantifier)) {
                    if (stack.peek() instanceof Operation) {
                        list.add((Operation) stack.pop());
                    } else {
                        list.add((Quantifier) stack.pop());
                    }
                }
                if (stack.isEmpty()) {
                    throw new RuntimeException("Opening bracket is missing in expression");
                }
                stack.pop();
                continue;
            }
            throw new RuntimeException("Invalid character in expression: " + line + " in position: " + i);
        }
        while (!stack.isEmpty()) {
            if (stack.peek() instanceof Bracket) {
                throw new RuntimeException("Closing bracket is missing in expression");
            }
            if (stack.peek() instanceof Operation) {
                list.add((Operation) stack.pop());
            } else {
                list.add((Quantifier) stack.pop());
            }
        }
        ArrayDeque<Expression> expressions = new ArrayDeque<>();
        for (ExpressionOrOperation elem : list) {
            if (elem instanceof Expression) {
                expressions.addLast((Expression) elem);
            } else {
                if (elem instanceof Operation) {
                    Expression firstArg, secondArg = null;
                    if (((Operation) elem).isBinary()) {
                        secondArg = expressions.removeLast();
                    }
                    firstArg = expressions.removeLast();
                    expressions.addLast(((Operation) elem).build(firstArg, secondArg));
                } else {
                    Expression firstArg = expressions.removeLast();
                    expressions.addLast(((Quantifier) elem).build(firstArg));
                }
            }
        }
        if (expressions.size() != 1) {
            throw new RuntimeException("Incorrect postfix notation: " + list.toString());
        }
        return expressions.removeLast();
    }

    public static class Accumulator {
        private HashMap<Expression, Integer> proven;
        private HashMap<Expression, HashMap<Expression, Integer>> supposed;

        public Accumulator() {
            this.proven = new HashMap<>();
            this.supposed = new HashMap<>();
        }

        public void putProven(Expression expr, int lineNum) {
            proven.put(expr, lineNum);
        }

        public void tryPutForMP(Expression expr, int lineNum) {
            if (expr instanceof AbstractBinaryExpression) {
                AbstractBinaryExpression node = (AbstractBinaryExpression) expr;
                if (node.getOperation().equals(Operation.LOGICAL_IMPLICATION.getSymbol())) {
                    HashMap<Expression, Integer> list;
                    if (supposed.containsKey(node.getSecondArg())) {
                        list = supposed.get(node.getSecondArg());
                    } else {
                        list = new HashMap<>();
                    }
                    list.put(node.getFirstArg(), lineNum);
                    supposed.put(node.getSecondArg(), list);
                }
            }
        }

        public Pair<Integer, Integer> isMP(Expression expr) {
            Pair<Integer, Integer> result = new Pair<>(-1, -1);
            if (supposed.containsKey(expr)) {
                HashMap<Expression, Integer> list = supposed.get(expr);
                list.forEach(((expression, integer) -> {
                    if (proven.containsKey(expression)) {
                        Integer ind = proven.get(expression);
                        if (result.second < ind || result.second.equals(ind) && result.first < integer) {
                            result.first = integer;
                            result.second = ind;
                        }
                    }
                }));
            }
            return result;
        }

        public Triple<Boolean, Integer, String> isQuantifiedRule(Expression expr) {
            Triple<Boolean, Integer, String> res = new Triple<>(false, null, null);
            if (expr instanceof LogicalIMPLICATION) {
                Expression firstArg = ((LogicalIMPLICATION) expr).getFirstArg();
                Expression secondArg = ((LogicalIMPLICATION) expr).getSecondArg();

                if (firstArg instanceof QuantifiedExpression &&
                        "?".equals(((QuantifiedExpression) firstArg).getOperation())) {
                    String variable = ((QuantifiedExpression) firstArg).getVariable();

                    Expression exprForSearch = Operation.LOGICAL_IMPLICATION.build(
                            ((QuantifiedExpression) firstArg).getArgument(), secondArg);

                    if (proven.containsKey(exprForSearch)) {
                        HashSet<String> freeVariables = secondArg.getFreeVariables();
                        if (!freeVariables.contains(variable)) {
                            return new Triple<>(true, proven.get(exprForSearch), null);
                        } else {
                            res = new Triple<>(false, proven.get(exprForSearch),
                                    "variable " + variable + " occurs free in ?@-rule.");
                        }
                    }
                }

                if (secondArg instanceof QuantifiedExpression &&
                        "@".equals(((QuantifiedExpression) secondArg).getOperation())) {
                    String variable = ((QuantifiedExpression) secondArg).getVariable();

                    Expression exprForSearch = Operation.LOGICAL_IMPLICATION.build(
                            firstArg, ((QuantifiedExpression) secondArg).getArgument());

                    if (proven.containsKey(exprForSearch)) {
                        HashSet<String> freeVariables = firstArg.getFreeVariables();
                        if (!freeVariables.contains(variable)) {
                            return new Triple<>(true, proven.get(exprForSearch), null);
                        } else {
                            if (res.second == null) {
                                res = new Triple<>(false, proven.get(exprForSearch),
                                        "variable " + variable + " occurs free in ?@-rule.");
                            }
                        }
                    }
                }
            }
            return res;
        }
    }

    public interface BracketOrOperation {
    }

    public interface ExpressionOrOperation {
    }

    public interface Expression extends ExpressionOrOperation {
        String getString();

        Triple<Boolean, HashMap<Expression, Expression>, Boolean> tryCompareAndCheck(
                Expression expr, Boolean lowMatch, Boolean absoluteMatch, String variable, HashMap<String, Integer> boundVariables);

        HashSet<String> getFreeVariables();
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
            if (ob instanceof AbstractExpression) {
                if (hashCode() != ob.hashCode())
                    return false;
                return getString().equals(((AbstractExpression) ob).getString());
                //return true;
            }
            return false;
        }
    }

    public static abstract class AbstractBinaryExpression extends AbstractExpression {
        protected Expression firstArg, secondArg;
        protected String operation;

        public AbstractBinaryExpression(Expression firstArg, Expression secondArg, String operation) {
            this.firstArg = firstArg;
            this.secondArg = secondArg;
            this.operation = operation;
            this.hash = (firstArg.hashCode() * 37 + secondArg.hashCode()) * 37 + operation.hashCode();
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
            return "(" + firstArg.getString() + operation + secondArg.getString() + ")";
        }

        public Triple<Boolean, HashMap<Expression, Expression>, Boolean> tryCompareAndCheck(
                Expression expr, Boolean lowMatch, Boolean absoluteMatch, String variable, HashMap<String, Integer> boundVariables) {
            if (expr instanceof AbstractBinaryExpression &&
                    operation.equals(((AbstractBinaryExpression) expr).getOperation())) {
                Triple<Boolean, HashMap<Expression, Expression>, Boolean> leftResult = firstArg.tryCompareAndCheck(
                        ((AbstractBinaryExpression) expr).getFirstArg(), lowMatch, absoluteMatch, variable, boundVariables);
                Triple<Boolean, HashMap<Expression, Expression>, Boolean> rightResult = secondArg.tryCompareAndCheck(
                        ((AbstractBinaryExpression) expr).getSecondArg(), lowMatch, absoluteMatch, variable, boundVariables);
                if (!leftResult.first)
                    return leftResult;
                if (!rightResult.first)
                    return rightResult;

                for (Map.Entry<Expression, Expression> pair : rightResult.second.entrySet()) {
                    if (leftResult.second.containsKey(pair.getKey())) {
                        if (!leftResult.second.get(pair.getKey()).equals(pair.getValue())) {
                            leftResult.first = false;
                            break;
                        }
                    } else {
                        leftResult.second.put(pair.getKey(), pair.getValue());
                    }
                }

                leftResult.third = leftResult.third || rightResult.third;
                return leftResult;
            } else {
                HashMap<Expression, Expression> map = new HashMap<>();
                map.put(this, expr);
                return new Triple<>(lowMatch, map, false);
            }
        }

        public HashSet<String> getFreeVariables() {
            HashSet<String> freeVariables = firstArg.getFreeVariables();
            freeVariables.addAll(secondArg.getFreeVariables());
            return freeVariables;
        }
    }

    public static class LogicalAND extends AbstractBinaryExpression {
        public LogicalAND(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operation.LOGICAL_AND.getSymbol());
        }
    }

    public static class LogicalOR extends AbstractBinaryExpression {
        public LogicalOR(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operation.LOGICAL_OR.getSymbol());
        }
    }

    public static class LogicalIMPLICATION extends AbstractBinaryExpression {
        public LogicalIMPLICATION(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operation.LOGICAL_IMPLICATION.getSymbol());
        }
    }

    public static class Multiplication extends AbstractBinaryExpression {
        public Multiplication(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operation.MULTIPLICATION.getSymbol());
        }
    }

    public static class Summation extends AbstractBinaryExpression {
        public Summation(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operation.SUMMATION.getSymbol());
        }
    }

    public static class Equality extends AbstractBinaryExpression {
        public Equality(Expression firstArg, Expression secondArg) {
            super(firstArg, secondArg, Operation.EQUALITY.getSymbol());
        }
    }

    public static abstract class AbstractUnaryExpression extends AbstractExpression {
        protected Expression argument;
        protected String operation;

        public AbstractUnaryExpression(Expression argument, String operation) {
            this.argument = argument;
            this.operation = operation;
            this.hash = argument.hashCode() * 37 + operation.hashCode();
        }

        public Expression getArgument() {
            return argument;
        }

        public String getOperation() {
            return operation;
        }

        public Triple<Boolean, HashMap<Expression, Expression>, Boolean> tryCompareAndCheck(
                Expression expr, Boolean lowMatch, Boolean absoluteMatch, String variable, HashMap<String, Integer> boundVariables) {
            if (expr instanceof AbstractUnaryExpression &&
                    operation.equals(((AbstractUnaryExpression) expr).getOperation())) {
                return argument.tryCompareAndCheck(((AbstractUnaryExpression) expr).getArgument(),
                        lowMatch, absoluteMatch, variable, boundVariables);
            } else {
                HashMap<Expression, Expression> map = new HashMap<>();
                HashSet<String> set = new HashSet<>();
                map.put(this, expr);
                return new Triple<>(lowMatch, map, false);
            }
        }

        public HashSet<String> getFreeVariables() {
            return argument.getFreeVariables();
        }
    }

    public static class LogicalNOT extends AbstractUnaryExpression {
        public LogicalNOT(Expression argument) {
            super(argument, Operation.LOGICAL_NOT.getSymbol());
        }

        public String getString() {
            return "(" + operation + argument.getString() + ")";
        }
    }

    public static class Increment extends AbstractUnaryExpression {
        public Increment(Expression argument) {
            super(argument, Operation.INCREMENT.getSymbol());
        }

        public String getString() {
            return argument.getString() + operation;
        }
    }

    public static class QuantifiedExpression extends AbstractExpression {
        private Expression argument;
        private String operation;
        private String variable;

        public QuantifiedExpression(Expression argument, Quantifier quantifier) {
            this.argument = argument;
            this.operation = quantifier.getName();
            this.variable = quantifier.getVariable();
            this.hash = (argument.hashCode() * 37 + operation.hashCode()) * 37 + variable.hashCode();
        }

        public String getString() {
            return "(" + operation + variable + "." + argument.getString() + ")";
        }

        public Expression getArgument() {
            return argument;
        }

        public String getOperation() {
            return operation;
        }

        public String getVariable() {
            return variable;
        }

        public Triple<Boolean, HashMap<Expression, Expression>, Boolean> tryCompareAndCheck(
                Expression expr, Boolean lowMatch, Boolean absoluteMatch, String variable, HashMap<String, Integer> boundVariables) {
            if (boundVariables.containsKey(this.variable)) {
                Integer counter = boundVariables.get(this.variable);
                counter++;
                boundVariables.put(this.variable, counter);
            } else {
                boundVariables.put(this.variable, 1);
            }

            if (this.variable.equals(variable)) {
                lowMatch = false;
                absoluteMatch = true;
            }

            if (expr instanceof QuantifiedExpression &&
                    operation.equals(((QuantifiedExpression) expr).getOperation()) &&
                    this.variable.equals(((QuantifiedExpression) expr).getVariable())) {
                Triple<Boolean, HashMap<Expression, Expression>, Boolean> result =
                        argument.tryCompareAndCheck(((QuantifiedExpression) expr).getArgument(),
                                lowMatch, absoluteMatch, variable, boundVariables);

                if (boundVariables.containsKey(this.variable)) {
                    Integer counter = boundVariables.get(this.variable);
                    counter--;
                    if (counter == 0) {
                        boundVariables.remove(this.variable);
                    } else {
                        boundVariables.put(this.variable, counter);
                    }
                }

                return result;
            } else {
                if (boundVariables.containsKey(this.variable)) {
                    Integer counter = boundVariables.get(this.variable);
                    counter--;
                    if (counter == 0) {
                        boundVariables.remove(this.variable);
                    } else {
                        boundVariables.put(this.variable, counter);
                    }
                }

                HashMap<Expression, Expression> map = new HashMap<>();
                map.put(this, expr);
                return new Triple<>(lowMatch, map, false);
            }
        }

        public HashSet<String> getFreeVariables() {
            HashSet<String> freeVariables = argument.getFreeVariables();
            freeVariables.remove(variable);
            return freeVariables;
        }
    }

    public static abstract class AbstractWord extends AbstractExpression {
        protected String name;

        public AbstractWord(String name) {
            this.name = name;
            this.hash = name.hashCode();
        }

        public String getName() {
            return name;
        }

        public String getString() {
            return name;
        }
    }

    public static class Variable extends AbstractWord {
        public Variable(String name) {
            super(name);
        }

        public Triple<Boolean, HashMap<Expression, Expression>, Boolean> tryCompareAndCheck(
                Expression expr, Boolean lowMatch, Boolean absoluteMatch, String variable, HashMap<String, Integer> boundVariables) {
            if (this.name.equals(variable) && !absoluteMatch) {
                HashSet<String> freeVariables = expr.getFreeVariables();
                boolean notFreeVariables = false;

                for (Map.Entry<String, Integer> pair : boundVariables.entrySet()) {
                    if (freeVariables.contains(pair.getKey())) {
                        notFreeVariables = true;
                        break;
                    }
                }

                HashMap<Expression, Expression> map = new HashMap<>();
                map.put(this, expr);
                return new Triple<>(true, map, notFreeVariables);
            }

            if (expr instanceof Variable &&
                    name.equals(((Variable) expr).getName())) {
                return new Triple<>(true, new HashMap<>(), false);
            } else {
                HashMap<Expression, Expression> map = new HashMap<>();
                map.put(this, expr);
                return new Triple<>(!absoluteMatch, map, false);
            }
        }

        public HashSet<String> getFreeVariables() {
            HashSet<String> freeVariables = new HashSet<>();
            freeVariables.add(name);
            return freeVariables;
        }
    }

    public static class Quantifier implements BracketOrOperation, ExpressionOrOperation {
        private String name;
        private String variable;

        public Quantifier(String name, String variable) {
            this.name = name;
            this.variable = variable;
        }

        public String getName() {
            return name;
        }

        public String getVariable() {
            return variable;
        }

        public Expression build(Expression firstArg) {
            return new QuantifiedExpression(firstArg, this);
        }
    }

    enum Bracket implements BracketOrOperation {
        OPEN()
    }

    enum Operation implements ExpressionOrOperation, BracketOrOperation {
        LOGICAL_IMPLICATION("->", 2, true, true) {
            public Expression build(Expression firstArg, Expression secondArg) {
                return new LogicalIMPLICATION(firstArg, secondArg);
            }
        },
        LOGICAL_OR("|", 3, false, true) {
            public Expression build(Expression firstArg, Expression secondArg) {
                return new LogicalOR(firstArg, secondArg);
            }
        },
        LOGICAL_AND("&", 4, false, true) {
            public Expression build(Expression firstArg, Expression secondArg) {
                return new LogicalAND(firstArg, secondArg);
            }
        },
        LOGICAL_NOT("!", 5, false, false) {
            public Expression build(Expression firstArg, Expression secondArg) {
                return new LogicalNOT(firstArg);
            }
        },
        EQUALITY("=", 6, false, true) {
            public Expression build(Expression firstArg, Expression secondArg) {
                return new Equality(firstArg, secondArg);
            }
        },
        SUMMATION("+", 7, false, true) {
            public Expression build(Expression firstArg, Expression secondArg) {
                return new Summation(firstArg, secondArg);
            }
        },
        MULTIPLICATION("*", 8, false, true) {
            public Expression build(Expression firstArg, Expression secondArg) {
                return new Multiplication(firstArg, secondArg);
            }
        },
        INCREMENT("'", 9, true, false) {
            public Expression build(Expression firstArg, Expression secondArg) {
                return new Increment(firstArg);
            }
        };

        private String symbol;
        private int priority;
        private boolean rightAssociativeOrUnaryPostfix;
        private boolean binary;

        Operation(String symbol, int priority, boolean rightAssociativeOrUnaryPostfix, boolean binary) {
            this.symbol = symbol;
            this.priority = priority;
            this.rightAssociativeOrUnaryPostfix = rightAssociativeOrUnaryPostfix;
            this.binary = binary;
        }

        public String getSymbol() {
            return symbol;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isRightAssociativeOrUnaryPostfix() {
            return rightAssociativeOrUnaryPostfix;
        }

        public boolean isBinary() {
            return binary;
        }

        public abstract Expression build(Expression firstArg, Expression secondArg);
    }

    enum Axioms {
        AXIOM_1("$a -> $b -> $a"),
        AXIOM_2("($a -> $b) -> ($a -> $b -> $c) -> ($a -> $c)"),
        AXIOM_3("$a & $b -> $a"),
        AXIOM_4("$a & $b -> $b"),
        AXIOM_5("$a -> $b -> $a & $b"),
        AXIOM_6("$a -> $a | $b"),
        AXIOM_7("$b -> $a | $b"),
        AXIOM_8("($a -> $c) -> ($b -> $c) -> ($a | $b -> $c)"),
        AXIOM_9("($a -> $b) -> ($a -> !$b) -> !$a"),
        AXIOM_10("!!$a -> $a");

        private String text;

        Axioms(String text) {
            this.text = text;
        }

        public Expression getPattern() {
            return parseExpression(text.replaceAll("\\s", ""));
        }
    }

    enum ArithmeticAxioms {
        ARITHMETIC_AXIOM_1("a = b -> a = c -> b = c"),
        ARITHMETIC_AXIOM_2("a = b -> a' = b'"),
        ARITHMETIC_AXIOM_3("a' = b' -> a = b"),
        ARITHMETIC_AXIOM_4("!(a' = 0)"),
        ARITHMETIC_AXIOM_5("a + 0 = a"),
        ARITHMETIC_AXIOM_6("a + b' = (a + b)'"),
        ARITHMETIC_AXIOM_7("a * 0 = 0"),
        ARITHMETIC_AXIOM_8("a * b' = a * b + a");

        private String text;

        ArithmeticAxioms(String text) {
            this.text = text;
        }

        public Expression getPattern() {
            return parseExpression(text.replaceAll("\\s", ""));
        }
    }

    public static Triple<Boolean, String, String> isAxiom11or12(Expression quantifiedExpr, Expression expression, String operation) {
        Triple<Boolean, String, String> result = new Triple<>(false, null, null);
        if (quantifiedExpr instanceof QuantifiedExpression &&
                operation.equals(((QuantifiedExpression) quantifiedExpr).getOperation())) {
            String variable = ((QuantifiedExpression) quantifiedExpr).getVariable();
            Expression argument = ((QuantifiedExpression) quantifiedExpr).getArgument();
            Triple<Boolean, HashMap<Expression, Expression>, Boolean> res =
                    argument.tryCompareAndCheck(expression, false, false, variable, new HashMap<>());
            if (!res.first) {
                return new Triple<>(false, null, null);
            }

            for (Map.Entry<Expression, Expression> pair : res.second.entrySet()) {
                if (!(pair.getKey() instanceof Variable && ((Variable) pair.getKey()).getName().equals(variable))) {
                    res.first = false;
                    break;
                } else {
                    result.second = variable;
                    result.third = pair.getValue().getString();
                }
            }

            if (!res.first) {
                return new Triple<>(false, null, null);
            }
            if (res.third) {
                res.first = false;
            }
            result.first = res.first;
            return result;
        }
        return result;
    }

    public static Triple<Boolean, String, String> isAxiom11(Expression expr) {
        if (expr instanceof LogicalIMPLICATION) {
            Expression firstArg = ((LogicalIMPLICATION) expr).getFirstArg();
            Expression secondArg = ((LogicalIMPLICATION) expr).getSecondArg();
            return isAxiom11or12(firstArg, secondArg, "@");
        }
        return new Triple<>(false, null, null);
    }

    public static Triple<Boolean, String, String> isAxiom12(Expression expr) {
        if (expr instanceof LogicalIMPLICATION) {
            Expression firstArg = ((LogicalIMPLICATION) expr).getFirstArg();
            Expression secondArg = ((LogicalIMPLICATION) expr).getSecondArg();
            return isAxiom11or12(secondArg, firstArg, "?");
        }
        return new Triple<>(false, null, null);
    }

    public static boolean isAxiom9(Expression expr) {
        if (expr instanceof LogicalIMPLICATION) {
            Expression andExpr = ((LogicalIMPLICATION) expr).getFirstArg();
            Expression phiExpr = ((LogicalIMPLICATION) expr).getSecondArg();
            if (andExpr instanceof LogicalAND) {
                Expression leftPhiExpr = ((LogicalAND) andExpr).getFirstArg();
                Expression quantifiedExpr = ((LogicalAND) andExpr).getSecondArg();
                if (quantifiedExpr instanceof QuantifiedExpression &&
                        "@".equals(((QuantifiedExpression) quantifiedExpr).getOperation())) {
                    Expression argument = ((QuantifiedExpression) quantifiedExpr).getArgument();
                    String variable = ((QuantifiedExpression) quantifiedExpr).getVariable();
                    if (argument instanceof LogicalIMPLICATION) {
                        Expression tooPhiExpr = ((LogicalIMPLICATION) argument).getFirstArg();
                        Expression rightPhiExpr = ((LogicalIMPLICATION) argument).getSecondArg();

                        Triple<Boolean, HashMap<Expression, Expression>, Boolean> res1 =
                                phiExpr.tryCompareAndCheck(tooPhiExpr, false, true, null, new HashMap<>());
                        if (!res1.first) {
                            return false;
                        }

                        Triple<Boolean, HashMap<Expression, Expression>, Boolean> res2 =
                                phiExpr.tryCompareAndCheck(leftPhiExpr, false, false, variable, new HashMap<>());

                        for (Map.Entry<Expression, Expression> pair : res2.second.entrySet()) {
                            if (pair.getKey() instanceof Variable && ((Variable) pair.getKey()).getName().equals(variable)) {
                                if (!(pair.getValue() instanceof Variable && "0".equals(((Variable) pair.getValue()).getName()))) {
                                    res2.first = false;
                                    break;
                                }
                            } else {
                                res2.first = false;
                                break;
                            }
                        }

                        if (res2.third) {
                            res2.first = false;
                        }

                        if (!res2.first) {
                            return false;
                        }

                        Triple<Boolean, HashMap<Expression, Expression>, Boolean> res3 =
                                phiExpr.tryCompareAndCheck(rightPhiExpr, false, false, variable, new HashMap<>());

                        for (Map.Entry<Expression, Expression> pair : res3.second.entrySet()) {
                            if (pair.getKey() instanceof Variable && ((Variable) pair.getKey()).getName().equals(variable)) {
                                if (pair.getValue() instanceof Increment) {
                                    Expression var = ((Increment) pair.getValue()).getArgument();
                                    if (!(var instanceof Variable && ((Variable) var).getName().equals(variable))) {
                                        res3.first = false;
                                        break;
                                    }
                                } else {
                                    res3.first = false;
                                    break;
                                }
                            } else {
                                res3.first = false;
                                break;
                            }
                        }

                        return res3.first;
                    }
                }
            }
        }
        return false;
    }

    public static class FastScanner {
        private BufferedReader reader;

        public FastScanner(InputStream inputStream) {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }

        public String nextLine() {
            try {
                String line = reader.readLine();
                return (line != null) ? line.replaceAll("\\s", "") : null;
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

    public static class Triple<R, S, T> {
        public R first;
        public S second;
        public T third;

        public Triple(R first, S second, T third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
}