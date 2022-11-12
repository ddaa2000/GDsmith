package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.ast.analyzer.IReturnAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IWithAnalyzer;

public interface IAliasGenerator {
    void fillReturnAlias(IReturnAnalyzer returnClause);
    void fillWithAlias(IWithAnalyzer withClause);
}
