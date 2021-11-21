package model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class Production {
    private final List<String> representation;

    @Override
    public String toString() {
        return representation.stream()
                .reduce((a, b) -> a+ " " + b)
                .orElseThrow(RuntimeException::new);
    }

    public String getElementOfProduction(int pos){
        return this.representation.get(pos);
    }
}
