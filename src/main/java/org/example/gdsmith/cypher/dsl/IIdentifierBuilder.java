package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.ast.ICopyable;

public interface IIdentifierBuilder extends ICopyable {
    String getNewNodeName();

    String getNewRelationName();

    String getNewAliasName();

    @Override
    IIdentifierBuilder getCopy();
}
