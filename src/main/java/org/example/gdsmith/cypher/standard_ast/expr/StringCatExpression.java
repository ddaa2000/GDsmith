package org.example.gdsmith.cypher.standard_ast.expr;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.CypherTypeDescriptor;

import java.util.List;
import java.util.Map;

public class StringCatExpression extends CypherExpression{
    private IExpression left, right;

    public IExpression getLeft() {
        return left;
    }

    public void setLeft(IExpression left) {
        this.left = left;
    }

    public IExpression getRight() {
        return right;
    }

    public void setRight(IExpression right) {
        this.right = right;
    }

    public StringCatExpression(IExpression left, IExpression right){
        this.left = left;
        this.right = right;
    }

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        return new CypherTypeDescriptor(CypherType.STRING);
    }

    @Override
    public IExpression getCopy() {
        return new StringCatExpression(left, right);
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("(");
        left.toTextRepresentation(sb);
        sb.append("+");
        right.toTextRepresentation(sb);
        sb.append(")");
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof StringCatExpression)){
            return false;
        }
        return left.equals(((StringCatExpression) o).left) && right.equals(((StringCatExpression) o).right);
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
        if(left.getValue(varToProperties) == ExprVal.UNKNOWN || right.getValue(varToProperties) == ExprVal.UNKNOWN){
            return ExprVal.UNKNOWN;
        }
        String leftVal = (String) left.getValue(varToProperties);
        String rightVal = (String) right.getValue(varToProperties);
        return leftVal + rightVal;
    }
}
