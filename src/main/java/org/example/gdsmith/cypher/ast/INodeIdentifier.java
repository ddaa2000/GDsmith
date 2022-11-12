package org.example.gdsmith.cypher.ast;

import java.util.List;

public interface INodeIdentifier extends IPatternElement{
    List<IProperty> getProperties();
    List<ILabel> getLabels();
    void setProperties(List<IProperty> properties);
    INodeIdentifier createRef();
}
