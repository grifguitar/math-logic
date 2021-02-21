package TaskB.utilities;

import java.util.HashMap;

public interface Expression extends ExpressionOrOperation {
    String convertToString();

    boolean tryMatch(Expression expr);

    Pair<Boolean, HashMap<String, String>> check();
}