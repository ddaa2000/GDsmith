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

public class SingleLogicalExpression extends CypherExpression {

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        return new CypherTypeDescriptor(CypherType.BOOLEAN);
    }

    public static SingleLogicalOperation randomOp(){
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 90);
        if(operationNum < 30){
            return SingleLogicalOperation.NOT;
        }
        if(operationNum < 60){
            return SingleLogicalOperation.IS_NULL;
        }
        return SingleLogicalOperation.IS_NOT_NULL;
    }

    public static SingleLogicalExpression randomLogical(IExpression expr){
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 90);
        //int operationNum = randomly.getInteger(0, 59); //todo
        if(operationNum < 30){
            return new SingleLogicalExpression(expr, SingleLogicalOperation.NOT);
        }
        if(operationNum < 60){
            return new SingleLogicalExpression(expr, SingleLogicalOperation.IS_NULL);
        }
        return new SingleLogicalExpression(expr, SingleLogicalOperation.IS_NOT_NULL);
    }

    @Override
    public IExpression getCopy() {
        IExpression child = null;
        if(this.child != null){
            child = this.child.getCopy();
        }
        return new SingleLogicalExpression(child, this.op);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if(originalExpression == child){
            this.child = newExpression;
            newExpression.setParentExpression(this);
            return;
        }

        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        Object childObject= child.getValue(varToProperties);
        if(childObject == ExprVal.UNKNOWN){
            return ExprVal.UNKNOWN;
        }

        switch (op){
            case NOT:
                return !(boolean) childObject;
            case IS_NULL:
                return childObject == null;
            case IS_NOT_NULL:
                return childObject != null;
            default:
                throw new RuntimeException();
        }
    }

    public enum SingleLogicalOperation{
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL"),
        NOT("NOT");

        SingleLogicalOperation(String textRepresentation){
            this.TextRepresentation = textRepresentation;
        }

        private final String TextRepresentation;

        public String getTextRepresentation(){
            return this.TextRepresentation;
        }
    }

    private IExpression child;
    private final SingleLogicalOperation op;

    public SingleLogicalExpression(IExpression child, SingleLogicalOperation op){
        this.child = child;
        this.op = op;
        child.setParentExpression(this);
    }

    public IExpression getChildExpression(){
        return child;
    }

    public SingleLogicalOperation getOperation(){
        return op;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("(");
        if(op == SingleLogicalOperation.NOT){
            sb.append(op.getTextRepresentation()).append(" ");
        }
        child.toTextRepresentation(sb);
        if(op != SingleLogicalOperation.NOT){
            sb.append(" ").append(op.getTextRepresentation());
        }
        sb.append(")");
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof SingleLogicalExpression)){
            return false;
        }
        if(child.equals(((SingleLogicalExpression) o).child)){
            return op == ((SingleLogicalExpression) o).op;
        }
        return false;
    }

}
