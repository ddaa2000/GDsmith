package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.IType;

public class RelationType implements IType {

    private String name;

    public RelationType(String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof RelationType)){
            return false;
        }
        return ((RelationType) o).name.equals(name);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }
}
