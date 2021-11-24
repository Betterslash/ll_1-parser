package model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class ParserTableKey {
    private final String row;
    private final String column;

    @Override
    public String toString() {
        return "Row = " + row + " Column = " + column;
    }
}
