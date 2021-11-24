package service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import model.ParserTableKey;
import model.Production;
import util.ProgramInitializer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Builder
@AllArgsConstructor
public class ParserTable {
    private final Map<ParserTableKey, ParserTableValue> representation;
    private Parser parser;

    public ParserTable(Parser parser){
        this.parser = parser;
        this.representation = initializeRepresentation();
    }

    private Map<ParserTableKey, ParserTableValue> initializeRepresentation() {
        var grammar = parser.getGrammar();
        var result = new HashMap<ParserTableKey, ParserTableValue>();
        var keys = new ArrayList<>(grammar.getN());
        keys.addAll(new ArrayList<>(grammar.getE()));
        grammar.getE().forEach(e -> keys.forEach(q -> result.put(ParserTableKey.builder()
                        .column(e)
                        .row(q)
                        .build(), ParserTableValue.builder()
                        .build())));
        var index = new AtomicInteger();
        grammar.getP()
                .forEach(e -> e.getRightHandside().forEach(q -> {
                    if(q.getRepresentation().size() > 0 && !Objects.equals(q.getRepresentation().get(0), ProgramInitializer.EPSILON)){
                        var ks = computeKey(e.getLeftHandside(), q);
                        if(ks != null){
                            ks.forEach(o -> result.forEach((k, v) -> {
                                if(Objects.equals(k.getColumn(), o.getColumn()) && Objects.equals(k.getRow(), o.getRow())){
                                    result.put(k, ParserTableValue.builder().production(q).productionKey(index.get() + 1).build());
                                }
                            }));
                        }
                    }else{
                        var ks = parser.getFollowFromMap(e.getLeftHandside());
                        ks.forEach(o -> result.forEach((k, v) -> {
                            if(Objects.equals(k.getRow(), e.getLeftHandside()) && Objects.equals(k.getColumn(), o)){
                                result.put(k, ParserTableValue.builder()
                                        .production(Production.builder().representation(List.of(ProgramInitializer.EPSILON)).build())
                                        .productionKey(index.get() + 1).build());
                            }
                        }));
                    }
                    index.addAndGet(1);
                }));
        result.forEach((key, value) -> {
            if(Objects.equals(key.getColumn(), key.getRow())){
                result.put(key, ParserTableValue
                        .builder()
                        .production(Production.builder()
                                .representation(List.of("pop"))
                                .build())
                        .productionKey(-1)
                        .build());
            }
        });
        result.put(ParserTableKey.builder()
                .column("$")
                .row("$")
                .build(), ParserTableValue.builder()
                .production(Production.builder()
                        .representation(List.of("acc"))
                        .build())
                .productionKey(-1)
                .build());
        return result;
    }

    private List<ParserTableKey> computeKey(String key, Production production) {
        if(production.getRepresentation() != null && !Objects.equals(production.getRepresentation().get(0), ProgramInitializer.EPSILON)){
            var intermediateValue = production.getRepresentation()
                    .stream()
                    .map(parser::getFristFromMap)
                    .toList();
            return intermediateValue.stream()
                    .reduce(this::applyAddition)
                    .stream().flatMap(Set::stream)
                    .map(e -> ParserTableKey.builder().row(key).column(e).build())
                    .toList();
        }
        return null;
    }

    private Set<String> applyAddition(Set<String> a, Set<String> b) {
        if(a == null && b == null){
            return null;
        }
        else if(b == null){
            return a;
        }else if(a == null){
            return b;
        }
        else {
            var result = new HashSet<String>();
            for (var elem: a) {
                b.forEach(q -> {
                    if(Objects.equals(q, ProgramInitializer.EPSILON)){
                        result.add(String.valueOf(elem.charAt(0)));
                    }
                    if(Objects.equals(elem, ProgramInitializer.EPSILON)){
                        result.add(String.valueOf(q.charAt(0)));
                    }
                    result.add(String.valueOf((elem + q).charAt(0)));
                });
            }
            return result;
        }
    }

    public ParserTableValue getValue(String row, String column){
        var possibleResult = this.representation.entrySet()
                .stream()
                .filter(e -> Objects.equals(e.getKey().getColumn(), column) && Objects.equals(e.getKey().getRow(), row))
                .toList();
        if(possibleResult.size() > 0){
            return possibleResult.get(0).getValue();
        }else{
            throw new RuntimeException("Sequence is not accepted !!");
        }
    }

    public String toTableInfo(){
        var stringBuilder = new StringBuilder();
        representation.forEach((key, vlaue) -> stringBuilder.append(key).append(" -> ").append(vlaue));
        return stringBuilder.toString();
    }
}
