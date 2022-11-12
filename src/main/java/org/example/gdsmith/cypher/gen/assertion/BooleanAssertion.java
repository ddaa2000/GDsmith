package org.example.gdsmith.cypher.gen.assertion;

public class BooleanAssertion implements ExpressionAssertion {
    private boolean value;

    public BooleanAssertion(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean check(Object value) {
        if(!(value instanceof Boolean)){
            return false;
        }
        return (Boolean)value == this.value;
    }
}
