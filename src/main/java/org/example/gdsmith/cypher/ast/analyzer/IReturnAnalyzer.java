package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ast.IReturn;

public interface IReturnAnalyzer extends IReturn, IClauseAnalyzer {
    @Override
    IReturn getSource();
}
