package model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class HandsidesGrammarPair {

    private final String leftHandside;
    private final List<Production> rightHandside;

    @Override
    public String toString() {
        var state = rightHandside.stream()
                .map(Production::toString)
                .reduce((a, b) -> a + " | " + b)
                .orElseThrow(RuntimeException::new);
        return String.format(System.lineSeparator() + "%s -> %s", leftHandside, state);
    }
}
