package org.example.gdsmith.composite;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.gdsmith.*;
import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.DatabaseProvider;
import org.example.gdsmith.Main;
import org.example.gdsmith.MainOptions;
import org.example.gdsmith.common.log.LoggableFactory;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherLoggableFactory;
import org.example.gdsmith.cypher.CypherProviderAdapter;
import org.example.gdsmith.cypher.CypherQueryAdapter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CompositePerformanceProvider extends CypherProviderAdapter<CompositeGlobalState, CompositeSchema, CompositeOptions> {
    public CompositePerformanceProvider() {
        super(CompositeGlobalState.class, CompositeOptions.class);
    }

    @Override
    public CypherConnection createDatabase(CompositeGlobalState globalState) throws Exception {
        return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public void generateAndTestDatabase(CompositeGlobalState globalState) throws Exception {
        try {
            generateDatabase(globalState);
            TestOracle oracle = getTestOracle(globalState);
            oracle.check();
            /*
            for (int i = 0; i < globalState.getOptions().getNrQueries(); i++) {
                try (StateToReproduce.OracleRunReproductionState localState = globalState.getState().createLocalState()) {
                    assert localState != null;
                    try {

                        globalState.getManager().incrementSelectQueryCount();
                    } catch (IgnoreMeException ignored) {
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    localState.executedWithoutError();
                }
            }
            System.exit(0);*/
        } finally {
            globalState.getConnection().close();
        }
    }

    @Override
    public String getDBMSName() {
        return "org/example/gdsmith/performance";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }

    @Override
    protected void checkViewsAreValid(CompositeGlobalState globalState) {

    }

    @Override
    public void generateDatabase(CompositeGlobalState globalState) throws Exception {
        List<CypherQueryAdapter> queries = globalState.getDbmsSpecificOptions().graphGenerator.create(globalState)
                .createGraph(globalState);
        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
        }
    }

    @Override
    public CompositeOptions generateOptionsFromConfig(JsonObject config) {
        return null;
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, CompositeOptions specificOptions) throws Exception {
        List<CypherConnection> connections = new ArrayList<>();
        Gson gson = new Gson();
        try {
            FileReader fileReader = new FileReader(specificOptions.getConfigPath());
            JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
            Set<String> databaseNamesWithVersion = jsonObject.keySet();
            for(DatabaseProvider provider: Main.getDBMSProviders()){
                String databaseName = provider.getDBMSName().toLowerCase();
                MainOptions options = mainOptions;
                for(String nameWithVersion : databaseNamesWithVersion){
                    if(nameWithVersion.contains(provider.getDBMSName().toLowerCase())){
                        DBMSSpecificOptions command = ((CypherProviderAdapter)provider)
                                .generateOptionsFromConfig(jsonObject.getAsJsonObject(nameWithVersion));
                        connections.add(((CypherProviderAdapter)provider).createDatabaseWithOptions(options, command));
                    }
                }

            }
            System.out.println("success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        CompositeConnection compositeConnection = new CompositeConnection(connections, mainOptions);
        return compositeConnection;
    }
}
