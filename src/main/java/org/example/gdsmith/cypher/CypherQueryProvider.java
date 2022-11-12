package org.example.gdsmith.cypher;

public interface CypherQueryProvider<S> {
    CypherQueryAdapter getQuery(S globalState) throws Exception;
}
