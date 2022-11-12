package org.example.gdsmith.composite;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.gdsmith.*;
import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.DatabaseProvider;
import org.example.gdsmith.Main;
import org.example.gdsmith.MainOptions;
import org.example.gdsmith.common.log.LoggableFactory;

import org.example.gdsmith.cypher.*;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherLoggableFactory;
import org.example.gdsmith.cypher.CypherProviderAdapter;
import org.example.gdsmith.cypher.CypherQueryAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CompositeProvider extends CypherProviderAdapter<CompositeGlobalState, CompositeSchema, CompositeOptions> {
    public CompositeProvider() {
        super(CompositeGlobalState.class, CompositeOptions.class);
    }

    @Override
    public CypherConnection createDatabase(CompositeGlobalState globalState) throws Exception {
        return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public String getDBMSName() {
        return "composite";
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

        return;
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
            for(String nameWithVersion : databaseNamesWithVersion){
                for(DatabaseProvider provider: Main.getDBMSProviders()){
                    String databaseName = provider.getDBMSName().toLowerCase();
                    MainOptions options = mainOptions;
                    if(nameWithVersion.contains(databaseName)){
                        FileWriter fw = null;
                        try {
                            File f = new File("./logs/gdb_version.txt");
                            fw = new FileWriter(f, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        PrintWriter pw = new PrintWriter(fw);
                        pw.println(nameWithVersion + "\n");
                        pw.flush();
                        try {
                            fw.flush();
                            pw.close();
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DBMSSpecificOptions command = ((CypherProviderAdapter)provider)
                                .generateOptionsFromConfig(jsonObject.getAsJsonObject(nameWithVersion));
                        connections.add(((CypherProviderAdapter)provider).createDatabaseWithOptions(options, command));
                        break;
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
