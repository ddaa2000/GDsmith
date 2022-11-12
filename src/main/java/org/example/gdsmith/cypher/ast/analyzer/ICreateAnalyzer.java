package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ast.ICreate;

public interface ICreateAnalyzer extends ICreate, IClauseAnalyzer {
    @Override
    ICreate getSource();
}
