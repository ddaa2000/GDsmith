package org.example.gdsmith.cypher;

import org.example.gdsmith.cypher.ast.ILabel;
import org.example.gdsmith.cypher.ast.IType;
import org.example.gdsmith.cypher.schema.IFunctionInfo;
import org.example.gdsmith.cypher.schema.ILabelInfo;
import org.example.gdsmith.cypher.schema.IRelationTypeInfo;

import java.util.List;

public interface ICypherSchema {
    boolean containsLabel(ILabel label);
    ILabelInfo getLabelInfo(ILabel label);
    boolean containsRelationType(IType relation);
    IRelationTypeInfo getRelationInfo(IType relation);
    List<IFunctionInfo> getFunctions();

    List<ILabelInfo> getLabelInfos();
    List<IRelationTypeInfo> getRelationshipTypeInfos();
}
