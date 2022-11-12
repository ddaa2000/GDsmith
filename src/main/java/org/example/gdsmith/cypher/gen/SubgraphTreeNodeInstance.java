package org.example.gdsmith.cypher.gen;

import org.example.gdsmith.cypher.schema.ILabelInfo;

import java.util.*;

public class SubgraphTreeNodeInstance {
    private Set<SubgraphTreeNodeInstance> parents = new HashSet<>();
    private Set<SubgraphTreeNodeInstance> children = new HashSet<>();
    private SubgraphTreeNode treeNode;

    private List<Integer> ids = new ArrayList<>();
    private List<Map<String, Object>> properties = new ArrayList<>();

    public Set<SubgraphTreeNodeInstance> getParents() {
        return parents;
    }

    public void setParents(Set<SubgraphTreeNodeInstance> parents) {
        this.parents = parents;
    }

    public Set<SubgraphTreeNodeInstance> getChildren() {
        return children;
    }

    public void setChildren(Set<SubgraphTreeNodeInstance> children) {
        this.children = children;
    }

    public SubgraphTreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(SubgraphTreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public void setProperties(List<Map<String, Object>> properties){
        this.properties = properties;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public List<Map<String, Object>> getProperties(){
        return properties;
    }

    public void addChild(SubgraphTreeNodeInstance child){
        children.add(child);
        child.parents.add(this);
    }

    public void addParent(SubgraphTreeNodeInstance parent){
        parent.addChild(this);
    }

    public Set<SubgraphTreeNodeInstance> getAncestors(){
        Set<SubgraphTreeNodeInstance> result = new HashSet<>();
        parents.forEach(p->{
            result.add(p);
            result.addAll(p.getAncestors());
        });

        return result;
    }

    public Set<SubgraphTreeNodeInstance> getDescendants(){
        Set<SubgraphTreeNodeInstance> result = new HashSet<>();
        children.forEach(c->{
            result.add(c);
            result.addAll(c.getDescendants());
        });

        return result;
    }

    public void printInfo(){
        StringBuilder sb = new StringBuilder();
        Subgraph subgraph = treeNode.getSubgraph();
        sb.append("(");
        for(ILabelInfo labelInfo : subgraph.getNodes().get(0).getLabelInfos()){
            sb.append(": "+labelInfo.getName());
        }
        sb.append("{"+getIds().get(0)+"}");
        sb.append(")");

        sb.append("-[");
        if(subgraph.getRelationships().get(0).getType() != null){
            sb.append(": "+subgraph.getRelationships().get(0).getType().getName());
        }
        sb.append("{"+getIds().get(1)+"}");
        sb.append("]-");


        sb.append("(");
        for(ILabelInfo labelInfo : subgraph.getNodes().get(1).getLabelInfos()){
            sb.append(": "+labelInfo.getName());
        }
        sb.append("{"+getIds().get(2)+"}");
        sb.append(")");

        sb.append("-[");
        if(subgraph.getRelationships().get(1).getType() != null){
            sb.append(": "+subgraph.getRelationships().get(1).getType().getName());
        }
        sb.append("{"+getIds().get(3)+"}");
        sb.append("]-");

        sb.append("(");
        for(ILabelInfo labelInfo : subgraph.getNodes().get(2).getLabelInfos()){
            sb.append(": "+labelInfo.getName());
        }
        sb.append("{"+getIds().get(4)+"}");
        sb.append(")");

        System.out.println(sb.toString());
    }
}
