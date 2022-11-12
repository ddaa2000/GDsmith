package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.IWithAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class With extends CypherClause implements IWithAnalyzer {

    private boolean distinct = false;
    private IExpression condition = null, skip = null, limit = null;
    private List<IExpression> orderBy = new ArrayList<>();
    boolean isOrderByDesc = false;

    public With(){
        super(false);
    }


    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        this.distinct = isDistinct;
    }

    @Override
    public List<IRet> getReturnList() {
        return symtab.getAliasDefinitions();
    }

    @Override
    public void setReturnList(List<IRet> returnList) {
        this.symtab.setAliasDefinition(returnList);
    }

    @Override
    public IExpression getCondition() {
        return condition;
    }

    @Override
    public void setCondition(IExpression condtion) {
        this.condition = condtion;
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
    public IWithAnalyzer toAnalyzer() {
        return this;
    }

    @Override
    public ICypherClause getCopy() {
        With with = new With();
        with.distinct = distinct;
        if(condition != null){
            with.condition = condition.getCopy();
        }
        else {
            with.condition = null;
        }
        if(symtab != null){
            with.symtab.setPatterns(symtab.getPatterns().stream().map(p->p.getCopy()).collect(Collectors.toList()));
            with.symtab.setAliasDefinition(symtab.getAliasDefinitions().stream().map(a->a.getCopy()).collect(Collectors.toList()));
        }
        if(skip != null){
            with.skip = skip.getCopy();
        }
        if(limit != null){
            with.limit = limit.getCopy();
        }
        with.orderBy = new ArrayList<>(orderBy.stream().map(e->e.getCopy()).collect(Collectors.toList()));
        with.isOrderByDesc = this.isOrderByDesc;
        with.distinct = this.distinct;
        return with;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        //todo distinct
        sb.append("WITH ");
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
        if(condition != null){
            sb.append(" WHERE ");
            condition.toTextRepresentation(sb);
        }
    }

    @Override
    public List<IPattern> getLocalPatternContainsIdentifier(IIdentifier identifier) {
        return new ArrayList<>();
    }

    @Override
    public IWith getSource() {
        return this;
    }
}
