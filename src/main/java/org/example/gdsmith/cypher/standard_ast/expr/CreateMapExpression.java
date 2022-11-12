package org.example.gdsmith.cypher.standard_ast.expr;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IMapDescriptor;
import org.example.gdsmith.cypher.standard_ast.CypherTypeDescriptor;
import org.example.gdsmith.cypher.standard_ast.MapDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateMapExpression extends CypherExpression{
    private Map<String, IExpression> propertyExpressions;

    public CreateMapExpression(Map<String, IExpression> propertyExpressions){
        this.propertyExpressions = propertyExpressions;
    }

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        Map<String, ICypherTypeDescriptor> mapMemberInfos = new HashMap<>();
        propertyExpressions.forEach((k,v)->{
            mapMemberInfos.put(k, v.analyzeType(schema, identifiers));
        });
        IMapDescriptor mapDescriptor = new MapDescriptor(mapMemberInfos);
        return new CypherTypeDescriptor(mapDescriptor);
    }

    @Override
    public IExpression getCopy() {
        Map<String, IExpression> newPropertyExpression = new HashMap<>();
        propertyExpressions.forEach((k, v)-> newPropertyExpression.put(k, v.getCopy()));
        return new CreateMapExpression(newPropertyExpression);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        String key = null;
        for(Map.Entry<String, IExpression> entry : propertyExpressions.entrySet()){
            if(entry.getValue() == originalExpression){
                key = entry.getKey();
            }
        }
        if(key != null){
            propertyExpressions.put(key, newExpression);
            return;
        }

        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        Map<String, Object> result = new HashMap<>();
        propertyExpressions.forEach((k,v)->{
            result.put(k, v.getValue(varToProperties));
        });
        return result;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("{");
        boolean first = true;
        for(Map.Entry<String, IExpression> entry: propertyExpressions.entrySet()){
            if(!first){
                sb.append(", ");
            }
            first = false;
            sb.append(entry.getKey());
            sb.append(": ");
            entry.getValue().toTextRepresentation(sb);
        }
        sb.append("}");
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof CreateMapExpression)){
            return false;
        }
        return propertyExpressions.equals(((CreateMapExpression) o).propertyExpressions);
    }
}
