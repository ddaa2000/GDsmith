package org.example.gdsmith.memGraph.gen;

import org.example.gdsmith.cypher.gen.CypherSchemaGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.memGraph.MemGraphGlobalState;
import org.example.gdsmith.memGraph.MemGraphSchema;

import java.util.ArrayList;
import java.util.List;

public class MemGraphSchemaGenerator extends CypherSchemaGenerator<MemGraphSchema, MemGraphGlobalState> {


    public MemGraphSchemaGenerator(MemGraphGlobalState globalState){
        super(globalState);
    }

    @Override
    public MemGraphSchema generateSchemaObject(MemGraphGlobalState globalState, List<CypherSchema.CypherLabelInfo> labels, List<CypherSchema.CypherRelationTypeInfo> relationTypes, List<CypherSchema.CypherPatternInfo> patternInfos) {
        return new MemGraphSchema(new ArrayList<>(), labels, relationTypes, patternInfos);
    }

}
