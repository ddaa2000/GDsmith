package org.example.gdsmith.cypher.gen;

import org.example.gdsmith.cypher.ast.ILabel;
import org.example.gdsmith.cypher.schema.ILabelInfo;
import org.example.gdsmith.cypher.standard_ast.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbstractNode {
    private List<ILabelInfo> labelInfos = new ArrayList<>();
    private int id;

    private Map<String, Object> properties = new HashMap<>();

    private List<AbstractRelationship> relationships = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<ILabelInfo> getLabelInfos() {
        return labelInfos;
    }

    public List<ILabel> getLabels(){
        return new ArrayList<>(labelInfos.stream().map(l->new Label(l.getName())).collect(Collectors.toList()));
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setLabelInfos(List<ILabelInfo> labelInfos) {
        this.labelInfos = labelInfos;
    }

    public void addRelationship(AbstractRelationship relationship){
        this.relationships.add(relationship);
    }

    public List<AbstractRelationship> getRelationships(){
        return relationships;
    }
}
