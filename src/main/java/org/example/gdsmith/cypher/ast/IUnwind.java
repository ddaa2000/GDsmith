package org.example.gdsmith.cypher.ast;

import org.example.gdsmith.cypher.ast.analyzer.IUnwindAnalyzer;

public interface IUnwind extends ICypherClause{
    IRet getListAsAliasRet();
    void setListAsAliasRet(IRet listAsAlias);

    @Override
    IUnwindAnalyzer toAnalyzer();
}
