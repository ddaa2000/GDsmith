package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.dsl.QueryFiller.QueryFillerContext;

public class QueryFiller<S extends CypherSchema<?,?>> extends ClauseVisitor<QueryFillerContext<S>>{


    public static class QueryFillerContext<S extends CypherSchema<?,?>> implements IContext{
        private S schema;
        private IIdentifierBuilder identifierBuilder;
        private QueryFillerContext(S schema, IIdentifierBuilder identifierBuilder){
            this.schema = schema;
            this.identifierBuilder = identifierBuilder;
        }
    }

    private IPatternGenerator patternGenerator;
    private IConditionGenerator conditionGenerator;
    private IAliasGenerator aliasGenerator;
    private IListGenerator listGenerator;


    public QueryFiller(IClauseSequence clauseSequence, IPatternGenerator patternGenerator,
                       IConditionGenerator conditionGenerator, IAliasGenerator aliasGenerator,
                       IListGenerator listGenerator,
                       S schema, IIdentifierBuilder identifierBuilder){
        super(clauseSequence, new QueryFillerContext<>(schema, identifierBuilder));
        this.patternGenerator = patternGenerator;
        this.conditionGenerator = conditionGenerator;
        this.aliasGenerator = aliasGenerator;
        this.listGenerator = listGenerator;
    }

    @Override
    public void visitMatch(IMatch matchClause, QueryFillerContext<S> context) {
        if(patternGenerator!=null){
            patternGenerator.fillMatchPattern(matchClause.toAnalyzer());
        }
        if(conditionGenerator!=null){
            conditionGenerator.fillMatchCondtion(matchClause.toAnalyzer());
        }
    }

    @Override
    public void visitWith(IWith withClause, QueryFillerContext<S> context) {
        if(aliasGenerator!=null){
            aliasGenerator.fillWithAlias(withClause.toAnalyzer());
        }
        if(conditionGenerator!=null){
            conditionGenerator.fillWithCondition(withClause.toAnalyzer());
        }

    }

    @Override
    public void visitReturn(IReturn returnClause, QueryFillerContext<S> context) {
        if(aliasGenerator!=null){
            aliasGenerator.fillReturnAlias(returnClause.toAnalyzer());
        }
    }

    @Override
    public void visitCreate(ICreate createClause, QueryFillerContext<S> context) {

    }

    @Override
    public void visitUnwind(IUnwind unwindClause, QueryFillerContext<S> context) {
        if(listGenerator!=null){
            listGenerator.fillUnwindList(unwindClause.toAnalyzer());
        }
    }
}
