package org.example.gdsmith.cypher.mutation.expression;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.standard_ast.expr.ConstExpression;
import org.example.gdsmith.cypher.standard_ast.expr.StringMatchingExpression;

import java.util.ArrayList;
import java.util.List;

public class StringMatchReductionMutator extends ExpressionVisitor{
    private List<StringMatchingExpression> stringMatchingExpressions = new ArrayList<>();

    public StringMatchReductionMutator(IClauseSequence clauseSequence) {
        super(clauseSequence);
    }

    @Override
    public ExpressionVisitingResult visitStringMatchingExpression(ExpressionVisitorContext context, StringMatchingExpression expression) {
        if(expression.getParentExpression() instanceof ConstExpression){
            stringMatchingExpressions.add(expression);
        }
        return ExpressionVisitor.ExpressionVisitingResult.continueToVisit();
    }

    @Override
    public void postProcessing(ExpressionVisitorContext context) {
        Randomly randomly = new Randomly();
        if(stringMatchingExpressions.size() == 0){
            return;
        }
        StringMatchingExpression expression = stringMatchingExpressions.get(randomly.getInteger(0, stringMatchingExpressions.size()));
        ConstExpression constExpression = (ConstExpression) expression.getParentExpression();

        String pattern = (String) constExpression.getValue();
        if(pattern.length() >= 2){
            String subPattern = pattern.substring(randomly.getInteger(1, pattern.length()));
            if(subPattern.length() >= 2){
                subPattern = pattern.substring(randomly.getInteger(1, pattern.length()));
            }
            constExpression.setValue(subPattern);
        }
    }
}
