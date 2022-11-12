package org.example.gdsmith.cypher.ast;

import org.example.gdsmith.cypher.ast.analyzer.IMergeAnalyzer;

public interface IMerge extends ICypherClause{
    IPattern getPattern();
    void setPattern(IPattern pattern);

    @Override
    IMergeAnalyzer toAnalyzer();
}
