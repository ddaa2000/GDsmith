package org.example.gdsmith.cypher.ast;

import org.example.gdsmith.cypher.ast.analyzer.IWithAnalyzer;

import java.util.List;

public interface IWith extends ICypherClause{
    boolean isDistinct();
    void setDistinct(boolean isDistinct);
    List<IRet> getReturnList();
    void setReturnList(List<IRet> returnList);
    IExpression getCondition();
    void setCondition(IExpression condtion);

    void setOrderBy(List<IExpression> expression, boolean isDesc);
    List<IExpression> getOrderByExpressions();
    boolean isOrderByDesc();

    void setLimit(IExpression expression);
    IExpression getLimit();

    void setSkip(IExpression expression);
    IExpression getSkip();


    @Override
    IWithAnalyzer toAnalyzer();
}
