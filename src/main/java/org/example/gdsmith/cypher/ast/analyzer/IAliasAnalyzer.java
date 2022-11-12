package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IAlias;
import org.example.gdsmith.cypher.ast.IExpression;


public interface IAliasAnalyzer extends IAlias, IIdentifierAnalyzer {
    @Override
    IAliasAnalyzer getFormerDef();
    void setFormerDef(IAliasAnalyzer formerDef);
    IExpression getAliasDefExpression();

    @Override
    IAlias getSource();

    ICypherTypeDescriptor analyzeType(ICypherSchema cypherSchema);
}
