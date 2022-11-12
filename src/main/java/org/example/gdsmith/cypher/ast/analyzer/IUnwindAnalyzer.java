package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ast.IUnwind;

public interface IUnwindAnalyzer extends IUnwind, IClauseAnalyzer {
    @Override
    IUnwind getSource();
}
