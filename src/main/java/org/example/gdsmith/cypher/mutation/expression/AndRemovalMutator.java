package org.example.gdsmith.cypher.mutation.expression;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.standard_ast.expr.BinaryLogicalExpression;

import java.util.ArrayList;
import java.util.List;

public class AndRemovalMutator extends ExpressionVisitor{

    public List<BinaryLogicalExpression> andExpressions = new ArrayList<>();

    public AndRemovalMutator(IClauseSequence clauseSequence) {
        super(clauseSequence);
    }

    @Override
    public ExpressionVisitingResult visitBinaryLogicalExpression(ExpressionVisitorContext context, BinaryLogicalExpression expression) {
        if(expression.getOperation() == BinaryLogicalExpression.BinaryLogicalOperation.AND){
            andExpressions.add(expression);
        }
        return ExpressionVisitingResult.continueToVisit();
    }

    @Override
    public void postProcessing(ExpressionVisitorContext context) {
        Randomly randomly = new Randomly();
        if(andExpressions.size() == 0){
            return;
        }
        BinaryLogicalExpression expression = andExpressions.get(randomly.getInteger(0, andExpressions.size()));

        if(expression.getParentExpression() != null){
            IExpression parent = expression.getParentExpression();
            if(randomly.getInteger(0, 100) < 50){
                parent.replaceChild(expression, expression.getLeftExpression());
            }
            else {
                parent.replaceChild(expression, expression.getRightExpression());
            }
        }
        else if(expression.getExpressionRootClause() == null){
            //todo: more
        }
    }
}
