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
import org.example.gdsmith.cypher.gen.graph.SlidingGraphGenerator;
import org.example.gdsmith.cypher.gen.query.SlidingQueryGeneratorCompared;
import org.example.gdsmith.cypher.oracle.DifferentialNonEmptyBranchOracle;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.exceptions.DatabaseCrashException;
import org.example.gdsmith.exceptions.MustRestartDatabaseException;
import org.example.gdsmith.cypher.gen.GraphManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class Compared1AlgorithmNew<S extends CypherSchema<G,?>, G extends CypherGlobalState<O, S>,
        O extends DBMSSpecificOptions<? extends OracleFactory<G>>, C extends CypherConnection> extends CypherTestingAlgorithm<S,G,O,C>{
    private GraphManager graphManager;


    public Compared1AlgorithmNew(CypherProviderAdapter<G, S, O> provider) {
        super(provider);
    }

    @Override
    public void generateAndTestDatabase(G globalState) throws Exception {
        try {
            generateDatabase(globalState); //抽象，应该是生成表结构，并往里面插入了初始的数据
            globalState.getManager().incrementCreateDatabase(); //原子操作计数

            File file = new File("coverage_info");
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);


            TestOracle oracle = new DifferentialNonEmptyBranchOracle<G, S>(globalState, new SlidingQueryGeneratorCompared<>(graphManager));
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
//                        executor.getStateToReproduce().exception = reduce.getMessage();
//                        globalState.getLogger().logFileWriter = null;
                        globalState.getLogger().logException(e, globalState.getState());
                    } catch (Exception e){
                        e.printStackTrace();
//                        executor.getStateToReproduce().exception = reduce.getMessage();
//                        globalState.getLogger().logFileWriter = null;
                        globalState.getLogger().logException(e, globalState.getState());
                    }
                    assert localState != null;
                    localState.executedWithoutError();
                }
            }
            throw new RuntimeException("total number reached");
//            System.exit(0);
        } finally {
            globalState.getConnection().close();
        }
    }

    public void generateDatabase(G globalState) throws Exception{
        //use a graph generator to generate guidance information for query generation so that the distribution of queries would be the same with other algorithms
        SlidingGraphGenerator<G,S> generator = new SlidingGraphGenerator<G,S>(globalState);
        this.graphManager = generator.getGraphManager();
        List<CypherQueryAdapter> queries = generator.createGraph(globalState);

        //use another graph generator to generate graph-creating quereis so that the above guidance information will be wrong for the graph and be equivalent with no guidance
        SlidingGraphGenerator<G,S> graphGenerator = new SlidingGraphGenerator<>(globalState);
        queries = graphGenerator.createGraph(globalState);
        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
            globalState.getState().logCreateStatement(query);
        }
    }
}
