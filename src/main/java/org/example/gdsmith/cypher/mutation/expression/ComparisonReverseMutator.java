package org.example.gdsmith.cypher.mutation.expression;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.standard_ast.expr.BinaryComparisonExpression;

import java.util.ArrayList;
import java.util.List;

public class ComparisonReverseMutator extends ExpressionVisitor {

    private static final int reversingProbability = 30;
    private List<BinaryComparisonExpression> comparisonExpressions = new ArrayList<>();

    public ComparisonReverseMutator(IClauseSequence clauseSequence) {
        super(clauseSequence);
    }

    @Override
    public ExpressionVisitingResult visitComparisonExpression(ExpressionVisitorContext context, BinaryComparisonExpression expression){
        comparisonExpressions.add(expression);

        return ExpressionVisitingResult.continueToVisit();
    }

    @Override
    public void postProcessing(ExpressionVisitorContext context){
        if(comparisonExpressions.size() == 0){
            return;
        }
        BinaryComparisonExpression expression = comparisonExpressions.get(new Randomly().getInteger(0, comparisonExpressions.size()));

        switch (expression.getOperation()){
            case EQUAL:
                expression.setOperation(BinaryComparisonExpression.BinaryComparisonOperation.NOT_EQUAL);
                break;
            case NOT_EQUAL:
                expression.setOperation(BinaryComparisonExpression.BinaryComparisonOperation.EQUAL);
                break;
            case HIGHER_OR_EQUAL:
                expression.setOperation(BinaryComparisonExpression.BinaryComparisonOperation.SMALLER);
                break;
            case HIGHER:
                expression.setOperation(BinaryComparisonExpression.BinaryComparisonOperation.SMALLER_OR_EQUAL);
                break;
            case SMALLER_OR_EQUAL:
                expression.setOperation(BinaryComparisonExpression.BinaryComparisonOperation.HIGHER);
                break;
            case SMALLER:
                expression.setOperation(BinaryComparisonExpression.BinaryComparisonOperation.HIGHER_OR_EQUAL);
                break;
        }
    }
}

