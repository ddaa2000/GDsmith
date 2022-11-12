package org.example.gdsmith.neo4j.gen;

import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.neo4j.Neo4jGlobalState;

public class Neo4jNodeGenerator {

    private final Neo4jGlobalState globalState;
    public Neo4jNodeGenerator(Neo4jGlobalState globalState){
        this.globalState = globalState;
    }

    public static CypherQueryAdapter createNode(Neo4jGlobalState globalState){
        return new Neo4jNodeGenerator(globalState).generateCreate();
    }

    public CypherQueryAdapter generateCreate(){
        return new CypherQueryAdapter("CREATE (p:Person{id: 1})");
    }
}
