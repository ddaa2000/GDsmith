package org.example.gdsmith.cypher.gen;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.schema.IPatternElementInfo;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.cypher.standard_ast.CypherType;

import java.util.ArrayList;
import java.util.List;

public abstract class CypherSchemaGenerator <S extends CypherSchema<G,?>, G extends CypherGlobalState<?, S>>{

    protected G globalState;
    protected List<CypherSchema.CypherLabelInfo> labels = new ArrayList<>();
    protected List<CypherSchema.CypherRelationTypeInfo> relationTypes = new ArrayList<>();
    protected List<CypherSchema.CypherPatternInfo> patternInfos = new ArrayList<>();

    public CypherSchemaGenerator(G globalState){
        this.globalState = globalState;
    }

    public S generateSchema(){
        Randomly r = new Randomly();


        int numOfLabels = r.getInteger(5,8);
        int numOfRelationTypes = r.getInteger(5, 8);
        int numOfPatternInfos = r.getInteger(5, 8);
        int indexOfProperty = 0;

        for (int i = 0; i < numOfLabels; i++) {
            int numOfProperties = r.getInteger(5, 8);
            List<IPropertyInfo> properties = new ArrayList<>();
            for (int j = 0; j < numOfProperties; j++) {
                String key = "k" + indexOfProperty;
                CypherType type = Randomly.fromOptions(CypherType.NUMBER, CypherType.STRING, CypherType.BOOLEAN);
                boolean isOptional = Randomly.getBoolean();
                CypherSchema.CypherPropertyInfo p = new  CypherSchema.CypherPropertyInfo(key, type, isOptional);
                properties.add(p);
                indexOfProperty++;
            }
            String name = "L" + i;
            CypherSchema.CypherLabelInfo t = new CypherSchema.CypherLabelInfo(name, properties);
            labels.add(t);
        }

        for (int i = 0; i < numOfRelationTypes; i++) {
            int numOfProperties = r.getInteger(5, 8);
            List<IPropertyInfo> properties = new ArrayList<>();
            for (int j = 0; j < numOfProperties; j++) {
                String key = "k" + indexOfProperty;
                CypherType type = Randomly.fromOptions(CypherType.NUMBER, CypherType.STRING, CypherType.BOOLEAN);
                boolean isOptional = Randomly.getBoolean();
                CypherSchema.CypherPropertyInfo p = new CypherSchema.CypherPropertyInfo(key, type, isOptional);
                properties.add(p);
                indexOfProperty++;
            }
            String name = "T" + i;
            CypherSchema.CypherRelationTypeInfo re = new CypherSchema.CypherRelationTypeInfo(name, properties);
            relationTypes.add(re);
        }

        for (int i = 0; i < numOfPatternInfos; i++) {
            List<IPatternElementInfo> patternElementInfos = new ArrayList<>();

            int index = r.getInteger(0, numOfLabels - 1);
            CypherSchema.CypherLabelInfo tLeft = labels.get(index);
            index = r.getInteger(0, numOfRelationTypes - 1);
            CypherSchema.CypherRelationTypeInfo re = relationTypes.get(index);
            index = r.getInteger(0, numOfLabels - 1);
            CypherSchema.CypherLabelInfo tRight = labels.get(index);

            patternElementInfos.add(tLeft);
            patternElementInfos.add(re);
            patternElementInfos.add(tRight);

            CypherSchema.CypherPatternInfo pi = new CypherSchema.CypherPatternInfo(patternElementInfos);
            patternInfos.add(pi);
        }

        return generateSchemaObject(globalState, labels, relationTypes, patternInfos);
    }

    public abstract S generateSchemaObject(G globalState,
            List<CypherSchema.CypherLabelInfo> labels,
            List<CypherSchema.CypherRelationTypeInfo> relationTypes,
            List<CypherSchema.CypherPatternInfo> patternInfos);
}
