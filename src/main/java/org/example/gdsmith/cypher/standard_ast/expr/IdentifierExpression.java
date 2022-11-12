package org.example.gdsmith.cypher.standard_ast.expr;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.IIdentifier;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.CypherTypeDescriptor;
import org.example.gdsmith.cypher.standard_ast.ListDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.*;

import java.util.List;
import java.util.Map;

public class IdentifierExpression extends CypherExpression {

    private final IIdentifier identifier;

    public IdentifierExpression(IIdentifier identifier){
        this.identifier = identifier;
    }

    public IIdentifier getIdentifier(){
        return identifier;
    }


    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append(identifier.getName());
    }

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        IIdentifierAnalyzer identifierAnalyzer = identifiers.stream().filter(i->i.getName().equals(identifier.getName())).findAny().orElse(null);
        if(identifierAnalyzer != null){
            if(identifierAnalyzer instanceof INodeAnalyzer){
                return new CypherTypeDescriptor((INodeAnalyzer) identifierAnalyzer);
            }
            if(identifierAnalyzer instanceof IRelationAnalyzer){
                if(!((IRelationAnalyzer) identifierAnalyzer).isSingleRelation()){ //非定长的，返回的是列表类型
                    ListDescriptor listDescriptor = new ListDescriptor(new CypherTypeDescriptor((IRelationAnalyzer) identifierAnalyzer));
                    return new CypherTypeDescriptor(listDescriptor);
                }
                return new CypherTypeDescriptor((IRelationAnalyzer) identifierAnalyzer);
            }
            if(identifierAnalyzer instanceof IAliasAnalyzer){
                return  ((IAliasAnalyzer) identifierAnalyzer).analyzeType(schema);
            }
        }
        return new CypherTypeDescriptor(CypherType.UNKNOWN);
    }

    @Override
    public IExpression getCopy() {
        IIdentifier identifier = null;
        if(this.identifier != null){
            identifier = this.identifier.getCopy();
        }
        return new IdentifierExpression(identifier);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        if(varToProperties.containsKey(identifier.getName())){
            return varToProperties.get(identifier.getName());
        }
        return ExprVal.UNKNOWN;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof IdentifierExpression)){
            return false;
        }
        return identifier.equals(((IdentifierExpression) o).identifier);
    }
}
