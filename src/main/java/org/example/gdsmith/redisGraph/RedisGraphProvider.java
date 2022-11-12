package org.example.gdsmith.redisGraph;

import com.google.gson.JsonObject;
import org.example.gdsmith.*;
import org.example.gdsmith.common.log.LoggableFactory;

import org.example.gdsmith.cypher.*;
import org.example.gdsmith.cypher.CypherLoggableFactory;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.redisGraph.gen.RedisGraphGraphGenerator;
import org.example.gdsmith.MainOptions;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherProviderAdapter;
import redis.clients.jedis.JedisPooled;
import java.util.List;

public class RedisGraphProvider extends CypherProviderAdapter<RedisGraphGlobalState, RedisGraphSchema, RedisGraphOptions> {
    public RedisGraphProvider() {
        super(RedisGraphGlobalState.class, RedisGraphOptions.class);
    }

    @Override
    public CypherConnection createDatabase(RedisGraphGlobalState globalState) throws Exception {
        return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public String getDBMSName() {
        return "redisgraph";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }

    @Override
    protected void checkViewsAreValid(RedisGraphGlobalState globalState) {

    }

    @Override
    public void generateDatabase(RedisGraphGlobalState globalState) throws Exception {
        List<CypherQueryAdapter> queries = RedisGraphGraphGenerator.createGraph(globalState);
        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
        }

        /*for(int i = 0; i < 10; i++){
            CypherQueryAdapter createNode = RedisGraphNodeGenerator.createNode(globalState);
            globalState.executeStatement(createNode);
        }*/
        /*while (globalState.getSchema().getDatabaseTables().size() < Randomly.smallNumber() + 1) { //创建tables
            String tableName = DBMSCommon.createTableName(globalState.getSchema().getDatabaseTables().size());//只是负责命名的final类
            SQLQueryAdapter createTable = MySQLTableGenerator.generate(globalState, tableName);
            globalState.executeStatement(createTable);
        }

        //似乎Action列出了所有的对应数据库的语句，每一个Action对应于mysql/gen中的一个语句
        StatementExecutor<RedisGraphGlobalState, MySQLProvider.Action> se = new StatementExecutor<>(globalState, MySQLProvider.Action.values(),
                MySQLProvider::mapActions, (q) -> {
            if (globalState.getSchema().getDatabaseTables().isEmpty()) {
                throw new IgnoreMeException();
            }
        });
        se.executeStatements(); //执行query，相当于随机地改变表的结构并添加行？*/
    }

    @Override
    public RedisGraphOptions generateOptionsFromConfig(JsonObject config) {
        return RedisGraphOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, RedisGraphOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = RedisGraphOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = RedisGraphOptions.DEFAULT_PORT;
        }
        RedisGraphConnection con = null;
        try{
            con = new RedisGraphConnection(specificOptions, new JedisPooled(host, port), "sqlancer");
            con.executeStatement("MATCH (n) DETACH DELETE n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}
