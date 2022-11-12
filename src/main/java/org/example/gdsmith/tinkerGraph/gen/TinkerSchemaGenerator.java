package org.example.gdsmith.tinkerGraph.gen;

import org.example.gdsmith.cypher.gen.CypherSchemaGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.tinkerGraph.TinkerGlobalState;
import org.example.gdsmith.tinkerGraph.schema.TinkerSchema;

import java.util.ArrayList;
import java.util.List;

public class TinkerSchemaGenerator extends CypherSchemaGenerator<TinkerSchema, TinkerGlobalState> {


    public TinkerSchemaGenerator(TinkerGlobalState globalState){
        super(globalState);
    }

    @Override
    public TinkerSchema generateSchemaObject(TinkerGlobalState globalState, List<CypherSchema.CypherLabelInfo> labels, List<CypherSchema.CypherRelationTypeInfo> relationTypes, List<CypherSchema.CypherPatternInfo> patternInfos) {
        /*Randomly r = new Randomly();
        int numOfIndexes = r.getInteger(5, 8);

        for (int i = 0; i < numOfIndexes; i++) {
            String createIndex = "CREATE INDEX i" + i;
            createIndex += " IF NOT EXISTS FOR (n:";
            if (Randomly.getBoolean()) {
                CypherSchema.CypherLabelInfo n = labels.get(r.getInteger(0, labels.size()));
                createIndex = createIndex + n.getName() + ") ON (n.";
                IPropertyInfo p = n.getProperties().get(r.getInteger(0, n.getProperties().size()));
                createIndex = createIndex + p.getKey() + ")";
            } else {
                CypherSchema.CypherRelationTypeInfo re = relationTypes.get(r.getInteger(0, relationTypes.size()));
                createIndex = createIndex + re.getName() + ") ON (n.";
                IPropertyInfo p = re.getProperties().get(r.getInteger(0, re.getProperties().size()));
                createIndex = createIndex + p.getKey() + ")";
            }
            try {
                globalState.executeStatement(new CypherQueryAdapter(createIndex));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        return new TinkerSchema(new ArrayList<>(), labels, relationTypes, patternInfos);
    }
}
