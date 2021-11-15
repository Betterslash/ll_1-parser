package model;

import lombok.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@ToString(exclude = {"reader"})
@AllArgsConstructor
public class Grammar {

    private List<String> N;
    private List<String> E;
    private String S;
    private List<HandsidesGrammarPair> P;
    private BufferedReader reader;


    /**
     * Custom constructor that reads a file with a path given in the configurations file and mapps the input as an Grammar object
     *
     * @throws RuntimeException if the initialization fails or if the file is not found at the location given from the configurations file
     */
    public Grammar() {
            try {
                this.reader = new BufferedReader(new FileReader("src/main/resources/regular_grammar_1.txt"));
                this.initializeFromFile();
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Couldn't initialize the grammar !!");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
    }

    /**
     * @return line parsed as a set of Terminals or non-Terminals for the Grammar object field N or E
     * @throws RuntimeException if the lines couldn't be parssed correctly
     */
    private List<String> readLineAsList() {
        try {
            var item = Arrays.stream(reader.readLine()
                            .strip()
                            .split("=")).toList()
                    .stream().map(String::strip)
                    .collect(Collectors.toList()).get(1);
            return Arrays.stream(item.substring(2, item.length() - 2)
                            .split(", "))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Something went wrong during reading!!");
        }
    }

    /**
     * Gets the rules from the last file lines asn parses them into the Grammar object
     *
     * @return Rules parsed as a list of HandsidePairs
     * @throws RuntimeException if the rules couldn't be mapped to the Grammar object field P
     */
    private List<HandsidesGrammarPair> parseRules() {
        try {
            var reducedLastLines = reader.lines().reduce((a, b) -> a + b)
                    .orElseThrow()
                    .split("=")[1];
            var expressions = reducedLastLines
                    .substring(3, reducedLastLines.length() - 1)
                    .replaceAll(",", "")
                    .split("\t");
            return Arrays.stream(expressions)
                    .toList()
                    .stream()
                    .map(e -> {
                        var leftHandside = e.split(" -> ")[0];
                        var rightHandside = e.split(" -> ")[1]
                                .replaceAll(" ", "")
                                .split("\\|");
                        return HandsidesGrammarPair
                                .builder()
                                .leftHandside(leftHandside)
                                .rightHandside(List.of(rightHandside))
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * initializes and parses a file given as input path from the configuration file as a grammar object
     *
     * @throws RuntimeException if the parsing is not succesfull
     */
    private void initializeFromFile() throws IOException {
        try {
            this.N = readLineAsList();
            this.E = readLineAsList();
            this.S = Arrays.stream(reader.readLine().strip().split(" = ")).toList().get(1);
            this.P = parseRules();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't parse the file correctly!!");
        }
        finally {
            reader.close();
        }
    }

    /**
     * @return true -> if the parsed grammar is a regular one
     *         false -> otherwise
     */
    public boolean isRegularGrammar(){
        for (var e : this.P) {
            var inRhs = new ArrayList<Character>();
            var excludeFromRhs = new ArrayList<Character>();
            for (var rhsValue : e.getRightHandside()) {
                var hasTerminal = false;
                var hasNonTerminal = false;
                if (rhsValue.length() > 2) {
                    return false;
                }
                for (var item : rhsValue.toCharArray()){
                    if(isInNonTerminals(item)){
                        inRhs.add(item);
                        hasNonTerminal = true;
                    }
                    else if(isInTerminals(item)){
                        // check if a non-terminal was't the last symbol
                        if(hasNonTerminal){
                            return false;
                        }
                        hasTerminal = true;
                    }
                    if(item == 'E'){
                        excludeFromRhs.add(item);
                    }
                }
                //check in case it has only non-terminal symbols
                if(hasNonTerminal && !hasTerminal){
                    return false;
                }
            }
            // check in cas E(Epsilon) was used asa a symbol
            for (var excludeFromRh : excludeFromRhs) {
                if (inRhs.contains(excludeFromRh)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param currentSymbol -> symbol to be checked if it is a terminal one
     * @return true -> if the symbol is in terminals list of the Grammar object (E field)
     * false -> otherwise
     */
    public boolean isInTerminals(char currentSymbol) {
        return E.contains(String.valueOf(currentSymbol));
    }

    /**
     * @param currentSymbol -> symbol to be checked if it is a non-terminal
     * @return true -> if the symbol is in non-terminals list of the Grammar object (N field)
     * false -> otherwise
     */
    public boolean isInNonTerminals(char currentSymbol) {
        return N.contains(String.valueOf(currentSymbol));
    }

}
