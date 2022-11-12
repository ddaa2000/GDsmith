package org.example.gdsmith.cypher.ast;

import java.util.List;

public interface IPattern extends ITextRepresentation, ICopyable{
    List<IPatternElement> getPatternElements();

    void setPatternElements(List<IPatternElement> elements);

    @Override
    IPattern getCopy();
}
