package service;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.Production;

@Getter
@Builder
@RequiredArgsConstructor
public class ParserTableValue {
    private final Production production;
    private final Integer productionKey;

    @Override
    public String toString() {
        return production + " " +productionKey  + "\n";
    }
}
