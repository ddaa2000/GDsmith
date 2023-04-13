package org.example.gdsmith.cypher.gen.list;

import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.IRet;
import org.example.gdsmith.cypher.ast.analyzer.IUnwindAnalyzer;
import org.example.gdsmith.cypher.dsl.BasicListGenerator;
import org.example.gdsmith.cypher.dsl.IIdentifierBuilder;
import org.example.gdsmith.cypher.gen.EnumerationSeq;
import org.example.gdsmith.cypher.gen.expr.EnumerationExpressionGenerator;
import org.example.gdsmith.cypher.gen.expr.RandomExpressionGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.Ret;

public class EnumerationListGenerator<S extends CypherSchema<?,?>> extends BasicListGenerator<S> {
    private EnumerationSeq enumerationSeq;


    public EnumerationListGenerator(S schema, IIdentifierBuilder identifierBuilder, EnumerationSeq enumerationSeq) {
        super(schema, identifierBuilder);
        this.enumerationSeq = enumerationSeq;
    }

    @Override
    public IRet generateList(IUnwindAnalyzer unwindAnalyzer, IIdentifierBuilder identifierBuilder, S schema) {
        IExpression listExpression = new EnumerationExpressionGenerator<>(unwindAnalyzer, enumerationSeq, schema).generateListWithBasicType(2, CypherType.NUMBER);
        return Ret.createNewExpressionAlias(identifierBuilder, listExpression);
    }
}
