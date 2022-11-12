package org.example.gdsmith;

import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.dsl.IGraphGenerator;

public interface IGraphGeneratorFactory <G extends CypherGlobalState<?,?>, GG extends IGraphGenerator<G>>{
    GG create(G globalState);
}
