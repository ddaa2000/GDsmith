package org.example.gdsmith.cypher.algorithm;

import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.IgnoreMeException;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.StateToReproduce;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherProviderAdapter;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.gen.*;
import org.example.gdsmith.cypher.gen.graph.EnumerationGraphGenerator;
import org.example.gdsmith.cypher.gen.query.EnumerationQueryGenerator;
import org.example.gdsmith.cypher.oracle.DifferentialNonEmptyBranchOracle;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.exceptions.DatabaseCrashException;
import org.example.gdsmith.exceptions.MustRestartDatabaseException;
import org.example.gdsmith.cypher.gen.GraphManager;
import org.opencypher.v9_0.expressions.functions.E;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class EnumerationAlgorithm<S extends CypherSchema<G,?>, G extends CypherGlobalState<O, S>,
        O extends DBMSSpecificOptions<? extends OracleFactory<G>>, C extends CypherConnection> extends CypherTestingAlgorithm<S,G,O,C>{
    private static EnumerationSeq graphSeq = new EnumerationSeq();


    public EnumerationAlgorithm(CypherProviderAdapter<G, S, O> provider) {
        super(provider);
    }

    @Override
    public void generateAndTestDatabase(G globalState) throws Exception {

        try {
            generateDatabase(globalState);
            globalState.getManager().incrementCreateDatabase(); //原子操作计数

            EnumerationSeq querySeq = new EnumerationSeq();
            TestOracle oracle = new DifferentialNonEmptyBranchOracle<G, S>(globalState, new EnumerationQueryGenerator<>(querySeq));

            for (int i = 0; i < globalState.getOptions().getNrQueries(); i++) {
                try (StateToReproduce.OracleRunReproductionState localState = globalState.getState().createLocalState()) {
                    assert localState != null;
                    try {
                        oracle.check();
                        globalState.getManager().incrementSelectQueryCount();
                    } catch (IgnoreMeException e) {
                    } catch (MustRestartDatabaseException e){
                        throw e;
                    } catch (DatabaseCrashException e){
                        if(e.getCause() instanceof MustRestartDatabaseException){
                            throw new MustRestartDatabaseException(e);
                        }
                        e.printStackTrace();
                        globalState.getLogger().logException(e, globalState.getState());
                    } catch (Exception e){
                        e.printStackTrace();
                        globalState.getLogger().logException(e, globalState.getState());
                    }

                    querySeq.finish();
                    querySeq.printPresent();
                    querySeq.randomize();

                    assert localState != null;
                    localState.executedWithoutError();

//                    if(querySeq.isEnded()){
//                        break;
//                    }

                }
            }
//            if(graphSeq.isEnded()){
//                System.exit(-1);
//            }
            throw new RuntimeException("total number reached");
        } finally {
            globalState.getConnection().close();
        }
    }

    public void generateDatabase(G globalState) throws Exception{
        EnumerationGraphGenerator<G,S> generator = new EnumerationGraphGenerator<>(globalState, graphSeq);
        List<CypherQueryAdapter> queries = generator.createGraph(globalState);

        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
            globalState.getState().logCreateStatement(query);
        }

        graphSeq.finish();
        graphSeq.randomize();
    }
}
