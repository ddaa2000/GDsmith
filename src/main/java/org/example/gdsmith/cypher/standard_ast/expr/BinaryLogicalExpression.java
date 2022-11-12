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

public class BinaryLogicalExpression extends CypherExpression {

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
        return new BinaryLogicalExpression(left, right, this.op);
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
        switch (op){
            case AND:
                return (boolean) left.getValue(varToProperties) && (boolean) right.getValue(varToProperties);
            case OR:
                return (boolean) left.getValue(varToProperties) || (boolean) right.getValue(varToProperties);
            case XOR:
                return (boolean) left.getValue(varToProperties) ^ (boolean) right.getValue(varToProperties);
            default:
                throw new RuntimeException();
        }
    }

    public enum BinaryLogicalOperation{
        OR("OR"),
        AND("AND"),
        XOR("XOR");

        BinaryLogicalOperation(String textRepresentation){
            this.TextRepresentation = textRepresentation;
        }

        private final String TextRepresentation;

        public String getTextRepresentation(){
            return this.TextRepresentation;
        }
    }

    public static BinaryLogicalOperation randomOp(){
        Randomly randomly = new Randomly();
        //int operationNum = randomly.getInteger(0, 90);
        int operationNum = randomly.getInteger(0, 60); //todo
        if(operationNum < 30){
            return BinaryLogicalOperation.AND;
        }
        if(operationNum < 60){
            return BinaryLogicalOperation.OR;
        }
        return BinaryLogicalOperation.XOR;
    }

    public static BinaryLogicalExpression randomLogical(IExpression left, IExpression right){
        Randomly randomly = new Randomly();
        //int operationNum = randomly.getInteger(0, 90);
        int operationNum = randomly.getInteger(0, 60); //todo
        if(operationNum < 30){
            return new BinaryLogicalExpression(left, right, BinaryLogicalOperation.AND);
        }
        if(operationNum < 60){
            return new BinaryLogicalExpression(left, right, BinaryLogicalOperation.OR);
        }
        return new BinaryLogicalExpression(left, right, BinaryLogicalOperation.XOR);
    }

    private IExpression left;
    private IExpression right;
    private final BinaryLogicalOperation op;

    public BinaryLogicalExpression(IExpression left, IExpression right, BinaryLogicalOperation op){
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

    public BinaryLogicalOperation getOperation(){
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
        if(!(o instanceof BinaryLogicalExpression)){
            return false;
        }
        return left.equals(((BinaryLogicalExpression)o).left) && right.equals(((BinaryLogicalExpression)o).right)
                && op == ((BinaryLogicalExpression)o).op;
    }
}
