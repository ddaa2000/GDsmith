package org.example.gdsmith.redisGraph.gen;

import org.example.gdsmith.cypher.gen.CypherSchemaGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.redisGraph.RedisGraphGlobalState;
import org.example.gdsmith.redisGraph.RedisGraphSchema;

import java.util.ArrayList;
import java.util.List;

public class RedisGraphSchemaGenerator extends CypherSchemaGenerator<RedisGraphSchema, RedisGraphGlobalState> {


    public RedisGraphSchemaGenerator(RedisGraphGlobalState globalState){
        super(globalState);
    }

    @Override
    public RedisGraphSchema generateSchemaObject(RedisGraphGlobalState globalState, List<CypherSchema.CypherLabelInfo> labels, List<CypherSchema.CypherRelationTypeInfo> relationTypes, List<CypherSchema.CypherPatternInfo> patternInfos) {
        /*Randomly r = new Randomly();
        int numOfIndexes = r.getInteger(5, 8);

        for (int i = 0; i < numOfIndexes; i++) {
            String createIndex = "CREATE INDEX ON :";
            if (Randomly.getBoolean()) {
                CypherSchema.CypherLabelInfo n = labels.get(r.getInteger(0, labels.size() - 1));
                createIndex = createIndex + n.getName() + "(";
                IPropertyInfo p = n.getProperties().get(r.getInteger(0, n.getProperties().size() - 1));
                createIndex = createIndex + p.getKey() + ")";
            } else {
                CypherSchema.CypherRelationTypeInfo re = relationTypes.get(r.getInteger(0, relationTypes.size() - 1));
                createIndex = createIndex + re.getName() + "(";
                IPropertyInfo p = re.getProperties().get(r.getInteger(0, re.getProperties().size() - 1));
                createIndex = createIndex + p.getKey() + ")";
            }
            System.out.println(createIndex);
            try {
                globalState.executeStatement(new CypherQueryAdapter(createIndex));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        return new RedisGraphSchema(new ArrayList<>(), labels, relationTypes, patternInfos);
    }

}
