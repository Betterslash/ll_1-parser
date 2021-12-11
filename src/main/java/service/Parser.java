package service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.Derivation;
import model.Grammar;
import model.HandsidesGrammarPair;
import model.Production;
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
    private final ParserTable parserTable;

    public Parser(){
        grammar = new Grammar();
        firstMap = generateFirstSet();
        followMap = generateFollow();
        parserTable = new ParserTable(this);
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

    public Set<String> getFristFromMap(String key){
        if(grammar.isInTerminals(key)){
            return Set.of(key);
        }
        return this.getFirstMap().get(key);
    }

    public Set<String> getFollowFromMap(String key){
        return this.getFollowMap().get(key);
    }

    public List<Integer> getDerivationsForSequence(List<String> production){
        var toBeChecked = new ArrayList<>(production);
        toBeChecked.add("$");
        var initialStep = new ArrayList<>(Arrays.asList(grammar.getS(), "$"));
        var derivations = new ArrayList<Integer>();
        while (!Objects.equals(toBeChecked.get(0), "$")){
            var e = toBeChecked.get(0);
                var result = parserTable.getValue(initialStep.get(0), e);
                if(Objects.equals(result.getProduction().getRepresentation().get(0), "pop")){
                    toBeChecked = new ArrayList<>(toBeChecked.subList(1, toBeChecked.size()));
                    initialStep = new ArrayList<>(initialStep.subList(1, initialStep.size()));
                }else if(Objects.equals(result.getProduction().getRepresentation().get(0), "acc")){
                    return derivations;
                }else if(!Objects.equals(result.getProduction().getRepresentation().get(0), ProgramInitializer.EPSILON)){
                    initialStep.remove(0);
                    initialStep.addAll(0, result.getProduction().getRepresentation());
                    derivations.add(result.getProductionKey());
                }else{
                    initialStep.remove(0);
                    derivations.add(result.getProductionKey());
                }
        }
        while (!Objects.equals(initialStep.get(0), "$")){
            var currentSymbol = initialStep.get(0);
            var index = grammar.getKeySortedProduction()
                    .entrySet().stream()
                    .filter(e -> Objects.equals(e.getValue().toString(), ProgramInitializer.EPSILON) && Objects.equals(e.getKey().getKey(), currentSymbol))
                            .findFirst()
                                    .orElseThrow()
                                            .getKey()
                    .getIndex();
            derivations.add(index + 1);
            initialStep.remove(0);
        }
        return  derivations;
    }

    public void displayDerivationsForSequence(List<String> productions){
        var derivations = getDerivationsForSequence(productions);
        var states = new ArrayList<Derivation>();
        System.out.println("Derivations : ");
        var currentDerivationNumber = derivations.get(0);
        var production = getProductionForIndex(currentDerivationNumber);
        derivations.remove(0);
        var result = new ArrayList<>(production.getRepresentation());
        states.add(new Derivation(new ArrayList<>(result), currentDerivationNumber));
        while (derivations.size() > 0){
            currentDerivationNumber = derivations.get(0);
            production = getProductionForIndex(currentDerivationNumber);
            derivations.remove(0);
            var leftHandise = getRightHandsideForProduction(production);
            var indexOfLeftHandsideInResult = result.indexOf(leftHandise);
            if(indexOfLeftHandsideInResult != -1){
                result.remove(indexOfLeftHandsideInResult);
                result.addAll(indexOfLeftHandsideInResult, production.getRepresentation().stream().filter(e -> !Objects.equals(e, ProgramInitializer.EPSILON)).collect(Collectors.toList()));
                states.add(new Derivation(new ArrayList<>(result), currentDerivationNumber));
            }
        }
        states.stream()
                .map(Derivation::toDerivationInfo)
                .toList()
                .forEach(System.out::println);
    }

    private Production getProductionForIndex(int index){
        return this.grammar.getSortedProductions().get(index - 1);
    }

    private String getRightHandsideForProduction(Production production){
        return this.grammar.getP()
                .stream()
                .filter(e -> e.getRightHandside().contains(production))
                .findAny()
                .orElseThrow()
                .getLeftHandside();
    }

    public int getFirstNonTerminalPosition(List<String> production){
        var nonTermial = production.stream().filter(grammar::isInNonTerminals).findFirst();
        return production.indexOf(nonTermial.orElse(""));
    }

    public void displayFollow(){
        System.out.println("Follow : ");
        this.followMap
                .forEach((key, value) -> System.out.println(key + " -> " + value));
        System.out.println();
    }

    public void displayFirst(){
        System.out.println("First : ");
        this.firstMap
                .forEach((key, value) -> System.out.println(key + " -> " + value));
        System.out.println();
    }
}
