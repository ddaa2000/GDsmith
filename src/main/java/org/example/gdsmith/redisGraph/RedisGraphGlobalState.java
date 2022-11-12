package org.example.gdsmith.redisGraph;

import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.redisGraph.gen.RedisGraphSchemaGenerator;

public class RedisGraphGlobalState extends CypherGlobalState<RedisGraphOptions, RedisGraphSchema> {

    private RedisGraphSchema redisGraphSchema = null;

    public RedisGraphGlobalState(){
        super();
        System.out.println("new global state");
    }

    @Override
    protected RedisGraphSchema readSchema() throws Exception {
        if(redisGraphSchema == null){
            redisGraphSchema = new RedisGraphSchemaGenerator(this).generateSchema();
        }
        return redisGraphSchema;
    }
}
