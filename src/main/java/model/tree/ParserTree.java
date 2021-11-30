package model.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import model.Grammar;
import util.ProgramInitializer;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class ParserTree {
    private Grammar grammar;
    private Node root;
    private Integer crt = 1;
    private String ws;
    private Integer indexInTreeSequence = 1;
    static final int COUNT = 2;
    public ParserTree(Grammar grammar, String sequence){
        this.grammar = grammar;
        this.ws = sequence;
        this.root = build();
    }

    private Node build(){
        var index = Integer.parseInt(String.valueOf(this.ws.charAt(0)));
        var seq = grammar.getSortedProductions().get(index);
        var grammarPair = this.grammar.getP()
                .stream()
                .filter(e -> e.getRightHandside().contains(seq))
                .findFirst()
                .orElseThrow(RuntimeException::new);
        this.root = Node.builder().value(grammarPair.getLeftHandside()).sibling(null).child(null).build();
        this.root.setChild(this.buildRecursive(seq.getRepresentation()));
        return root;
    }

    private Node buildRecursive(List<String> currentTransition){
        if(currentTransition.size() == 0 || this.indexInTreeSequence >= ws.length()) {
            return null;
        }

        var currentSymbol = currentTransition.get(0);
        if(this.grammar.getE().contains(currentSymbol)){
            var node = Node
                    .builder()
                    .value(currentSymbol)
                    .child(null)
                    .sibling(null)
                    .build();
            node.setSibling(this.buildRecursive(currentTransition.subList(1, currentTransition.size())));
            return node;
        }else if(this.grammar.getN().contains(currentSymbol)){
            var transitionNumber = Integer.parseInt(String.valueOf(this.ws.charAt(this.indexInTreeSequence)));
            var transition = this.grammar.getSortedProductions().get(transitionNumber - 1);
            var node = Node.builder()
                    .value(currentSymbol)
                    .sibling(null)
                    .child(null)
                    .build();
            this.indexInTreeSequence += 1;
            node.setChild(this.buildRecursive(transition.getRepresentation()));
            node.setSibling(this.buildRecursive(currentTransition.subList(1, currentTransition.size())));
            return node;
        }else {
            return Node.builder()
                    .value(ProgramInitializer.EPSILON)
                    .child(null)
                    .sibling(null)
                    .build();
        }
    }

    private List<Node> getSibingsForNode(Node node, List<Node> result){
        if(node.getSibling() == null){
            return result;
        }else{
            result.add(node.getSibling());
            return getSibingsForNode(node.getSibling(), result);
        }
    }

    private void print2DUtil(Node root)
    {
        if(root != null){
            if(root.getSibling() != null){
                System.out.print(root.getValue() + " " + root.getSibling().getValue());
            }else{
                System.out.print(root.getValue());
            }
            print2DUtil(root.getSibling());
            System.out.println();
            print2DUtil(root.getChild());
        }
    }

    public void prettyPrint()
    {
        System.out.println("Tree : ");
        print2DUtil(this.root);
    }
}
