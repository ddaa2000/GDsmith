package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ast.IMerge;

public interface IMergeAnalyzer extends IMerge, IClauseAnalyzer {
    @Override
    IMerge getSource();
}
