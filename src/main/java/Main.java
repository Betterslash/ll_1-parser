import model.Grammar;
import service.Parser;

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
        parser.getFirstMap().forEach((key, value) -> System.out.println(key + " -> " + value + System.lineSeparator()));
        parser.getFollowMap().forEach((key, value) -> System.out.println(key + " -> " + value + System.lineSeparator()));
    }
}
