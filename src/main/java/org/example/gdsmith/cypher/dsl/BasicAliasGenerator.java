package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.ast.IRet;
import org.example.gdsmith.cypher.ast.analyzer.IReturnAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IWithAnalyzer;
import org.example.gdsmith.cypher.schema.CypherSchema;

import java.util.List;

public abstract class BasicAliasGenerator<S extends CypherSchema<?,?>> implements IAliasGenerator{

    protected final S schema;
    private final IIdentifierBuilder identifierBuilder;

    public BasicAliasGenerator(S schema, IIdentifierBuilder identifierBuilder){
        this.schema = schema;
        this.identifierBuilder = identifierBuilder;
    }

    @Override
    public void fillReturnAlias(IReturnAnalyzer returnClause) {
        returnClause.setReturnList(generateReturnAlias(returnClause, identifierBuilder, schema));
    }

    @Override
    public void fillWithAlias(IWithAnalyzer withClause) {
        withClause.setReturnList(generateWithAlias(withClause, identifierBuilder, schema));
    }

    public abstract List<IRet> generateReturnAlias(IReturnAnalyzer returnClause, IIdentifierBuilder identifierBuilder, S schema);
    public abstract List<IRet> generateWithAlias(IWithAnalyzer withClause, IIdentifierBuilder identifierBuilder, S schema);
}
