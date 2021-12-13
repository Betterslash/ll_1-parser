# ll_1-parser
link to github : https://github.com/Betterslash/ll_1-parser

Statement: Implement a parser algorithm

1. One of the following parsing methods will be chosen (assigned by teaching staff):
    1.b. ll(1)


2. The representation of the parsing tree (output) will be (decided by the team):
   2.b. derivations string (max grade = 9)


UML Diagram :
<img src="src/main/resources/img_1.png">

Implementation :
1. Grammar
      1. Class is parsed from file and mapped in the following structures :
      2. List<String> N;
      3. List<String> E;
      4. String S;
      5. List<HandsidesGrammarPair> P;
      6. * Grammar is also checked to be context free
      7. ** HnadsideGrammarPair class is represented as key value pair for string to a Production list 
      8. *** A Production is a wrapper class for a list of string values
      9. **** Sorted productions and key Sorted production are helper wrappers for enabling production numerotation for parser table as in seminar
    
2. Parser
      1.  Being a ll(1) parser it has 2 tables, one for follow and the second one for first
    represented as maps of string keys with set of string as values
      2.  After generating the first/follow tables, the parser is able to determine a sequence of derivations as numbers
    for a given sequence to be parsed
      3.  Also contains wrapper methods for toString making possible a smooth visualization of the parser process

3. Parser Table
   1. Class that has a parser as main initialization resource 
   2. Initializes with respect to its parser object
   3. The representation is Key-Value one with Keys of type ParserTableKey and Values of type ParserTableValue
   4. * ParserTableKey has a row and a colum string values
   5. ** Parser table value is represented by a production and its associated key
   6. This class has toString wrapper to be able to smoothly display its format

4. Parser tree
    Still in work at the presentation time

Test cases : 
#
    Grammar :
        N = { S, A, B, C, D }
        E = { +, *, (, ), a }
        S = S
        P = {
        S -> B A,
        A -> + B A | ϵ,
        B -> D C,
        C -> * D C | ϵ,
        D -> ( S ) | a
        }
    Sequence :
        a + ( a + a ) * ( a + a ) + a
    Results :
        First :
        A -> [ϵ, +]
        B -> [a, (]
        S -> [a, (]
        C -> [ϵ, *]
        D -> [a, (]
        
        Follow :
        A -> [$, )]
        B -> [$, ), +]
        S -> [$, )]
        C -> [$, ), +]
        D -> [$, ), *, +]
        
        Row = B Column = ( -> D C 4
        Row = A Column = + -> + B A 2
        Row = C Column = ) -> ϵ 6
        Row = + Column = + -> pop -1
        Row = A Column = ) -> ϵ 3
        Row = ) Column = ) -> pop -1
        Row = D Column = ( -> ( S ) 7
        Row = C Column = + -> ϵ 6
        Row = ( Column = ( -> pop -1
        Row = D Column = a -> a 8
        Row = a Column = a -> pop -1
        Row = C Column = * -> * D C 5
        Row = S Column = ( -> B A 1
        Row = S Column = a -> B A 1
        Row = $ Column = $ -> acc -1
        Row = B Column = a -> D C 4
        Row = * Column = * -> pop -1
        
        Derivations :
        [B, A] after applying (1)
        [D, C, A] after applying (4)
        [a, C, A] after applying (8)
        [a, A] after applying (6)
        [a, +, B, A] after applying (2)
        [a, +, D, C, A] after applying (4)
        [a, +, (, S, ), C, A] after applying (7)
        [a, +, (, B, A, ), C, A] after applying (1)
        [a, +, (, D, C, A, ), C, A] after applying (4)
        [a, +, (, a, C, A, ), C, A] after applying (8)
        [a, +, (, a, A, ), C, A] after applying (6)
        [a, +, (, a, +, B, A, ), C, A] after applying (2)
        [a, +, (, a, +, D, C, A, ), C, A] after applying (4)
        [a, +, (, a, +, a, C, A, ), C, A] after applying (8)
        [a, +, (, a, +, a, A, ), C, A] after applying (6)
        [a, +, (, a, +, a, ), C, A] after applying (3)
        [a, +, (, a, +, a, ), *, D, C, A] after applying (5)
        [a, +, (, a, +, a, ), *, (, S, ), C, A] after applying (7)
        [a, +, (, a, +, a, ), *, (, B, A, ), C, A] after applying (1)
        [a, +, (, a, +, a, ), *, (, D, C, A, ), C, A] after applying (4)
        [a, +, (, a, +, a, ), *, (, a, C, A, ), C, A] after applying (8)
        [a, +, (, a, +, a, ), *, (, a, A, ), C, A] after applying (6)
        [a, +, (, a, +, a, ), *, (, a, +, B, A, ), C, A] after applying (2)
        [a, +, (, a, +, a, ), *, (, a, +, D, C, A, ), C, A] after applying (4)
        [a, +, (, a, +, a, ), *, (, a, +, a, C, A, ), C, A] after applying (8)
        [a, +, (, a, +, a, ), *, (, a, +, a, A, ), C, A] after applying (6)
        [a, +, (, a, +, a, ), *, (, a, +, a, ), C, A] after applying (3)
        [a, +, (, a, +, a, ), *, (, a, +, a, ), A] after applying (6)
        [a, +, (, a, +, a, ), *, (, a, +, a, ), +, B, A] after applying (2)
        [a, +, (, a, +, a, ), *, (, a, +, a, ), +, D, C, A] after applying (4)
        [a, +, (, a, +, a, ), *, (, a, +, a, ), +, a, C, A] after applying (8)
        [a, +, (, a, +, a, ), *, (, a, +, a, ), +, a, A] after applying (6)
        [a, +, (, a, +, a, ), *, (, a, +, a, ), +, a] after applying (3) 
        