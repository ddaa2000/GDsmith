package org.example.gdsmith.cypher;

import org.example.gdsmith.*;
import org.example.gdsmith.cypher.algorithm.*;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.MainOptions;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.ProviderAdapter;
import org.example.gdsmith.cypher.algorithm.*;

public abstract  class CypherProviderAdapter <G extends CypherGlobalState<O, S>, S extends CypherSchema<G,?>, O extends DBMSSpecificOptions<? extends OracleFactory<G>>> extends ProviderAdapter<G, O, CypherConnection> {

    public CypherProviderAdapter(Class<G> globalClass, Class<O> optionClass) {
        super(globalClass, optionClass);
    }

    @Override
    public void generateAndTestDatabase(G globalState) throws Exception { //todo 主过程
        CypherTestingAlgorithm<S,G,O,CypherConnection> algorithm;
        switch (globalState.getOptions().getAlgorithm()){
            case SIMPLE:
                algorithm = new SimpleTestingAlgorithm<S,G,O,CypherConnection>(this);
                break;
            case PATTERN_GUIDED:
                algorithm = new NonEmptyAlgorithm<>(this);
                break;
            case MANUAL:
                algorithm = new ManualDifferentialAlgorithm<>(this);
                break;
            case NON_EMPTY://treeGraph, guidedPattern, guidedCondition
                algorithm = new CoverageGuidedAlgorithm<>(this);
                break;
            case COMPARED1://treeGraph, randPattern, randCondition
                algorithm = new Compared1AlgorithmNew<>(this);
                break;
            case COMPARED2://treeGraph, guidedPattern, randCondition
                algorithm = new Compared2AlgorithmNew<>(this);
                break;
            case COMPARED3://randGraph, guidedPattern, guidedCondition
                algorithm = new Compared3AlgorithmNew<>(this);
                break;
            case COMPARED4://randGraph, randPattern, randCondition
                algorithm = new Compared4Algorithm<>(this);
                break;
            case COMPARED5://randGraph, guidedPattern, randCondition
                algorithm = new Compared5Algorithm<>(this);
                break;
            default:
                throw new RuntimeException();
        }
        algorithm.generateAndTestDatabase(globalState);
        System.gc();
    }

    @Override
    protected void checkViewsAreValid(G globalState){

    }

    public abstract CypherConnection createDatabaseWithOptions(MainOptions mainOptions, O specificOptions) throws Exception;

}
