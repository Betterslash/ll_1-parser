package service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.Grammar;
import model.HandsidesGrammarPair;
import model.Production;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class Parser {
    private final Grammar grammar;
    private final Map<String, Set<String>> firstMap;
    private final Map<String, Set<String>> followMap;
    private final Map<String, String> table = null;

    public Parser(){
        grammar = new Grammar();
        firstMap = generateFirstSet();
        followMap = generateFollowSet();
    }

    private Map<String, Set<String>> generateFollowSet() {
        var result = new HashMap<String, Set<String>>();
        grammar.getP()
                .forEach(e -> result.put(e.getLeftHandside(), Collections.emptySet()));
        result.put(grammar.getS(), Set.of("$"));
        grammar.getP()
                .forEach(e -> {
                    if(grammar.isInNonTerminals(e.getLeftHandside())){
                        e.getRightHandside().forEach(q -> {
                            if(q.getRepresentation().contains("ϵ")){
                                q.getRepresentation().forEach(z -> {
                                    if(grammar.isInNonTerminals(z)){
                                        var innerResult = new HashSet<String>();
                                        var current = result.get(e.getLeftHandside());
                                        current.addAll(first(getNonTerminalProductions(z), 0, innerResult));
                                        result.put(e.getLeftHandside(), current);
                                    }
                                });
                            }
                        });
                    }
                });
        return result;
    }

    private Map<String, Set<String>> generateFirstSet() {
        var result = new HashMap<String, Set<String>>();
        var index = 0;
        for (var e: grammar.getP()) {
            var innerResult = new HashSet<String>();
            first(e, index, innerResult);
            result.put(e.getLeftHandside(), innerResult);
        }
        return result;
    }

    private Set<String> first(HandsidesGrammarPair elem, int index, Set<String> result){
        var productions = elem.getRightHandside();
        productions.forEach(e -> {
            var firstElementOfProduction = e.getElementOfProduction(index);
            if(grammar.isInTerminals(firstElementOfProduction)){
                result.add(firstElementOfProduction);
            } else{
                var nonTerminalProduction = getNonTerminalProductions(firstElementOfProduction);
                result.addAll(first(nonTerminalProduction, index, result));
            }
        });
        return result;
    }

    private HandsidesGrammarPair getNonTerminalProductions(String firstElementOfProduction) {
        return grammar.getP()
                .stream()
                .filter(q -> Objects.equals(q.getLeftHandside(), firstElementOfProduction))
                .findFirst()
                .get();
    }
}
