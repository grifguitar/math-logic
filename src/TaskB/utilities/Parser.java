package TaskB.utilities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;

public class Parser {
    public static Expression parseExpression(String line) {
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
            if (line.charAt(i) >= 'a' && line.charAt(i) <= 'z') {
                list.add(new Scheme(line.substring(i, i + 1)));
                continue;
            }
            Operation op = null;
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
                stack.push(op);
                continue;
            }
            if (op != null) {
                while (!stack.isEmpty() && (stack.peek() instanceof Operation) && (op.isRightAssociative()
                        ? (((Operation) Objects.requireNonNull(stack.peek())).getPriority() > op.getPriority())
                        : (((Operation) Objects.requireNonNull(stack.peek())).getPriority() >= op.getPriority()))) {
                    list.add((Operation) stack.pop());
                }
                stack.push(op);
                continue;
            }
            if (line.charAt(i) == '(') {
                stack.push(Bracket.OPEN);
                continue;
            }
            if (line.charAt(i) == ')') {
                while (!stack.isEmpty() && stack.peek() instanceof Operation) {
                    list.add((Operation) stack.pop());
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
            list.add((Operation) stack.pop());
        }
        ArrayDeque<Expression> expressions = new ArrayDeque<>();
        for (ExpressionOrOperation elem : list) {
            if (elem instanceof Expression) {
                expressions.addLast((Expression) elem);
            } else {
                Expression firstArg, secondArg = null;
                if (((Operation) elem).isBinary()) {
                    secondArg = expressions.removeLast();
                }
                firstArg = expressions.removeLast();
                expressions.addLast(((Operation) elem).build(firstArg, secondArg));
            }
        }
        if (expressions.size() != 1) {
            throw new RuntimeException("Incorrect postfix notation: " + list.toString());
        }
        return expressions.removeLast();
    }
}
