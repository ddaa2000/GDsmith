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

public class GetListSliceExpression extends CypherExpression{
    private IExpression listExpression, leftBound, rightBound;

    public GetListSliceExpression(IExpression listExpression, IExpression leftBound, IExpression rightBound){
        this.listExpression = listExpression;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        ICypherTypeDescriptor typeDescriptor = listExpression.analyzeType(schema, identifiers);
        if(typeDescriptor.isList()){
            IListDescriptor listDescriptor = typeDescriptor.getListDescriptor();
            if(listDescriptor.isListLengthUnknown()){
                if(listDescriptor.isMembersWithSameType()){
                    return typeDescriptor;
                }
                else {
                    return new CypherTypeDescriptor(CypherType.UNKNOWN);
                }
            }
            else {
                //如果是定长的，需要知道究竟是取的哪些元素
                //todo 给Expression加算值的接口
                return new CypherTypeDescriptor(CypherType.UNKNOWN);
            }

        }
        return new CypherTypeDescriptor(CypherType.UNKNOWN);
    }

    @Override
    public IExpression getCopy() {
        return new GetListSliceExpression(listExpression.getCopy(), leftBound.getCopy(), rightBound.getCopy());
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        listExpression.toTextRepresentation(sb);
        sb.append("[");
        if(leftBound!=null){
            leftBound.toTextRepresentation(sb);
        }
        sb.append("...");
        if(rightBound!=null){
            rightBound.toTextRepresentation(sb);
        }
        sb.append("]");
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof GetListSliceExpression)){
            return false;
        }
        return listExpression.equals(((GetListSliceExpression) o).listExpression) &&
                leftBound.equals(((GetListSliceExpression) o).leftBound) &&
                rightBound.equals(((GetListSliceExpression) o).rightBound);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if(originalExpression == listExpression){
            this.listExpression = newExpression;
            newExpression.setParentExpression(this);
            return;
        }
        if(originalExpression == leftBound){
            this.leftBound = newExpression;
            newExpression.setParentExpression(this);
            return;
        }
        if(originalExpression == rightBound){
            this.rightBound = newExpression;
            newExpression.setParentExpression(this);
            return;
        }

        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        Object listObject = listExpression.getValue(varToProperties);
        Object leftBoundObject = leftBound.getValue(varToProperties);
        Object rightBoundObject = rightBound.getValue(varToProperties);
        if(listObject == ExprVal.UNKNOWN || leftBoundObject == ExprVal.UNKNOWN || rightBoundObject == ExprVal.UNKNOWN){
            return ExprVal.UNKNOWN;
        }
        List list = (List)listObject;
        int leftBound = (int)leftBoundObject;
        int rightBound = (int)rightBoundObject;
        return list.subList(leftBound, rightBound);
    }
}
