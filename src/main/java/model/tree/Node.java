package model.tree;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Node {
    private String value;
    private Node sibling;
    private Node child;
}
