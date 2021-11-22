package model.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.Grammar;
import model.HandsidesGrammarPair;

import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParserTree {
    private Grammar grammar;
    private Node root;
    private Integer crt = 1;
    private String ws = "";
    private Integer indexInTreeSequence = 1;


    private HandsidesGrammarPair getGrammarPair(String firstElementOfProduction) {
        return grammar.getP()
                .stream()
                .filter(q -> Objects.equals(q.getLeftHandside(), firstElementOfProduction))
                .findAny()
                .orElseThrow(() -> {
                    throw new RuntimeException(firstElementOfProduction);});
    }
}
