package TaskB.utilities;

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

    private final String text;

    Axioms(String text) {
        this.text = text;
    }

    public Expression getPattern() {
        return Parser.parseExpression(text.replaceAll("\\s", ""));
    }
}