package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ast.IMatch;

public interface IMatchAnalyzer extends IMatch, IClauseAnalyzer {
    @Override
    IMatch getSource();
}
