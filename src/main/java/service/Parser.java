package service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.Grammar;
import model.HandsidesGrammarPair;
import model.Production;
import util.ProgramInitializer;

import java.util.*;
import java.util.stream.Collectors;

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

    private Set<String> getFollows(Production production, String key){
        var productionRepresentation = production.getRepresentation();
        return new HashSet<>(productionRepresentation
                .subList(productionRepresentation.indexOf(key) + 1,
                        productionRepresentation.size()));
    }

    private Map<String, Set<String>> generateFollowSet() {
        var result = new HashMap<String, Set<String>>();
        grammar.getP()
                .forEach(e -> result.put(e.getLeftHandside(), new HashSet<>()));
        result.put(grammar.getS(), Set.of("$"));
        result.forEach((key, value) -> grammar.getP()
                .forEach(u -> u.getRightHandside()
                        .forEach(l -> {
                            if(l.getRepresentation().contains(key)){
                                var values = getFollows(l, key);
                                values.forEach(o -> {
                                    var curretnValue = result.get(key);
                                    if(grammar.isInTerminals(o)){
                                        curretnValue.add(o);
                                    }else{
                                        curretnValue.addAll(applyFirst(o)
                                                .stream()
                                                .filter(q -> !Objects.equals(q, ProgramInitializer.EPSILON)).collect(Collectors.toSet()));
                                    }
                                    result.put(key, curretnValue);
                                });
                            }
                })));
        return result;
    }

    private Map<String, Set<String>> generateFirstSet() {
        var result = new HashMap<String, Set<String>>();
        for (var e: grammar.getP()) {
            var innerResult = applyFirst(e.getLeftHandside());
            result.put(e.getLeftHandside(), innerResult);
        }
        return result;
    }

    private Set<String> applyFirst(String symbol){
        var result = new HashSet<String>();
        if(grammar.isInTerminals(symbol)){
            result.add(symbol);
        }else if(Objects.equals(symbol, ProgramInitializer.EPSILON)){
            result.add(symbol);
        }else{
            var handsidePair = getGrammarPair(symbol);
            handsidePair.getRightHandside()
                    .forEach(e -> {
                        var element = e.getElementOfProduction(0);
                        var first = applyFirst(element);
                        if(first.contains(ProgramInitializer.EPSILON)){
                            result.addAll(first.stream().filter(z -> !Objects.equals(z, ProgramInitializer.EPSILON)).collect(Collectors.toSet()));
                            if(e.getRepresentation().size() > 1){
                                var second = applyFirst(e.getElementOfProduction(1));
                                result.addAll(second);
                            }
                        }else {
                            result.addAll(first);
                        }
                        var needsEpsilon = e.getRepresentation()
                                .stream()
                                .allMatch(q -> applyFirst(q).contains(ProgramInitializer.EPSILON));
                        if(needsEpsilon){
                            result.add(ProgramInitializer.EPSILON);
                        }
                    });
        }
        return result;
    }


    private HandsidesGrammarPair getGrammarPair(String firstElementOfProduction) {
        return grammar.getP()
                .stream()
                .filter(q -> Objects.equals(q.getLeftHandside(), firstElementOfProduction))
                .findAny()
                .orElseThrow(() -> {
                    throw new RuntimeException(firstElementOfProduction);});
    }

}
