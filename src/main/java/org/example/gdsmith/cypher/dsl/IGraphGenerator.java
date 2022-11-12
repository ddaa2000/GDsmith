package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherQueryAdapter;

import java.util.List;

public interface IGraphGenerator <G extends CypherGlobalState<?,?>> {
    List<CypherQueryAdapter> createGraph(G globalState) throws Exception;
}
