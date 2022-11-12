package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.IReturnAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Return extends CypherClause implements IReturnAnalyzer {

    private IExpression skip = null, limit = null;
    private List<IExpression> orderBy = new ArrayList<>();
    boolean isOrderByDesc = false;
    private boolean distinct = false;

    public Return(){
        super(false);
    }


    @Override
    public List<IRet> getReturnList() {
        return symtab.getAliasDefinitions();
    }

    @Override
    public void setReturnList(List<IRet> returnList) {
        symtab.setAliasDefinition(returnList);
    }

    @Override
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean isDistinct() {
        return distinct;
    }


    @Override
    public List<IExpression> getOrderByExpressions() {
        return orderBy;
    }

    @Override
    public boolean isOrderByDesc() {
        return isOrderByDesc;
    }

    @Override
    public void setOrderBy(List<IExpression> expressions, boolean isDesc) {
        orderBy = expressions;
        isOrderByDesc = isDesc;
    }


    @Override
    public void setLimit(IExpression expression) {
        limit = expression;
    }

    @Override
    public IExpression getLimit() {
        return limit;
    }

    @Override
    public void setSkip(IExpression expression) {
        skip = expression;
    }

    @Override
    public IExpression getSkip() {
        return skip;
    }

    @Override
    public IReturnAnalyzer toAnalyzer() {
        return this;
    }

    @Override
    public ICypherClause getCopy() {
        Return returnClause = new Return();
        if(symtab != null){
            if(symtab != null){
                returnClause.symtab.setPatterns(symtab.getPatterns().stream().map(p->p.getCopy()).collect(Collectors.toList()));
                returnClause.symtab.setAliasDefinition(symtab.getAliasDefinitions().stream().map(a->a.getCopy()).collect(Collectors.toList()));
            }
        }
        if(skip != null){
            returnClause.skip = skip.getCopy();
        }
        if(limit != null){
            returnClause.limit = limit.getCopy();
        }
        returnClause.orderBy = new ArrayList<>(orderBy.stream().map(e->e.getCopy()).collect(Collectors.toList()));
        returnClause.isOrderByDesc = this.isOrderByDesc;
        returnClause.distinct = this.distinct;
        return returnClause;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("RETURN ");
        if(distinct){
            sb.append("DISTINCT ");
        }
        List<IRet> returnList = getReturnList();
        for(int i = 0; i < returnList.size(); i++){
            returnList.get(i).toTextRepresentation(sb);
            if(i != returnList.size()-1){
                sb.append(", ");
            }
        }
        if(orderBy != null && orderBy.size() != 0){
            sb.append(" ORDER BY ");
            for(int i = 0; i < orderBy.size(); i++){
                orderBy.get(i).toTextRepresentation(sb);
                if(i != orderBy.size()-1){
                    sb.append(", ");
                }
            }
            if(isOrderByDesc){
                sb.append(" DESC");
            }
        }
        if(skip != null){
            sb.append(" SKIP ");
            skip.toTextRepresentation(sb);
        }
        if(limit != null){
            sb.append(" LIMIT ");
            limit.toTextRepresentation(sb);
        }
    }

    @Override
    public List<IPattern> getLocalPatternContainsIdentifier(IIdentifier identifier) {
        return new ArrayList<>();
    }

    @Override
    public IReturn getSource() {
        return this;
    }
}
