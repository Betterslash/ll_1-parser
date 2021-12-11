import model.tree.ParserTree;
import service.Parser;

import java.util.List;

/**
 * Main Class for running the program
 */
public class Main {
    /**
     * Main method for running the program
     * @param args args for determining specific behaviour of the program
     */
    public static void main(String[] args) {
        var parser = new Parser();
        var testSequence= List.of("a","+", "(", "a", "+", "a", ")", "*", "a"/*"var","id", ":", "int", ";", "id", ":", "bool", ";","id", ":", "char", ";"*/);

        parser.displayFirst();
        parser.displayFollow();
        System.out.println(parser.getParserTable().toTableInfo());
        parser.displayDerivationsForSequence(testSequence);
        var ws = parser.getDerivationsForSequence(testSequence).stream().map(Object::toString).reduce((a, b) -> a + b).orElseThrow();
        var pareserTree = new ParserTree(parser.getGrammar(), ws);

        pareserTree.prettyPrint();
    }
}
