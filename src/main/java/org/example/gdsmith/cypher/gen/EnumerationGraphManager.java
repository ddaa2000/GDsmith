package org.example.gdsmith.cypher.gen;

import org.example.gdsmith.MainOptions;
import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.schema.ILabelInfo;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.cypher.schema.IRelationTypeInfo;

import java.util.*;
import java.util.stream.Collectors;

public class EnumerationGraphManager {
    private List<AbstractNode> nodes = new ArrayList<>();
    private List<AbstractRelationship> relationships = new ArrayList<>();
    private ICypherSchema schema;
    private MainOptions options;
    private static int maxNodeColor = 3;

    private Map<IPropertyInfo, List<Object>> propertyValues = new HashMap<>();

    private int presentID = 0;

    private EnumerationSeq enumerationSeq;

    private static final int maxNodeNumber = 4;
    private static final int maxRelationNumber = 4;

    public EnumerationGraphManager(ICypherSchema schema, MainOptions options, EnumerationSeq enumerationSeq){
        this.schema = schema;
        this.options = options;
        this.enumerationSeq = enumerationSeq;
    }

    public List<CypherQueryAdapter> generateCreateGraphQueries(){
        List<CypherQueryAdapter> results = new ArrayList<>();


        for(int i = 0; i < maxNodeNumber; i++){
            AbstractNode node = randomColorNode();
            nodes.add(node);
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE ");
            sb.append("(n0 ");
            node.getLabelInfos().forEach(
                    l->{
                        sb.append(":").append(l.getName());
                    }
            );
            printProperties(sb, node.getProperties());
            sb.append(")");
            results.add(new CypherQueryAdapter(sb.toString()));
        }

        for(int i = 0; i < maxRelationNumber; i++){
            AbstractNode n0 = enumerationSeq.getElement(nodes);
            AbstractNode n1 = enumerationSeq.getElement(nodes);
            AbstractRelationship relationship = randomColorRelationship();
            n0.addRelationship(relationship);
            n1.addRelationship(relationship);
            relationship.setFrom(n0);
            relationship.setTo(n1);
            relationships.add(relationship);
            StringBuilder sb = new StringBuilder();
            sb.append("MATCH ");
            sb.append("(n0 {id : ").append(""+n0.getId()).append("})");
            sb.append(", ");
            sb.append("(n1 {id : ").append(""+n1.getId()).append("})");

            sb.append(" MERGE");
            sb.append("(n0)-[r ").append(":").append(relationship.getType().getName());
            printProperties(sb, relationship.getProperties());
            sb.append("]->(n1)");

            results.add(new CypherQueryAdapter(sb.toString()));
        }

        return results;
    }

    public List<AbstractNode> getNodes(){
        return nodes;
    }

    public List<AbstractRelationship> getRelationships(){
        return relationships;
    }

    private void printProperties(StringBuilder sb, Map<String, Object> properties){
        if(properties.size() != 0){
            sb.append("{");
            boolean first = true;
            for(Map.Entry<String, Object> pair : properties.entrySet()){
                if(!first){
                    sb.append(", ");
                }
                first = false;
                sb.append(pair.getKey());
                sb.append(" : ");
                if(pair.getValue() instanceof String){
                    sb.append("\"").append(pair.getValue()).append("\"");
                }
                else if(pair.getValue() instanceof Number){
                    sb.append(pair.getValue());
                }
                else if(pair.getValue() instanceof Boolean){
                    sb.append(pair.getValue());
                }
            }
            sb.append("}");
        }
    }


    private Object generateValue(IPropertyInfo propertyInfo){
        Randomly randomly = new Randomly();
        if(propertyValues.containsKey(propertyInfo)){
            List<Object> values = propertyValues.get(propertyInfo);
            return values.get(randomly.getInteger(0, values.size()));
        }
        else{
            List<Object> values = new ArrayList<>();
            for(int i = 0; i < 20; i++){
                switch (propertyInfo.getType()){
                    case NUMBER:
                        values.add(randomly.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE));
                        break;
                    case BOOLEAN:
                        values.add(randomly.getInteger(0, 2) == 0);
                        break;
                    case STRING:
                        values.add(randomly.getString());
                        break;
                }
            }
            propertyValues.put(propertyInfo, values);
            return values.get(randomly.getInteger(0, values.size()));
        }
    }

    private AbstractNode randomColorNode(){
        List<ILabelInfo> labels = new ArrayList<>(schema.getLabelInfos());
        AbstractNode node = new AbstractNode();

        int labelNum = Math.min(labels.size(), enumerationSeq.getRange(3));
        List<ILabelInfo> selectedLabels = new ArrayList<>();
        for(int j = 0; j < labelNum; j++){
            ILabelInfo selected = enumerationSeq.getElement(labels);
            labels.remove(selected);
            selectedLabels.add(selected);
        }
        node.setLabelInfos(selectedLabels);
        generateProperties(node);

        return node;
    }

    private AbstractRelationship randomColorRelationship(){
        AbstractRelationship relationship = new AbstractRelationship();
        List<IRelationTypeInfo> relationTypeInfos = new ArrayList<>(schema.getRelationshipTypeInfos());
        IRelationTypeInfo relationTypeInfo = null;
        if(relationTypeInfos.size() != 0){
            relationTypeInfo = enumerationSeq.getElement(relationTypeInfos);
        }
        relationship.setType(relationTypeInfo);
        generateProperties(relationship);

        return relationship;
    }

    private void generateProperties(AbstractNode abstractNode){
        Randomly randomly = new Randomly();
        Map<String, Object> result = new HashMap<>();
        result.put("id", presentID);
        abstractNode.setId(presentID);
        presentID++;
        for(ILabelInfo labelInfo : abstractNode.getLabelInfos()){
            for(IPropertyInfo propertyInfo : labelInfo.getProperties()){
                if(randomly.getInteger(0, 100) < 95){
                    result.put(propertyInfo.getKey(), generateValue(propertyInfo));
                }
            }
        }
        abstractNode.setProperties(result);
    }

    private void generateProperties(AbstractRelationship abstractRelationship){
        Randomly randomly = new Randomly();
        Map<String, Object> result = new HashMap<>();
        result.put("id", presentID);
        abstractRelationship.setId(presentID);
        presentID++;
        if(abstractRelationship.getType() != null){
            for(IPropertyInfo propertyInfo : abstractRelationship.getType().getProperties()){
                if(randomly.getInteger(0, 100) < 95){
                    result.put(propertyInfo.getKey(), generateValue(propertyInfo));
                }
            }
        }
        abstractRelationship.setProperties(result);
    }
}
