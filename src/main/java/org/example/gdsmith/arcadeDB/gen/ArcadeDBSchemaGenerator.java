package org.example.gdsmith.arcadeDB.gen;

import org.example.gdsmith.cypher.gen.CypherSchemaGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.arcadeDB.ArcadeDBGlobalState;
import org.example.gdsmith.arcadeDB.ArcadeDBSchema;

import java.util.ArrayList;
import java.util.List;

public class ArcadeDBSchemaGenerator extends CypherSchemaGenerator<ArcadeDBSchema, ArcadeDBGlobalState> {


    public ArcadeDBSchemaGenerator(ArcadeDBGlobalState globalState){
        super(globalState);
    }

    @Override
    public ArcadeDBSchema generateSchemaObject(ArcadeDBGlobalState globalState, List<CypherSchema.CypherLabelInfo> labels, List<CypherSchema.CypherRelationTypeInfo> relationTypes, List<CypherSchema.CypherPatternInfo> patternInfos) {
        /*for (CypherSchema.CypherLabelInfo label: labels) {
            String createVertex = "CREATE VLABEL ";
            createVertex += label.getName();
            //System.out.println(createVertex);
            try {
                globalState.executeStatement(new CypherQueryAdapter(createVertex));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (CypherSchema.CypherRelationTypeInfo type: relationTypes) {
            String createEdge = "CREATE ELABEL ";
            createEdge += type.getName();
            try {
                globalState.executeStatement(new CypherQueryAdapter(createEdge));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Randomly r = new Randomly();
        int numOfIndexes = r.getInteger(5, 8);

        for (int i = 0; i < numOfIndexes; i++) {
            String createIndex = "CREATE PROPERTY INDEX ON ";
            if (Randomly.getBoolean()) {
                CypherSchema.CypherLabelInfo n = labels.get(r.getInteger(0, labels.size() - 1));
                createIndex = createIndex + n.getName() + " (";
                IPropertyInfo p = n.getProperties().get(r.getInteger(0, n.getProperties().size() - 1));
                createIndex = createIndex + p.getKey() + ")";
            } else {
                CypherSchema.CypherRelationTypeInfo re = relationTypes.get(r.getInteger(0, relationTypes.size() - 1));
                createIndex = createIndex + re.getName() + " (";
                IPropertyInfo p = re.getProperties().get(r.getInteger(0, re.getProperties().size() - 1));
                createIndex = createIndex + p.getKey() + ")";
            }
            //System.out.println(createIndex);
            try {
                globalState.executeStatement(new CypherQueryAdapter(createIndex));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        return new ArcadeDBSchema(new ArrayList<>(), labels, relationTypes, patternInfos);
    }

}
