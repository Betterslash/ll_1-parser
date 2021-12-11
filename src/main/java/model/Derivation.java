package model;

import lombok.Data;

import java.util.List;

@Data
public class Derivation {
    private final List<String> currentState;
    private final Integer appliedDerivationNumber;

    public String toDerivationInfo(){
        return String.format("%s after applying (%s) ",currentState, appliedDerivationNumber);
    }
}
