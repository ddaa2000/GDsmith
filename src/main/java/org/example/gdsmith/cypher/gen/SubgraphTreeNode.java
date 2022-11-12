package org.example.gdsmith.cypher.gen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SubgraphTreeNode {
    private Set<SubgraphTreeNode> parents = new HashSet<>();
    private Set<SubgraphTreeNode> children = new HashSet<>();
    private Subgraph subgraph;

    private List<SubgraphTreeNodeInstance> instances = new ArrayList<>();

    public SubgraphTreeNode(Subgraph subgraph){
        this.subgraph = subgraph;
    }

    public void addChild(SubgraphTreeNode child){
        children.add(child);
        child.parents.add(this);
    }

    public void addParent(SubgraphTreeNode parent){
        parent.addChild(this);
    }

    public int getDepth(){
        if(parents.size() == 0){
            return 1;
        }
        List<Integer> orderedDepth = parents.stream().map(p -> p.getDepth() + 1).sorted().collect(Collectors.toList());
        if(parents.size() != 1){
            int z = 90;
        }
        return orderedDepth.get(orderedDepth.size() - 1);
    }

    public Subgraph getSubgraph(){
        return subgraph;
    }

    public Set<SubgraphTreeNode> getParents(){
        return parents;
    }

    public Set<SubgraphTreeNode> getAncestors(){
        Set<SubgraphTreeNode> result = new HashSet<>();
        parents.forEach(p->{
            result.add(p);
            result.addAll(p.getAncestors());
        });

        return result;
    }

    public Set<SubgraphTreeNode> getDescendants(){
        Set<SubgraphTreeNode> result = new HashSet<>();
        children.forEach(c->{
            result.add(c);
            result.addAll(c.getDescendants());
        });

        return result;
    }

    public void addInstance(SubgraphTreeNodeInstance instance){
        instances.add(instance);
        instance.setTreeNode(this);
    }

    public List<SubgraphTreeNodeInstance> getInstances(){
        return instances;
    }

    public void printInfo(){
        for(SubgraphTreeNodeInstance instance : instances){
            instance.printInfo();
        }
    }
}
