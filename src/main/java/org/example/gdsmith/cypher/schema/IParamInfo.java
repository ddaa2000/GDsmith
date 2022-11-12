package org.example.gdsmith.cypher.schema;

import org.example.gdsmith.cypher.standard_ast.CypherType;

public interface IParamInfo {
    boolean isOptionalLength();
    CypherType getParamType();
}
