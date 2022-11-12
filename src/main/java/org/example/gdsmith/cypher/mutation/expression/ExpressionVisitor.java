package org.example.gdsmith.cypher.mutation.expression;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.ClauseVisitor;
import org.example.gdsmith.cypher.dsl.IContext;
import org.example.gdsmith.cypher.mutation.IClauseMutator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.mutation.expression.ExpressionVisitor.ExpressionVisitorContext;
import org.example.gdsmith.cypher.standard_ast.expr.*;
import org.example.gdsmith.cypher.standard_ast.expr.*;

import java.util.LinkedList;
import java.util.List;

public abstract class ExpressionVisitor<S extends CypherSchema<?,?>> extends ClauseVisitor<ExpressionVisitorContext>  implements IClauseMutator {

    public ExpressionVisitor(IClauseSequence clauseSequence) {
        super(clauseSequence, new ExpressionVisitorContext());
    }

    public static class ExpressionVisitorContext implements IContext {
        private List<IExpression> expressionStack = new LinkedList<>();

        public void popExpressionStack(){
            expressionStack.remove(expressionStack.size()-1);
        }

        public void pushExpressionStack(IExpression expression){
            expressionStack.add(expression);
        }

        public void clearStack(){
            expressionStack.clear();
        }

        public void rebuildStack(IExpression expression){
            expressionStack.clear();
            expressionStack.add(expression);
            while (expression.getParentExpression() != null){
                expression = expression.getParentExpression();
                expressionStack.add(0, expression);
            }
        }

        public IExpression getTopExpression(){
            if(expressionStack.size() == 0){
                return null;
            }
            return expressionStack.get(expressionStack.size() - 1);
        }

        public List<IExpression> getExpressionStack(){
            return expressionStack;
        }
    }

    public static class ExpressionVisitingResult {

        public enum NextVisitStrategy{
            CONTINUE_TO_VISIT, BREAK_TO_PARENT, BREAK_FROM_CLAUSE, BREAK_FROM_MUTATOR
        }

        private NextVisitStrategy strategy;


        @Deprecated
        private IExpression nextToVisit;

        private ExpressionVisitingResult(NextVisitStrategy strategy, IExpression nextToVisit){
            this.strategy = strategy;
            this.nextToVisit = nextToVisit;
        }

        public static ExpressionVisitingResult continueToVisit(){
            return new ExpressionVisitingResult(NextVisitStrategy.CONTINUE_TO_VISIT, null);
        }
//        public static ExpressionMutationResult gotoVisit(IExpression nextToVisit){
//            return new ExpressionMutationResult(NextVisitStrategy.GOTO_VISIT, nextToVisit);
//        }
        public static ExpressionVisitingResult breakToParent(){
            return new ExpressionVisitingResult(NextVisitStrategy.BREAK_TO_PARENT, null);
        }
        public static ExpressionVisitingResult breakFromClause(){
            return new ExpressionVisitingResult(NextVisitStrategy.BREAK_FROM_CLAUSE, null);
        }
        public static ExpressionVisitingResult breakFromMutator(){
            return new ExpressionVisitingResult(NextVisitStrategy.BREAK_FROM_MUTATOR, null);
        }
    }

    @Override
    public void postProcessing(ExpressionVisitorContext context){

    }

    @Override
    public void visitMatch(IMatch matchClause, ExpressionVisitorContext context) {
        context.clearStack();
        if(matchClause.getCondition() != null){
            ExpressionVisitingResult result = visitExpression(context, matchClause.getCondition());
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.BREAK_FROM_MUTATOR){
                stopVisit();
            }
        }
    }

    public void mutate(){
        startVisit();
    }

    public ExpressionVisitingResult visitBinaryLogicalExpression(ExpressionVisitorContext context, BinaryLogicalExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitComparisonExpression(ExpressionVisitorContext context, BinaryComparisonExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitBinaryNumberExpression(ExpressionVisitorContext context, BinaryNumberExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitCallExpression(ExpressionVisitorContext context, CallExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitConstExpression(ExpressionVisitorContext context, ConstExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitCreateListExpression(ExpressionVisitorContext context, CreateListExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitCreateMapExpression(ExpressionVisitorContext context, CreateMapExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitGetListElementExpression(ExpressionVisitorContext context, GetListElementExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitGetListSliceExpression(ExpressionVisitorContext context, GetListSliceExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitGetPropertyExpression(ExpressionVisitorContext context, GetPropertyExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitIdentifierExpression(ExpressionVisitorContext context, IdentifierExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitSingleLogicalExpression(ExpressionVisitorContext context, SingleLogicalExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitStar(ExpressionVisitorContext context, Star expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitStringCatExpression(ExpressionVisitorContext context, StringCatExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    public ExpressionVisitingResult visitStringMatchingExpression(ExpressionVisitorContext context, StringMatchingExpression expression){
        return ExpressionVisitingResult.continueToVisit();
    }

    private ExpressionVisitingResult visitExpression(ExpressionVisitorContext context, IExpression expression){
        ExpressionVisitingResult result = null;

        context.rebuildStack(expression);
        if(expression instanceof BinaryComparisonExpression){
            result = visitComparisonExpression(context, (BinaryComparisonExpression) expression);
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT){
                result = visitExpression(context, ((BinaryComparisonExpression) expression).getLeftExpression());
                if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT) {
                    result = visitExpression(context, ((BinaryComparisonExpression) expression).getRightExpression());
                }
            }
        }
        else if(expression instanceof BinaryLogicalExpression){
            result = visitBinaryLogicalExpression(context, (BinaryLogicalExpression) expression);
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT){
                result = visitExpression(context, ((BinaryLogicalExpression) expression).getLeftExpression());
                if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT) {
                    result = visitExpression(context, ((BinaryLogicalExpression) expression).getRightExpression());
                }
            }
        }
        else if(expression instanceof BinaryNumberExpression){
            result = visitBinaryNumberExpression(context, (BinaryNumberExpression) expression);
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT){
                result = visitExpression(context, ((BinaryNumberExpression) expression).getLeft());
                if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT) {
                    result = visitExpression(context, ((BinaryNumberExpression) expression).getRight());
                }
            }
        }
        else if (expression instanceof CallExpression) {
            result = visitCallExpression(context, (CallExpression) expression);
            //todo: more
        }
        else if (expression instanceof ConstExpression) {
            result = visitConstExpression(context, (ConstExpression) expression);
        }
        else if(expression instanceof CreateListExpression){
            result = visitCreateListExpression(context, (CreateListExpression) expression);
            //todo: more
        }
        else if(expression instanceof CreateMapExpression){
            result = visitCreateMapExpression(context, (CreateMapExpression) expression);
            //todo: more
        }
        else if(expression instanceof GetListElementExpression){
            result = visitGetListElementExpression(context, (GetListElementExpression) expression);
            //todo: more
        }
        else if(expression instanceof GetListSliceExpression){
            result = visitGetListSliceExpression(context, (GetListSliceExpression) expression);
            //todo: more
        }
        else if(expression instanceof GetPropertyExpression){
            result = visitGetPropertyExpression(context, (GetPropertyExpression) expression);
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT){
                result = visitExpression(context, ((GetPropertyExpression) expression).getFromExpression());
            }
        }
        else if(expression instanceof IdentifierExpression){
            result = visitIdentifierExpression(context, (IdentifierExpression) expression);
        }
        else if(expression instanceof SingleLogicalExpression){
            result = visitSingleLogicalExpression(context, (SingleLogicalExpression) expression);
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT){
                result = visitExpression(context, ((SingleLogicalExpression) expression).getChildExpression());
            }
        }
        else if(expression instanceof Star){
            result = visitStar(context, (Star) expression);
        }
        else if(expression instanceof StringCatExpression){
            result = visitStringCatExpression(context, (StringCatExpression) expression);
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT){
                result = visitExpression(context, ((StringCatExpression) expression).getLeft());
                if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT) {
                    result = visitExpression(context, ((StringCatExpression) expression).getRight());
                }
            }
        }
        else if(expression instanceof StringMatchingExpression){
            result = visitStringMatchingExpression(context, (StringMatchingExpression) expression);
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT){
                result = visitExpression(context, ((StringMatchingExpression) expression).getSource());
                if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.CONTINUE_TO_VISIT) {
                    result = visitExpression(context, ((StringMatchingExpression) expression).getPattern());
                }
            }
        }

        switch (result.strategy){
            case BREAK_TO_PARENT:
                return ExpressionVisitingResult.continueToVisit();
            case CONTINUE_TO_VISIT:
            case BREAK_FROM_CLAUSE:
            case BREAK_FROM_MUTATOR:
                return result;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void visitWith(IWith withClause, ExpressionVisitorContext context) {
        context.clearStack();
        if(withClause.getCondition() != null){
            ExpressionVisitingResult result = visitExpression(context, withClause.getCondition());
            if(result.strategy == ExpressionVisitingResult.NextVisitStrategy.BREAK_FROM_MUTATOR){
                stopVisit();
            }
        }
    }

    @Override
    public void visitReturn(IReturn returnClause, ExpressionVisitorContext context) {
        return;
    }

    @Override
    public void visitCreate(ICreate createClause, ExpressionVisitorContext context) {
        return;
    }

    @Override
    public void visitUnwind(IUnwind unwindClause, ExpressionVisitorContext context) {
        return;
    }

}
