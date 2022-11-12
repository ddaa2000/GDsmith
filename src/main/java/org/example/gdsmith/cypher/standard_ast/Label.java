package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.ILabel;

public class Label implements ILabel {
    private String name;

    public Label(String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Label)){
            return false;
        }
        return ((Label) o).name.equals(name);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

}
