package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ast.IWith;

public interface IWithAnalyzer extends IWith, IClauseAnalyzer {
    @Override
    IWith getSource();
}
