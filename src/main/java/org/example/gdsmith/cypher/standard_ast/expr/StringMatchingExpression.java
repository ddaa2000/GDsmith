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

public class StringMatchingExpression extends CypherExpression {
    private IExpression source, pattern;
    private StringMatchingOperation op;

    public IExpression getSource() {
        return source;
    }

    public void setSource(IExpression source) {
        this.source = source;
    }

    public IExpression getPattern() {
        return pattern;
    }

    public void setPattern(IExpression pattern) {
        this.pattern = pattern;
    }

    public StringMatchingOperation getOp() {
        return op;
    }

    public void setOp(StringMatchingOperation op) {
        this.op = op;
    }

    public StringMatchingExpression(IExpression source, IExpression pattern, StringMatchingOperation op){
        this.source = source;
        this.pattern = pattern;
        this.op = op;
        source.setParentExpression(this);
        pattern.setParentExpression(this);
    }

    public enum StringMatchingOperation{
        CONTAINS("CONTAINS"),
        STARTS_WITH("STARTS WITH"),
        ENDS_WITH("ENDS WITH");

        StringMatchingOperation(String textRepresentation){
            this.TextRepresentation = textRepresentation;
        }

        private final String TextRepresentation;

        public String getTextRepresentation(){
            return this.TextRepresentation;
        }
    }

    public static StringMatchingOperation randomOperation(){
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 90);
        if(operationNum < 30){
            return StringMatchingOperation.CONTAINS;
        }
        if(operationNum < 60){
            return StringMatchingOperation.ENDS_WITH;
        }
        return StringMatchingOperation.STARTS_WITH;
    }

    public static StringMatchingExpression randomMatching(IExpression left, IExpression right){
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 90);
        if(operationNum < 30){
            return new  StringMatchingExpression(left, right, StringMatchingOperation.CONTAINS);
        }
        if(operationNum < 60){
            return new  StringMatchingExpression(left, right, StringMatchingOperation.ENDS_WITH);
        }
        return new  StringMatchingExpression(left, right, StringMatchingOperation.STARTS_WITH);
    }

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        return new CypherTypeDescriptor(CypherType.BOOLEAN);
    }

    @Override
    public IExpression getCopy() {
        return new StringMatchingExpression(source.getCopy(), pattern.getCopy(), op);
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("(");
        source.toTextRepresentation(sb);
        sb.append(" ").append(op.getTextRepresentation()).append(" ");
        pattern.toTextRepresentation(sb);
        sb.append(")");
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if(originalExpression == source){
            this.source = newExpression;
            newExpression.setParentExpression(this);
            return;
        }
        if(originalExpression == pattern){
            this.pattern = newExpression;
            newExpression.setParentExpression(this);
            return;
        }

        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        Object sourceObject = source.getValue(varToProperties);
        Object patternObject = pattern.getValue(varToProperties);
        if(sourceObject == ExprVal.UNKNOWN || patternObject == ExprVal.UNKNOWN){
            return ExprVal.UNKNOWN;
        }
        switch (op){
            case CONTAINS:
                return ((String)sourceObject).contains((String)patternObject);
            case STARTS_WITH:
                return ((String)sourceObject).startsWith((String) patternObject);
            case ENDS_WITH:
                return ((String)sourceObject).endsWith((String) patternObject);
            default:
                throw new RuntimeException();
        }
    }
}
