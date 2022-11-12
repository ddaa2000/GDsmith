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

public class GraphManager {
    private List<AbstractNode> nodes = new ArrayList<>();
    private List<AbstractRelationship> relationships = new ArrayList<>();
    private ICypherSchema schema;
    private MainOptions options;
    private static int maxNodeColor = 3;

    private Map<IPropertyInfo, List<Object>> propertyValues = new HashMap<>();

    private int presentID = 0;


    private int maxNodeNumber = 128;

    private Randomly randomly = new Randomly();

    public GraphManager(ICypherSchema schema, MainOptions options){
        this.schema = schema;
        this.options = options;
        this.maxNodeNumber = options.getMaxNodeNum();
    }

    public List<CypherQueryAdapter> generateCreateGraphQueries(){
        List<CypherQueryAdapter> results = new ArrayList<>();
        int relationshipNum = randomly.getInteger(maxNodeNumber / 2, maxNodeNumber * 2);

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

        for(int i = 0; i < relationshipNum; i++){
            AbstractNode n0 = nodes.get(randomly.getInteger(0, nodes.size()));
            AbstractNode n1 = nodes.get(randomly.getInteger(0, nodes.size()));
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

        //todo: might corrupt the randomly chain
        Collections.shuffle(labels);
        int labelNum = getRandomLabelNum(labels.size());
        List<ILabelInfo> selectedLabels = new ArrayList<>();
        for(int j = 0; j < labelNum; j++){
            selectedLabels.add(labels.get(j));
        }
        node.setLabelInfos(selectedLabels);
        generateProperties(node);

        return node;
    }

    private AbstractRelationship randomColorRelationship(){
        AbstractRelationship relationship = new AbstractRelationship();
        List<IRelationTypeInfo> relationTypeInfos = new ArrayList<>(schema.getRelationshipTypeInfos());
        Collections.shuffle(relationTypeInfos);
        IRelationTypeInfo relationTypeInfo = null;
        if(relationTypeInfos.size() != 0){
            relationTypeInfo = relationTypeInfos.get(0);
        }
        relationship.setType(relationTypeInfo);
        generateProperties(relationship);

        return relationship;
    }

    private void generateProperties(AbstractNode abstractNode){
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

    public List<SubgraphTreeNodeInstance> randomCluster(){
        List<AbstractNode> closureNodes = new ArrayList<>();
        AbstractNode node = getNodes().get(randomly.getInteger(0, getNodes().size()));

        if(node.getRelationships().size() == 0){
            Subgraph subgraph = new Subgraph();
            subgraph.addNode(node);
            SubgraphTreeNode treeNode = new SubgraphTreeNode(subgraph);
            SubgraphTreeNodeInstance instance = new SubgraphTreeNodeInstance();
            instance.setTreeNode(treeNode);
            instance.setIds(new ArrayList<>(Arrays.asList(node.getId())));
            instance.setProperties(new ArrayList<>(Arrays.asList(node.getProperties())));
            return new ArrayList<>(Arrays.asList(instance));
        }

        List<SubgraphTreeNodeInstance> instances = new ArrayList<>();

        closureNodes.add(node);

        AbstractRelationship root = node.getRelationships().get(randomly.getInteger(0, node.getRelationships().size()));
        Set<AbstractRelationship> closureRelationships = new HashSet<>();
        Set<AbstractRelationship> availableRelationships = new HashSet<>();
        availableRelationships.add(root);


        int closureSize = randomly.getInteger(3, 7);

        for(int i = 0; i < closureSize; i++){
            if(availableRelationships.size() == 0){
                break;
            }
            List<AbstractRelationship> availables = availableRelationships.stream().collect(Collectors.toList());
            AbstractRelationship relationship = availables.get(randomly.getInteger(0, availables.size()));
            addToClosure(closureRelationships, availableRelationships, relationship);

            if(randomly.getInteger(0, 100) < 50){
                List<AbstractRelationship> candidates = new ArrayList<>(relationship.getFrom().getRelationships());
                candidates = candidates.stream().filter(r->r != relationship).collect(Collectors.toList());

                if(candidates.size() == 0){
                    instances.add(randomInstance(Arrays.asList(relationship.getFrom(), relationship.getTo()), relationships));
                }
                else{
                    AbstractRelationship other = candidates.get(randomly.getInteger(0, candidates.size()));
                    AbstractNode otherNode = other.getFrom() == relationship.getFrom() ? other.getTo() : other.getFrom();
                    instances.add(randomInstance(Arrays.asList(otherNode, relationship.getFrom(), relationship.getTo()),
                            Arrays.asList(other, relationship)));
                }
            }
            else {
                List<AbstractRelationship> candidates = new ArrayList<>(relationship.getTo().getRelationships());
                candidates = candidates.stream().filter(r->r != relationship).collect(Collectors.toList());

                if(candidates.size() == 0){
                    instances.add(randomInstance(Arrays.asList(relationship.getFrom(), relationship.getTo()), relationships));
                }
                else {
                    AbstractRelationship other = candidates.get(randomly.getInteger(0, candidates.size()));
                    AbstractNode otherNode = other.getFrom() == relationship.getTo() ? other.getTo() : other.getFrom();
                    instances.add(randomInstance(Arrays.asList(relationship.getFrom(), relationship.getTo(), otherNode),
                            Arrays.asList(relationship, other)));
                }
            }
        }

        return instances;
    }

    private SubgraphTreeNodeInstance randomInstance(List<AbstractNode> nodes, List<AbstractRelationship> relationships){
        boolean reverse = randomly.getInteger(0, 100) < 50;
        if(reverse){
            Collections.reverse(nodes);
            Collections.reverse(relationships);
        }

        Subgraph subgraph = new Subgraph();
        SubgraphTreeNode treeNode = new SubgraphTreeNode(subgraph);
        SubgraphTreeNodeInstance instance = new SubgraphTreeNodeInstance();
        instance.setTreeNode(treeNode);
        instance.setIds(new ArrayList<>());
        instance.setProperties(new ArrayList<>());


        for(int i = 0; i < nodes.size(); i++){
            AbstractNode node = nodes.get(i);
            subgraph.addNode(node);
            instance.getIds().add(node.getId());
            instance.getProperties().add(node.getProperties());

            if(i < relationships.size()){
                AbstractRelationship relationship = relationships.get(i);
                subgraph.addRelationship(relationship);
                instance.getIds().add(relationship.getId());
                instance.getProperties().add(relationship.getProperties());
            }
        }

        return instance;
    }

    private void addToClosure(Set<AbstractRelationship> closureRelationships, Set<AbstractRelationship> availableRelationships, AbstractRelationship relationship){
        availableRelationships.remove(relationship);
        closureRelationships.add(relationship);

        List<AbstractRelationship> candidates = new ArrayList<>(relationship.getFrom().getRelationships());
        candidates.addAll(relationship.getTo().getRelationships());
        candidates.forEach(
                r->{
                    if(!closureRelationships.contains(r)){
                        availableRelationships.add(r);
                    }
                }
        );
    }



    private int getRandomLabelNum(int labelsSize){
        int maxNum = 1 << maxNodeColor;
        int randNum = randomly.getInteger(0, maxNum);
        int labelNum = maxNodeColor + 1;
        if(randNum == 0){
            labelNum = 0;
        }
        else {
            while (randNum > 0){
                randNum = randNum >> 1;
                labelNum--;
            }
        }
        return Math.min(options.getLabelNum(), Math.min(labelsSize, labelNum));
    }





}
