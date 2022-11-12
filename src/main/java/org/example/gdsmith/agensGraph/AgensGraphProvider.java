package org.example.gdsmith.agensGraph;

import com.google.gson.JsonObject;
import org.example.gdsmith.*;
import org.example.gdsmith.MainOptions;
import org.example.gdsmith.common.log.LoggableFactory;

import org.example.gdsmith.cypher.*;
import org.example.gdsmith.agensGraph.gen.AgensGraphGraphGenerator;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherLoggableFactory;
import org.example.gdsmith.cypher.CypherProviderAdapter;
import org.example.gdsmith.cypher.CypherQueryAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class AgensGraphProvider extends CypherProviderAdapter<AgensGraphGlobalState, AgensGraphSchema, AgensGraphOptions> {
    public AgensGraphProvider() {
        super(AgensGraphGlobalState.class, AgensGraphOptions.class);
    }

    @Override
    public CypherConnection createDatabase(AgensGraphGlobalState globalState) throws Exception {
        return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public String getDBMSName() {
        return "agensgraph";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }

    @Override
    protected void checkViewsAreValid(AgensGraphGlobalState globalState) {

    }

    @Override
    public void generateDatabase(AgensGraphGlobalState globalState) throws Exception {
        List<CypherQueryAdapter> queries = AgensGraphGraphGenerator.createGraph(globalState);
        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
        }

        /*for(int i = 0; i < 10; i++){
            CypherQueryAdapter createNode = AgensGraphNodeGenerator.createNode(globalState);
            globalState.executeStatement(createNode);
        }*/
        /*while (globalState.getSchema().getDatabaseTables().size() < Randomly.smallNumber() + 1) { //创建tables
            String tableName = DBMSCommon.createTableName(globalState.getSchema().getDatabaseTables().size());//只是负责命名的final类
            SQLQueryAdapter createTable = MySQLTableGenerator.generate(globalState, tableName);
            globalState.executeStatement(createTable);
        }

        //似乎Action列出了所有的对应数据库的语句，每一个Action对应于mysql/gen中的一个语句
        StatementExecutor<AgensGraphGlobalState, MySQLProvider.Action> se = new StatementExecutor<>(globalState, MySQLProvider.Action.values(),
                MySQLProvider::mapActions, (q) -> {
            if (globalState.getSchema().getDatabaseTables().isEmpty()) {
                throw new IgnoreMeException();
            }
        });
        se.executeStatements(); //执行query，相当于随机地改变表的结构并添加行？*/
    }

    @Override
    public AgensGraphOptions generateOptionsFromConfig(JsonObject config) {
        return AgensGraphOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, AgensGraphOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = AgensGraphOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = AgensGraphOptions.DEFAULT_PORT;
        }
        AgensGraphConnection con = null;
        try{
            Class.forName("net.bitnine.agensgraph.Driver");
            String connectionString = "jdbc:agensgraph://"+host+":"+port+"/postgres";
            Connection connection = DriverManager.getConnection(connectionString, username, password);
            con = new AgensGraphConnection(connection);
            con.executeStatement("DROP GRAPH IF EXISTS sqlancer CASCADE");
            con.executeStatement("CREATE GRAPH sqlancer");
            con.executeStatement("SET graph_path = sqlancer");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}
