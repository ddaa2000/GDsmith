package org.example.gdsmith.cypher.algorithm;

import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.IgnoreMeException;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.StateToReproduce;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherProviderAdapter;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.gen.graph.ManualGraphGenerator;
import org.example.gdsmith.cypher.gen.query.ManualQueryGenerator;
import org.example.gdsmith.cypher.oracle.ManualDifferentialOracle;
import org.example.gdsmith.cypher.oracle.ManualPerformanceOracle;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.exceptions.DatabaseCrashException;
import org.example.gdsmith.exceptions.MustRestartDatabaseException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

public class ManualPerformanceAlgorithm <S extends CypherSchema<G,?>, G extends CypherGlobalState<O, S>,
        O extends DBMSSpecificOptions<? extends OracleFactory<G>>, C extends CypherConnection> extends CypherTestingAlgorithm<S,G,O,C>{

    public ManualPerformanceAlgorithm(CypherProviderAdapter<G, S, O> provider) {
        super(provider);
    }

    public static int presentNum = 0;
    public static boolean changed = false;

    @Override
    public void generateAndTestDatabase(G globalState) throws Exception {
        if(!changed){
            presentNum = globalState.getOptions().getManualStarting();
            changed = true;
        }
        try {
            File dir = new File("databases");
            File[] files = dir.listFiles();
            ManualGraphGenerator<G> generator = new ManualGraphGenerator<>();
            ManualQueryGenerator<S,G> queryGenerator = new ManualQueryGenerator<>();

            if(Arrays.stream(files).anyMatch(f->f.getName().equals(""+presentNum))){
                generator.loadFile("databases/" + presentNum + "/graph.txt");
                queryGenerator.loadFile("databases/" + presentNum + "/query.txt");
            } else {
                System.exit(0);
            }


            List<CypherQueryAdapter> queries = generator.createGraph(globalState);
            for(CypherQueryAdapter query : queries){
                globalState.executeStatement(query);
                globalState.getState().logCreateStatement(query);
            }


            globalState.getManager().incrementCreateDatabase(); //原子操作计数

            ManualPerformanceOracle<G, S> oracle = new ManualPerformanceOracle<G,S>(globalState, queryGenerator);
            for(int i = 0; i < queryGenerator.queries.size(); i++){
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
                    assert localState != null;
                    localState.executedWithoutError();
                }
            }
            presentNum++;
//            System.exit(0);
        } finally {
            globalState.getConnection().close();
        }
    }
}
