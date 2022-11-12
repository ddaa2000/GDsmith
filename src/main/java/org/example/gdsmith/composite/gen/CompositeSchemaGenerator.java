package org.example.gdsmith.composite.gen;

import org.example.gdsmith.cypher.gen.CypherSchemaGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.composite.CompositeGlobalState;
import org.example.gdsmith.composite.CompositeSchema;

import java.util.ArrayList;
import java.util.List;

public class CompositeSchemaGenerator extends CypherSchemaGenerator<CompositeSchema, CompositeGlobalState> {


    public CompositeSchemaGenerator(CompositeGlobalState globalState){
        super(globalState);
    }

    @Override
    public CompositeSchema generateSchemaObject(CompositeGlobalState globalState, List<CypherSchema.CypherLabelInfo> labels, List<CypherSchema.CypherRelationTypeInfo> relationTypes, List<CypherSchema.CypherPatternInfo> patternInfos) {
//        List<CypherGlobalState> globalStates = globalState.getGlobalStates();
//        for (CypherGlobalState g: globalStates) {
//            if (g instanceof TuGraphGlobalState) {
//                for(CypherSchema.CypherLabelInfo label : labels){
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("CALL db.createVertexLabel(");
//                    sb.append("'"+label.getName()+"',");
//                    sb.append("'id',");
//                    sb.append("'id', int32, false");
//                    label.getProperties().forEach(p->{
//                        sb.append(",'"+p.getKey()+"'");
//                        switch (p.getType()){
//                            case NUMBER:
//                                sb.append(", int64, true");
//                                break;
//                            case STRING:
//                                sb.append(", string, true");
//                                break;
//                            case BOOLEAN:
//                                sb.append(", bool, true");
//                                break;
//                        }
//                    });
//                    sb.append(")");
//                    try {
//                        globalState.executeStatement(new CypherQueryAdapter(sb.toString()));
//                    } catch (Exception e) {
//                        System.out.println(e.toString());
//                        throw new RuntimeException(e);
//                    }
//                }
//
//                for(CypherSchema.CypherRelationTypeInfo relationType : relationTypes){
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("CALL db.createEdgeLabel(");
//                    sb.append("'"+relationType.getName()+"',");
//                    sb.append("'[]'");
//                    relationType.getProperties().forEach(p->{
//                        sb.append(",'"+p.getKey()+"'");
//                        switch (p.getType()){
//                            case NUMBER:
//                                sb.append(", int64, false");
//                                break;
//                            case STRING:
//                                sb.append(", string, false");
//                                break;
//                            case BOOLEAN:
//                                sb.append(", bool, false");
//                                break;
//                        }
//                    });
//                    sb.append(")");
//                    try {
//                        globalState.executeStatement(new CypherQueryAdapter(sb.toString()));
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            } else if (g instanceof AgensGraphGlobalState) {
//                for (CypherSchema.CypherLabelInfo label: labels) {
//                    String createVertex = "CREATE VLABEL ";
//                    createVertex += label.getName();
//                    try {
//                        globalState.executeStatement(new CypherQueryAdapter(createVertex));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                for (CypherSchema.CypherRelationTypeInfo type: relationTypes) {
//                    String createEdge = "CREATE ELABEL ";
//                    createEdge += type.getName();
//                    try {
//                        globalState.executeStatement(new CypherQueryAdapter(createEdge));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
        /* Randomly r = new Randomly();
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

        return new CompositeSchema(new ArrayList<>(), labels, relationTypes, patternInfos);
    }

}
