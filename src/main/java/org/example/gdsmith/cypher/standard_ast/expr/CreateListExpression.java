package org.example.gdsmith.cypher.standard_ast.expr;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IListDescriptor;
import org.example.gdsmith.cypher.standard_ast.CypherTypeDescriptor;
import org.example.gdsmith.cypher.standard_ast.ListDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateListExpression extends CypherExpression {
    private List<IExpression> listElements;

    public CreateListExpression(List<IExpression> listElements){
        this.listElements = listElements;
    }

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        IListDescriptor listDescriptor = new ListDescriptor(listElements.stream()
                .map(e->e.analyzeType(schema, identifiers)).collect(Collectors.toList()));
        return new CypherTypeDescriptor(listDescriptor);
    }

    @Override
    public IExpression getCopy() {
        List<IExpression> newListElements = new ArrayList<>();
        listElements.forEach(e-> newListElements.add(e.getCopy()));
        return new CreateListExpression(newListElements);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        for(int i = 0; i < listElements.size(); i++){
            if(originalExpression == listElements.get(i)){
                listElements.set(i, newExpression);
                newExpression.setParentExpression(this);
                return;
            }
        }

        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        return listElements.stream().map(e->e.getValue(varToProperties)).collect(Collectors.toList());
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("[");
        for(int i = 0; i < listElements.size(); i++){
            if(i!=0){
                sb.append(", ");
            }
            listElements.get(i).toTextRepresentation(sb);
        }
        sb.append("]");
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof CreateListExpression)){
            return false;
        }
        if(listElements.size() != ((CreateListExpression) o).listElements.size()){
            return false;
        }
        return listElements.containsAll(((CreateListExpression) o).listElements);
    }
}
