package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.ast.analyzer.IMatchAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IWithAnalyzer;

public interface IConditionGenerator {
    void fillMatchCondtion(IMatchAnalyzer matchClause);
    void fillWithCondition(IWithAnalyzer withClause);
}
