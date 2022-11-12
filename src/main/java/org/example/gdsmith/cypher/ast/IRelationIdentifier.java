package org.example.gdsmith.cypher.ast;

import java.util.List;

public interface IRelationIdentifier extends IPatternElement{
    List<IProperty> getProperties();
    List<IType> getTypes();
    Direction getDirection();
    void setDirection(Direction direction);
    IRelationIdentifier createRef();
    int getLengthLowerBound();
    int getLengthUpperBound();
    void setProperties(List<IProperty> properties);
}
