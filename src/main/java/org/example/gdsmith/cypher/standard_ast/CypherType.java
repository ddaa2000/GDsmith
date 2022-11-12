package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.ICypherType;

public enum CypherType implements ICypherType {
    NUMBER, BOOLEAN, STRING, NODE, RELATION, UNKNOWN, LIST, MAP, BASIC, ANY;

    public static CypherType getRandomBasicType(){
        Randomly randomly = new Randomly();
        int randomNum = randomly.getInteger(0, 100);
        if(randomNum < 40){
            return NUMBER;
        }
        if(randomNum < 80){
            return STRING;
        }
        return BOOLEAN;
    }
}
