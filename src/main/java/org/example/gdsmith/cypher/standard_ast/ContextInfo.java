package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.analyzer.IClauseAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IContextInfo;
import org.example.gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;

import java.util.List;

public class ContextInfo implements IContextInfo {
    List<IIdentifierAnalyzer> identifierAnalyzers;
    IClauseAnalyzer clauseAnalyzer;

    ContextInfo(IClauseAnalyzer clauseAnalyzer, List<IIdentifierAnalyzer> identifierAnalyzers){
        this.clauseAnalyzer = clauseAnalyzer;
        this.identifierAnalyzers = identifierAnalyzers;
    }

    @Override
    public List<IIdentifierAnalyzer> getIdentifiers() {
        return identifierAnalyzers;
    }

    @Override
    public IIdentifierAnalyzer getIdentifierByName(String name) {
        return identifierAnalyzers.stream().filter(i->i.getName().equals(name)).findAny().orElse(null);
    }

    @Override
    public IClauseAnalyzer getParentClause() {
        return clauseAnalyzer;
    }
}
