package org.example.gdsmith.neo4j.oracle;

import org.example.gdsmith.cypher.oracle.NoRecOracle;
import org.example.gdsmith.neo4j.Neo4jGlobalState;
import org.example.gdsmith.neo4j.schema.Neo4jSchema;

public class Neo4jNoRecOracle extends NoRecOracle<Neo4jGlobalState, Neo4jSchema> {
    public Neo4jNoRecOracle(Neo4jGlobalState globalState) {
        super(globalState);
    }
}
