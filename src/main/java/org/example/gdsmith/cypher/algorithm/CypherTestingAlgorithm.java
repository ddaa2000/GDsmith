package org.example.gdsmith.cypher.algorithm;

import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherProviderAdapter;
import org.example.gdsmith.cypher.schema.CypherSchema;

public abstract class CypherTestingAlgorithm <S extends CypherSchema<G,?>, G extends CypherGlobalState<O, S>,
        O extends DBMSSpecificOptions<? extends OracleFactory<G>>, C extends CypherConnection>{

    protected CypherProviderAdapter<G,S,O> provider;
    public CypherTestingAlgorithm(CypherProviderAdapter<G,S,O> provider){
        this.provider = provider;
    }

    public abstract void generateAndTestDatabase(G globalState) throws Exception;
}
