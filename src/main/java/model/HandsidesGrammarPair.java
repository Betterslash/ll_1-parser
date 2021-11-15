package model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
public class HandsidesGrammarPair {
    private final String leftHandside;
    private final List<String> rightHandside;
}
