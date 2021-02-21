package TaskB;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import TaskB.utilities.*;

import static TaskB.utilities.Parser.parseExpression;
import static TaskB.utilities.Alphabet.*;

public class Main {
    public static void main(String[] args) {
        try {
            FastScanner input = new FastScanner(System.in);
            Writer output = new PrintWriter(System.out);
            solve(input, output);
            output.close();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void solve(FastScanner in, Writer out) throws IOException, ParseException {
        HashMap<Expression, Integer> hypotheses = new HashMap<>();

        String line = in.nextLine();
        if (line == null) {
            out.write(INCORRECT_PROOF);
            return;
        }

        String[] parts = line.split("\\|-");
        if (parts.length != 2) {
            throw new ParseException("Incorrect statement", 0);
        }

        StringBuilder firstLine = new StringBuilder();

        if (!parts[0].isEmpty()) {
            String[] context = parts[0].split(",");
            for (int i = 0; i < context.length; i++) {
                Expression hyp = parseExpression(context[i]);
                hypotheses.put(hyp, i + 1);
                firstLine.append(hyp.convertToString());
                if (i < context.length - 1) {
                    firstLine.append(COMMA);
                } else {
                    firstLine.append(SPACE);
                }
            }
        }

        Expression statement = parseExpression(parts[1]);
        firstLine.append(DEDUCE);
        firstLine.append(statement.convertToString());

        Proof initProof = new Proof();
        initProof.hypotheses = hypotheses;
        initProof.statement = statement;

        for (line = in.nextLine(); line != null; line = in.nextLine()) {
            Expression expr = parseExpression(line);
            initProof.chain.add(expr);
        }

        Minimizer minimizer = new Minimizer(initProof);

        //print if incorrect proof:

        if (!minimizer.minimize()) {
            out.write(INCORRECT_PROOF);
            return;
        }

        //print first line:

        out.write(firstLine.toString());
        out.write(EOLN);

        //print other lines:

        for (int lineNum = 0; lineNum < minimizer.getProof().chain.size(); lineNum++) {
            //print open parenthesis and line number:
            out.write(OPEN_B);
            out.write(Integer.toString(lineNum + 1));
            out.write(POINT);

            //print if the line is a Hypothesis:
            if (minimizer.getProof().lineIsHypothesis(lineNum)) {
                out.write(HYPOTHESIS);
                out.write(minimizer.getProof().getHypothesisNumber(lineNum).toString());
            }

            //print if the line is a Modus Ponens:
            if (minimizer.getProof().lineIsModusPonens(lineNum)) {
                out.write(MP);
                out.write(minimizer.getProof().getModusPonensFrom(lineNum).toString());
                out.write(COMMA);
                out.write(minimizer.getProof().getModusPonensTo(lineNum).toString());
            }

            //print if the line is a Axiom Scheme:
            if (minimizer.getProof().lineIsAxiomScheme(lineNum)) {
                out.write(AX_SCH);
                out.write(minimizer.getProof().getAxiomSchemeNumber(lineNum).toString());
            }

            //print the closing parenthesis:
            out.write(CLOSE_B);
            out.write(SPACE);

            //print expression:
            out.write(minimizer.getProof().chain.get(lineNum).convertToString());
            out.write(EOLN);

        }

    }

}