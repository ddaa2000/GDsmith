package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.ast.analyzer.IUnwindAnalyzer;

public interface IListGenerator {
    void fillUnwindList(IUnwindAnalyzer unwindAnalyzer);
}
