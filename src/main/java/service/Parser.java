package service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.Grammar;
import model.HandsidesGrammarPair;
import util.ProgramInitializer;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
        followMap = generateFollow();
    }

    private Map<String, Set<String>> generateFollow(){
        var result = new HashMap<String, Set<String>>();
        grammar.getN()
                .forEach(e -> result.put(e, new HashSet<>()));
        result.get(grammar.getS()).add("$");
        var nonTerms = grammar.getN();
        var changed = new AtomicBoolean(true);
        while (changed.get()){
            changed.set(false);
            for (var nT: nonTerms) {
                var rst = getFollow(nT, result);
                var previous = new HashMap<>(result);
                result.get(nT).addAll(rst);
                previous.forEach((key, value) -> {
                    if (result.get(key).size() != previous.get(key).size()) {
                        changed.set(true);
                    }
                });
            }
        }
        return result;
    }

    private Set<String> getFollow(String key, Map<String, Set<String>> follow) {
        var result = new HashSet<String>();
        grammar.getP()
                .forEach(e -> {
                    var filteredProductions = e.getRightHandside()
                            .stream()
                            .filter(n -> n.getRepresentation().contains(key))
                            .map(l -> {
                                var index = l.getRepresentation().indexOf(key);
                                return l.getRepresentation().subList(index + 1, l.getRepresentation().size());})
                            .collect(Collectors.toSet());
                    var res = filteredProductions.stream()
                            .map(s -> {
                                if(s.size() > 0){
                                    var first = applyFirst(s.get(0));
                                    if(first.contains(ProgramInitializer.EPSILON)){
                                        var r1 = new HashSet<>(follow.get(e.getLeftHandside()));
                                        r1.addAll(first.stream()
                                                .filter(j -> !Objects.equals(j, ProgramInitializer.EPSILON))
                                                .collect(Collectors.toSet()));
                                        return r1;
                                    }else{
                                        return first;
                                    }
                                }else{
                                    return follow.get(e.getLeftHandside());
                                }
                            })
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet());
                    result.addAll(res);
                });
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
