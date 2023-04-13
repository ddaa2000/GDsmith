package org.example.gdsmith.cypher.gen.condition;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.IMatchAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IRelationAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IWithAnalyzer;
import org.example.gdsmith.cypher.dsl.BasicConditionGenerator;
import org.example.gdsmith.cypher.gen.EnumerationSeq;
import org.example.gdsmith.cypher.gen.expr.EnumerationExpressionGenerator;
import org.example.gdsmith.cypher.gen.expr.RandomExpressionGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.expr.*;
import org.example.gdsmith.cypher.standard_ast.expr.*;

import java.util.List;

public class EnumerationConditionGenerator<S extends CypherSchema<?,?>> extends BasicConditionGenerator<S> {
    private EnumerationSeq enumerationSeq;
    public EnumerationConditionGenerator(S schema, EnumerationSeq enumerationSeq) {
        super(schema);
        this.enumerationSeq = enumerationSeq;
    }

    private static final int MAX_DEPTH = 1;

    @Override
    public IExpression generateMatchCondition(IMatchAnalyzer matchClause, S schema) {
        List<IRelationAnalyzer> relationships = matchClause.getLocalRelationIdentifiers();

        if(enumerationSeq.getDecision()){
            if(relationships.size() != 0){
                IExpression result = new BinaryComparisonExpression(new GetPropertyExpression(new IdentifierExpression(relationships.get(0)), "id"), new ConstExpression(-1), BinaryComparisonExpression.BinaryComparisonOperation.HIGHER);
                for(int x = 0; x < relationships.size(); x++){
                    for(int y = x + 1; y < relationships.size(); y++){
                        result = new BinaryLogicalExpression(result, new BinaryComparisonExpression(new GetPropertyExpression(new IdentifierExpression(relationships.get(x)), "id"), new GetPropertyExpression(new IdentifierExpression(relationships.get(y)), "id"), BinaryComparisonExpression.BinaryComparisonOperation.NOT_EQUAL), BinaryLogicalExpression.BinaryLogicalOperation.AND);
                    }
                }
                return result;
            }
            return null;
        }
        IExpression result = new EnumerationExpressionGenerator<>(matchClause, enumerationSeq, schema).generateCondition(MAX_DEPTH);

        for(int x = 0; x < relationships.size(); x++){
            for(int y = x + 1; y < relationships.size(); y++){
                result = new BinaryLogicalExpression(result, new BinaryComparisonExpression(new GetPropertyExpression(new IdentifierExpression(relationships.get(x)), "id"), new GetPropertyExpression(new IdentifierExpression(relationships.get(y)), "id"), BinaryComparisonExpression.BinaryComparisonOperation.NOT_EQUAL), BinaryLogicalExpression.BinaryLogicalOperation.AND);
            }
        }
        return result;
    }

    @Override
    public IExpression generateWithCondition(IWithAnalyzer withClause, S schema) {
        if(enumerationSeq.getDecision()){
            return null;
        }
        return new RandomExpressionGenerator<>(withClause, schema).generateCondition(MAX_DEPTH);
    }
}
