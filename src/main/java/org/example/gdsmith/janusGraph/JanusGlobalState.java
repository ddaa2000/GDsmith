package org.example.gdsmith.janusGraph;

import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.janusGraph.gen.JanusSchemaGenerator;
import org.example.gdsmith.janusGraph.schema.JanusSchema;

public class JanusGlobalState extends CypherGlobalState<JanusOptions, org.example.gdsmith.janusGraph.schema.JanusSchema> {

    private JanusSchema JanusSchema = null;

    public JanusGlobalState(){
        super();
        System.out.println("new global state");
    }

    @Override
    protected JanusSchema readSchema() throws Exception {
        if(JanusSchema == null){
            JanusSchema = new JanusSchemaGenerator(this).generateSchema();
        }
        return JanusSchema;
    }
}
