package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.ast.analyzer.IMatchAnalyzer;

public interface IPatternGenerator {
    void fillMatchPattern(IMatchAnalyzer match);
}
