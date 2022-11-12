package org.example.gdsmith.cypher.gen.condition;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.IMatchAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IRelationAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IWithAnalyzer;
import org.example.gdsmith.cypher.dsl.BasicConditionGenerator;
import org.example.gdsmith.cypher.gen.expr.NonEmptyExpressionGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.expr.*;
import org.example.gdsmith.cypher.standard_ast.expr.*;

import java.util.List;
import java.util.Map;

public class GuidedConditionGenerator<S extends CypherSchema<?,?>> extends BasicConditionGenerator<S> {
    private boolean overrideOld;
    private Map<String, Object> varToVal;

    public GuidedConditionGenerator(S schema, boolean overrideOld, Map<String, Object> varToVal) {
        super(schema);
        this.overrideOld = overrideOld;
        this.varToVal = varToVal;
    }

    private static final int NO_CONDITION_RATE = 50, MAX_DEPTH = 1;

    @Override
    public IExpression generateMatchCondition(IMatchAnalyzer matchClause, S schema) {
        IExpression matchCondition = matchClause.getCondition();
        if (matchCondition != null && !overrideOld) {
            return matchCondition;
        }

        Randomly r = new Randomly();
        List<IRelationAnalyzer> relationships = matchClause.getLocalRelationIdentifiers();

//        return null;
        if(r.getInteger(0, 100)< NO_CONDITION_RATE){
            if(relationships.size() != 0){
                IExpression result = new BinaryComparisonExpression(new GetPropertyExpression(new IdentifierExpression(relationships.get(0)), "id"), new ConstExpression(-1), BinaryComparisonExpression.BinaryComparisonOperation.HIGHER);
//                for(int i = 1; i < relationships.size(); i++){
//                    result = new BinaryLogicalExpression(result, new BinaryComparisonExpression(new GetPropertyExpression(new IdentifierExpression(relationships.get(i)), "id"), new ConstExpression(-1), BinaryComparisonExpression.BinaryComparisonOperation.HIGHER), BinaryLogicalExpression.BinaryLogicalOperation.AND);
//                }
                for(int x = 0; x < relationships.size(); x++){
                    for(int y = x + 1; y < relationships.size(); y++){
                        result = new BinaryLogicalExpression(result, new BinaryComparisonExpression(new GetPropertyExpression(new IdentifierExpression(relationships.get(x)), "id"), new GetPropertyExpression(new IdentifierExpression(relationships.get(y)), "id"), BinaryComparisonExpression.BinaryComparisonOperation.NOT_EQUAL), BinaryLogicalExpression.BinaryLogicalOperation.AND);
                    }
                }
                return result;
            }
            return null;
        }
        IExpression result = new NonEmptyExpressionGenerator<>(matchClause, schema, varToVal).generateCondition(MAX_DEPTH);

//        for(IRelationAnalyzer relationAnalyzer : relationships){
//            result = new BinaryLogicalExpression(result, new BinaryComparisonExpression(new GetPropertyExpression(new IdentifierExpression(relationAnalyzer), "id"), new ConstExpression(-1), BinaryComparisonExpression.BinaryComparisonOperation.HIGHER), BinaryLogicalExpression.BinaryLogicalOperation.AND);
//
//        }
        for(int x = 0; x < relationships.size(); x++){
            for(int y = x + 1; y < relationships.size(); y++){
                result = new BinaryLogicalExpression(result, new BinaryComparisonExpression(new GetPropertyExpression(new IdentifierExpression(relationships.get(x)), "id"), new GetPropertyExpression(new IdentifierExpression(relationships.get(y)), "id"), BinaryComparisonExpression.BinaryComparisonOperation.NOT_EQUAL), BinaryLogicalExpression.BinaryLogicalOperation.AND);
            }
        }
        return result;
    }

    @Override
    public IExpression generateWithCondition(IWithAnalyzer withClause, S schema) {
        IExpression withCondition = withClause.getCondition();
        if (withCondition != null) {
            return withCondition;
        }

//        return null;

        Randomly r = new Randomly();
        if(r.getInteger(0, 100)< NO_CONDITION_RATE){
            return null;
        }
        return new NonEmptyExpressionGenerator<>(withClause, schema, varToVal).generateCondition(MAX_DEPTH);
    }
}
