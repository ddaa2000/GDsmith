package org.example.gdsmith.cypher.standard_ast.expr;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;
import org.example.gdsmith.cypher.schema.IFunctionInfo;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.CypherTypeDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CallExpression extends CypherExpression {
    private String functionName;
    private String functionSignature;
    private List<IExpression> params;

    public CallExpression(IFunctionInfo functionInfo, List<IExpression> params){
        this.functionName = functionInfo.getName();
        this.functionSignature = functionInfo.getSignature();
        this.params = params;
        params.forEach(e->e.setParentExpression(this));
    }

    public CallExpression(String functionName, String functionSignature, List<IExpression> params){
        this.functionName = functionName;
        this.functionSignature = functionSignature;
        this.params = params;
        params.forEach(e->e.setParentExpression(this));
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append(functionName).append("(");
        params.forEach(e->{e.toTextRepresentation(sb); sb.append(", ");});
        sb.delete(sb.length()-2, sb.length()); //多余的", "
        sb.append(")");
    }

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        IFunctionInfo functionInfo = schema.getFunctions().stream().filter(f->f.getSignature().equals(functionSignature)).findAny().orElse(null);
        if(functionInfo!=null){
            return functionInfo.calculateReturnType(params);
        }
        return new CypherTypeDescriptor(CypherType.UNKNOWN);
    }

    @Override
    public IExpression getCopy() {
        if(params == null){
            return new CallExpression(this.functionName, this.functionSignature, new ArrayList<>());
        }
        return new CallExpression(this.functionName, this.functionSignature,
                this.params.stream().map(p->p.getCopy()).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof CallExpression)){
            return false;
        }
        if(((CallExpression) o).params == null){
            ((CallExpression) o).params = new ArrayList<>();
        }
        if(params == null){
            params = new ArrayList<>();
        }
        if(params.size() != ((CallExpression) o).params.size()){
            return false;
        }
        return ((CallExpression) o).params.containsAll(params);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        for(int i = 0; i < params.size(); i++){
            if(originalExpression == params.get(i)){
                params.set(i, newExpression);
                newExpression.setParentExpression(this);
                return;
            }
        }

        throw new RuntimeException();
    }

    @Override
    public Object getValue(Map<String, Object> varToProperties) {
        return ExprVal.UNKNOWN;
    }
}
