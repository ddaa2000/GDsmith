package org.example.gdsmith.cypher.standard_ast.expr;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.CypherTypeDescriptor;

import java.util.List;
import java.util.Map;

public class BinaryComparisonExpression extends CypherExpression {

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        return new CypherTypeDescriptor(CypherType.BOOLEAN);
    }

    @Override
    public IExpression getCopy() {
        IExpression left = null, right = null;
        if(this.left != null){
            left = this.left.getCopy();
        }
        if(this.right != null){
            right = this.right.getCopy();
        }
        return new BinaryComparisonExpression(left, right, this.op);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if(originalExpression == left){
            this.left = newExpression;
            newExpression.setParentExpression(this);
            return;
        }
        if(originalExpression == right){
            this.right = newExpression;
            newExpression.setParentExpression(this);
            return;
        }

        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        Object leftObject = left.getValue(varToProperties);
        Object rightObject = right.getValue(varToProperties);
        if(leftObject == ExprVal.UNKNOWN || rightObject == ExprVal.UNKNOWN){
            return ExprVal.UNKNOWN;
        }
        if(leftObject instanceof String){
            switch (op){
                case SMALLER:
                    return ((String) leftObject).compareTo((String) rightObject) < 0;
                case SMALLER_OR_EQUAL:
                    return ((String) leftObject).compareTo((String) rightObject) <= 0;
                case HIGHER:
                    return ((String) leftObject).compareTo((String) rightObject) > 0;
                case NOT_EQUAL:
                    return ((String) leftObject).compareTo((String) rightObject) != 0;
                case EQUAL:
                    return ((String) leftObject).compareTo((String) rightObject) == 0;
                case HIGHER_OR_EQUAL:
                    return ((String) leftObject).compareTo((String) rightObject) >= 0;
                default:
                    throw new RuntimeException();
            }
        }
        if(leftObject instanceof Number){
            switch (op){
                case SMALLER:
                    return ((int)leftObject) < ((int) rightObject);
                case SMALLER_OR_EQUAL:
                    return ((int)leftObject) <= ((int) rightObject);
                case HIGHER:
                    return ((int)leftObject) > ((int) rightObject);
                case NOT_EQUAL:
                    return ((int)leftObject) != ((int) rightObject);
                case EQUAL:
                    return ((int)leftObject) == ((int) rightObject);
                case HIGHER_OR_EQUAL:
                    return ((int)leftObject) >= ((int) rightObject);
                default:
                    throw new RuntimeException();
            }
        }
        throw new RuntimeException();
    }

    public static BinaryComparisonOperation randomOperation(){
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 100);
        if(operationNum < 5){
            return BinaryComparisonOperation.EQUAL;
        }
        if(operationNum < 20){
            return BinaryComparisonOperation.NOT_EQUAL;
        }
        if(operationNum < 40){
            return BinaryComparisonOperation.HIGHER;
        }
        if(operationNum < 60){
            return BinaryComparisonOperation.HIGHER_OR_EQUAL;
        }
        if(operationNum < 80){
            return BinaryComparisonOperation.SMALLER;
        }
        return BinaryComparisonOperation.SMALLER_OR_EQUAL;
    }

    public static BinaryComparisonExpression randomComparison(IExpression left, IExpression right){
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 100);
        if(operationNum < 5){
            return new BinaryComparisonExpression(left, right, BinaryComparisonOperation.EQUAL);
        }
        if(operationNum < 20){
            return new BinaryComparisonExpression(left, right, BinaryComparisonOperation.NOT_EQUAL);
        }
        if(operationNum < 40){
            return new BinaryComparisonExpression(left, right, BinaryComparisonOperation.HIGHER);
        }
        if(operationNum < 60){
            return new BinaryComparisonExpression(left, right, BinaryComparisonOperation.HIGHER_OR_EQUAL);
        }
        if(operationNum < 80){
            return new BinaryComparisonExpression(left, right, BinaryComparisonOperation.SMALLER);
        }
        return new BinaryComparisonExpression(left, right, BinaryComparisonOperation.SMALLER_OR_EQUAL);
    }

    public enum BinaryComparisonOperation{
        SMALLER("<"),
        EQUAL("="),
        SMALLER_OR_EQUAL("<="),
        HIGHER(">"),
        HIGHER_OR_EQUAL(">="),
        NOT_EQUAL("<>");

        BinaryComparisonOperation(String textRepresentation){
            this.TextRepresentation = textRepresentation;
        }

        private final String TextRepresentation;

        public String getTextRepresentation(){
            return this.TextRepresentation;
        }

        public BinaryComparisonOperation reverse(){
            switch (this){
                case EQUAL:
                    return NOT_EQUAL;
                case NOT_EQUAL:
                    return EQUAL;
                case HIGHER:
                    return SMALLER_OR_EQUAL;
                case HIGHER_OR_EQUAL:
                    return SMALLER;
                case SMALLER:
                    return HIGHER_OR_EQUAL;
                case SMALLER_OR_EQUAL:
                    return HIGHER;
                default:
                    throw new RuntimeException();
            }
        }
    }

    private IExpression left, right;
    private BinaryComparisonOperation op;

    public BinaryComparisonExpression(IExpression left, IExpression right, BinaryComparisonOperation op){
        left.setParentExpression(this);
        right.setParentExpression(this);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public IExpression getLeftExpression(){
        return left;
    }

    public IExpression getRightExpression(){
        return right;
    }

    public BinaryComparisonOperation getOperation(){
        return op;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("(");
        left.toTextRepresentation(sb);
        sb.append(" ").append(op.getTextRepresentation()).append(" ");
        right.toTextRepresentation(sb);
        sb.append(")");
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof BinaryComparisonExpression)){
            return false;
        }
        return left.equals(((BinaryComparisonExpression)o).left) && right.equals(((BinaryComparisonExpression)o).right)
                && op == ((BinaryComparisonExpression)o).op;
    }

    public void setOperation(BinaryComparisonOperation op){
        this.op = op;
    }
}
