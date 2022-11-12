package org.example.gdsmith.tinkerGraph;

import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.tinkerGraph.gen.TinkerSchemaGenerator;
import org.example.gdsmith.tinkerGraph.schema.TinkerSchema;

public class TinkerGlobalState extends CypherGlobalState<TinkerOptions, org.example.gdsmith.tinkerGraph.schema.TinkerSchema> {

    private TinkerSchema TinkerSchema = null;

    public TinkerGlobalState(){
        super();
        System.out.println("new global state");
    }

    @Override
    protected TinkerSchema readSchema() throws Exception {
        if(TinkerSchema == null){
            TinkerSchema = new TinkerSchemaGenerator(this).generateSchema();
        }
        return TinkerSchema;
    }
}
