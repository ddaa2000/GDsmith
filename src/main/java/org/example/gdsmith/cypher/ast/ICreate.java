package org.example.gdsmith.cypher.ast;

import org.example.gdsmith.cypher.ast.analyzer.ICreateAnalyzer;

public interface ICreate extends ICypherClause{
    IPattern getPattern();
    void setPattern(IPattern pattern);

    @Override
    ICreateAnalyzer toAnalyzer();
}
