package org.example.gdsmith.cypher.gen.assertion;

import org.example.gdsmith.cypher.standard_ast.expr.BinaryComparisonExpression;
import org.example.gdsmith.cypher.standard_ast.expr.ExprVal;

public class ComparisonAssertion implements ExpressionAssertion {
    private BinaryComparisonExpression.BinaryComparisonOperation operation;
    private Object leftOp;
    private boolean target;

    public ComparisonAssertion(BinaryComparisonExpression.BinaryComparisonOperation operation, Object val, boolean target) {
        if(val == null){
            int z = 0;
        }
        this.leftOp = val;
        this.operation = operation;
        this.target = target;
    }

    public BinaryComparisonExpression.BinaryComparisonOperation getOperation() {
        return operation;
    }

    public void setOperation(BinaryComparisonExpression.BinaryComparisonOperation operation) {
        this.operation = operation;
    }

    public Object getLeftOp() {
        return leftOp;
    }

    public void setLeftOp(Object leftOp) {
        this.leftOp = leftOp;
    }

    public boolean trueTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    @Override
    public boolean check(Object value) {
        if(value == ExprVal.UNKNOWN || getLeftOp() == ExprVal.UNKNOWN){
            return true;
        }
        if(value instanceof Number){
            int leftOp = ((Number)getLeftOp()).intValue();
            int rightOp = ((Number) value).intValue();
            BinaryComparisonExpression.BinaryComparisonOperation operation = getOperation();
            if(!trueTarget()){
                operation = operation.reverse();
            }

            switch (operation) {
                case EQUAL:
                    return leftOp == rightOp;
                case HIGHER:
                    return leftOp > rightOp;
                case HIGHER_OR_EQUAL:
                    return leftOp >= rightOp;
                case SMALLER:
                    return leftOp < rightOp;
                case SMALLER_OR_EQUAL:
                    return leftOp <= rightOp;
                case NOT_EQUAL:
                    return leftOp != rightOp;
            }
        }

        if(value instanceof String){
            String leftOp = (String) getLeftOp();
            String rightOp = (String) value;
            BinaryComparisonExpression.BinaryComparisonOperation operation = getOperation();
            if(!trueTarget()){
                operation = operation.reverse();
            }

            switch (operation) {
                case EQUAL:
                    return leftOp.compareTo(rightOp) == 0;
                case HIGHER:
                    return leftOp.compareTo(rightOp) > 0;
                case HIGHER_OR_EQUAL:
                    return leftOp.compareTo(rightOp) >= 0;
                case SMALLER:
                    return leftOp.compareTo(rightOp) < 0;
                case SMALLER_OR_EQUAL:
                    return leftOp.compareTo(rightOp) <= 0;
                case NOT_EQUAL:
                    return leftOp.compareTo(rightOp) != 0;
            }
        }

        return false;
    }
}
