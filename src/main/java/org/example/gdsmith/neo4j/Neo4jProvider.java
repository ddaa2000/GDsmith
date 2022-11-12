package org.example.gdsmith.neo4j;

import com.google.gson.JsonObject;
import org.example.gdsmith.*;
import org.example.gdsmith.AbstractAction;
import org.example.gdsmith.MainOptions;
import org.example.gdsmith.common.log.LoggableFactory;

import org.example.gdsmith.cypher.*;
import org.example.gdsmith.cypher.*;
import org.example.gdsmith.cypher.standard_ast.ClauseSequence;
import org.example.gdsmith.neo4j.gen.Neo4jGraphGenerator;
import org.example.gdsmith.neo4j.schema.Neo4jSchema;
import org.example.gdsmith.neo4j.gen.Neo4jNodeGenerator;
import org.neo4j.driver.Driver;

import java.util.List;

public class Neo4jProvider extends CypherProviderAdapter<Neo4jGlobalState, Neo4jSchema, Neo4jOptions> {
    public Neo4jProvider() {
        super(Neo4jGlobalState.class, Neo4jOptions.class);
    }

    @Override
    public Neo4jOptions generateOptionsFromConfig(JsonObject config) {
        return Neo4jOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, Neo4jOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = Neo4jOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = Neo4jOptions.DEFAULT_PORT;
        }

        CypherConnection con;
        if(specificOptions.proxyPort == 0){
            String url = String.format("bolt://%s:%d", host, port);
            Driver driver = Neo4jDriverManager.getDriver(url, username, password);
            con = new Neo4jConnection(driver, specificOptions);
        }
        else{
            con = new Neo4jProxyConnection(specificOptions);
        }
        con.executeStatement("MATCH (n) DETACH DELETE n");
//        con.executeStatement("CALL apoc.schema.assert({}, {})");

        return con;
    }

    enum Action implements AbstractAction<Neo4jGlobalState> {
        CREATE(Neo4jNodeGenerator::createNode);

        private final CypherQueryProvider<Neo4jGlobalState> cypherQueryProvider;

        //SQLQueryProvider是一个接口，返回SQLQueryAdapter
        Action(CypherQueryProvider<Neo4jGlobalState> cypherQueryProvider) {
            this.cypherQueryProvider = cypherQueryProvider;
        }

        @Override
        public CypherQueryAdapter getQuery(Neo4jGlobalState globalState) throws Exception {
            return cypherQueryProvider.getQuery(globalState);
        }
    }

    @Override
    public CypherConnection createDatabase(Neo4jGlobalState globalState) throws Exception {
       return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public String getDBMSName() {
        return "neo4j";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }

    @Override
    protected void checkViewsAreValid(Neo4jGlobalState globalState) {

    }

    @Override
    public void generateDatabase(Neo4jGlobalState globalState) throws Exception {
        List<ClauseSequence> queries = Neo4jGraphGenerator.createGraph(globalState);

        for(ClauseSequence query : queries){
            StringBuilder sb = new StringBuilder();
            query.toTextRepresentation(sb);
            globalState.executeStatement(new CypherQueryAdapter(sb.toString()));
            //System.out.println("original");
        }

        //List<ClauseSequence> mutated = new GraphMutator<Neo4jSchema>(globalState.getSchema()).mutate(queries);


        /*for(int i = 0; i < 10; i++){
            CypherQueryAdapter createNode = Neo4jNodeGenerator.createNode(globalState);
            globalState.executeStatement(createNode);
        }*/
        /*while (globalState.getSchema().getDatabaseTables().size() < Randomly.smallNumber() + 1) { //创建tables
            String tableName = DBMSCommon.createTableName(globalState.getSchema().getDatabaseTables().size());//只是负责命名的final类
            SQLQueryAdapter createTable = MySQLTableGenerator.generate(globalState, tableName);
            globalState.executeStatement(createTable);
        }

        //似乎Action列出了所有的对应数据库的语句，每一个Action对应于mysql/gen中的一个语句
        StatementExecutor<Neo4jGlobalState, MySQLProvider.Action> se = new StatementExecutor<>(globalState, MySQLProvider.Action.values(),
                MySQLProvider::mapActions, (q) -> {
            if (globalState.getSchema().getDatabaseTables().isEmpty()) {
                throw new IgnoreMeException();
            }
        });
        se.executeStatements(); //执行query，相当于随机地改变表的结构并添加行？*/
    }
}
