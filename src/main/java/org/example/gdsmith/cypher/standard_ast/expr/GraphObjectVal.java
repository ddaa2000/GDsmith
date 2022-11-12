package org.example.gdsmith.cypher.standard_ast.expr;

import java.util.Map;

public class GraphObjectVal {
    private String name;
    private Map<String, Object> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public GraphObjectVal(String name, Map<String, Object> properties) {
        this.name = name;
        this.properties = properties;
    }
}
