package org.example.gdsmith.cypher.standard_ast.expr;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IListDescriptor;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.CypherTypeDescriptor;

import java.util.List;
import java.util.Map;

public class GetListElementExpression extends CypherExpression{
    private IExpression listExpression, indexExpression;

    public GetListElementExpression(IExpression listExpression, IExpression indexExpression){
        this.listExpression  = listExpression;
        this.indexExpression = indexExpression;
    }
    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        ICypherTypeDescriptor typeDescriptor = listExpression.analyzeType(schema, identifiers);
        if(typeDescriptor.isList()){
            IListDescriptor listDescriptor = typeDescriptor.getListDescriptor();
            if(listDescriptor.isListLengthUnknown()){
                if(listDescriptor.isMembersWithSameType()){
                    return listDescriptor.getSameMemberType();
                }
                else{
                    return new CypherTypeDescriptor(CypherType.UNKNOWN);
                }
            }
            else{
                //如果是定长的，需要知道究竟是取的哪个元素
                //todo 给Expression加算值的接口
                return new CypherTypeDescriptor(CypherType.UNKNOWN);
            }
        }
        return new CypherTypeDescriptor(CypherType.UNKNOWN);
    }

    @Override
    public IExpression getCopy() {
        return new GetListElementExpression(listExpression.getCopy(), indexExpression.getCopy());
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if(originalExpression == listExpression){
            this.listExpression = newExpression;
            newExpression.setParentExpression(this);
            return;
        }
        if(originalExpression == indexExpression){
            this.indexExpression = newExpression;
            newExpression.setParentExpression(this);
            return;
        }

        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        Object listObject = listExpression.getValue(varToProperties);
        Object indexObject = indexExpression.getValue(varToProperties);
        if(listObject == ExprVal.UNKNOWN || indexObject == ExprVal.UNKNOWN){
            return ExprVal.UNKNOWN;
        }
        List list = (List)listObject;
        int index = (int)indexObject;
        return list.get(index);
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        listExpression.toTextRepresentation(sb);
        sb.append("[");
        indexExpression.toTextRepresentation(sb);
        sb.append("]");
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof GetListElementExpression)){
            return false;
        }
        return listExpression.equals(((GetListElementExpression) o).listExpression) &&
                indexExpression.equals(((GetListElementExpression) o).indexExpression);
    }
}
