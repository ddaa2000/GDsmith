package org.example.gdsmith.cypher.schema;

import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.standard_ast.CypherType;

import java.util.List;

public interface IFunctionInfo {
    String getName();
    String getSignature();
    List<IParamInfo> getParams();
    CypherType getExpectedReturnType();
    ICypherTypeDescriptor calculateReturnType(List<IExpression> params);
}
