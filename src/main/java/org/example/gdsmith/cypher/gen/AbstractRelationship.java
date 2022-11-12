package org.example.gdsmith.cypher.gen;

import org.example.gdsmith.cypher.schema.IRelationTypeInfo;

import java.util.HashMap;
import java.util.Map;

public class AbstractRelationship {
    private IRelationTypeInfo type = null;
    private AbstractNode from;
    private AbstractNode to;

    private int id;
    private Map<String, Object> properties = new HashMap<>();

    public IRelationTypeInfo getType() {
        return type;
    }

    public void setType(IRelationTypeInfo type) {
        this.type = type;
    }

    public AbstractNode getFrom() {
        return from;
    }

    public void setFrom(AbstractNode from) {
        this.from = from;
    }

    public AbstractNode getTo() {
        return to;
    }

    public void setTo(AbstractNode to) {
        this.to = to;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
