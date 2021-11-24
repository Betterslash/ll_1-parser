package model;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Production {
    private List<String> representation;

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
