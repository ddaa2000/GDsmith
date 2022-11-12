package org.example.gdsmith.cypher.gen.assertion;

import org.example.gdsmith.cypher.standard_ast.expr.ExprVal;
import org.example.gdsmith.cypher.standard_ast.expr.StringMatchingExpression;

public class StringMatchingAssertion implements ExpressionAssertion {
    private StringMatchingExpression.StringMatchingOperation operation;
    private Object string;
    private boolean target;

    public StringMatchingAssertion(StringMatchingExpression.StringMatchingOperation operation, Object string, boolean target) {
        this.operation = operation;
        this.string = string;
        this.target = target;
    }

    public StringMatchingExpression.StringMatchingOperation getOperation() {
        return operation;
    }

    public void setOperation(StringMatchingExpression.StringMatchingOperation operation) {
        this.operation = operation;
    }

    public Object getString() {
        return string;
    }

    public void setString(Object string) {
        this.string = string;
    }

    public boolean isTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    @Override
    public boolean check(Object value) {
        if(value == ExprVal.UNKNOWN || this.string == ExprVal.UNKNOWN){
            return true;
        }
        if(!(value instanceof String) || !(this.string instanceof String)){
            return false;
        }
        boolean result = false;
        String string = (String) this.string;
        switch (operation){
            case CONTAINS:
                result =  string.contains((String)value);
                break;
            case STARTS_WITH:
                result =  string.startsWith((String)value);
                break;
            case ENDS_WITH:
                result =  string.endsWith((String)value);
                break;
        }
        return target ? result : !result;
    }
}
