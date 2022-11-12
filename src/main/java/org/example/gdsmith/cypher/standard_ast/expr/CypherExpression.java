package org.example.gdsmith.cypher.standard_ast.expr;

import org.example.gdsmith.cypher.ast.ICypherClause;
import org.example.gdsmith.cypher.ast.IExpression;

public abstract class CypherExpression implements IExpression {
    protected IExpression parentExpression;
    protected ICypherClause parentClause;

    @Override
    public IExpression getParentExpression() {
        return parentExpression;
    }

    @Override
    public void setParentExpression(IExpression parentExpression){
        this.parentExpression = parentExpression;
    }

    @Override
    public ICypherClause getExpressionRootClause() {
        if(parentExpression != null){
            return parentExpression.getExpressionRootClause();
        }
        return parentClause;
    }

    @Override
    public void setParentClause(ICypherClause parentClause){
        this.parentClause = parentClause;
    }

}
