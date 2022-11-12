package org.example.gdsmith;

import org.example.gdsmith.cypher.dsl.IQueryGenerator;

public interface IGeneratorFactory <G extends IQueryGenerator>{
    G create();
}
