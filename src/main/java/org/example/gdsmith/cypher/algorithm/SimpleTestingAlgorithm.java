package org.example.gdsmith.cypher.algorithm;

import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherProviderAdapter;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.cypher.gen.query.RandomCoverageQueryGenerator;
import org.example.gdsmith.cypher.gen.graph.RandomGraphGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;

import java.util.List;

public class SimpleTestingAlgorithm  <S extends CypherSchema<G,?>, G extends CypherGlobalState<O, S>,
        O extends DBMSSpecificOptions<? extends OracleFactory<G>>, C extends CypherConnection> extends SimpleTestingAlgorithmBase<S,G,O,C>{

    public SimpleTestingAlgorithm(CypherProviderAdapter<G, S, O> provider) {
        super(provider);
    }

    @Override
    public IQueryGenerator<S, G> createQueryGenerator() {
        return new RandomCoverageQueryGenerator<>();
    }

    @Override
    public void generateDatabase(G globalState) throws Exception {
        List<CypherQueryAdapter> queries = new RandomGraphGenerator<G,S>(globalState).createGraph(globalState);
        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
        }
    }
}
