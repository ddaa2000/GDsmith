package org.example.gdsmith.cypher.gen;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.IIdentifierBuilder;
import org.example.gdsmith.cypher.schema.ILabelInfo;
import org.example.gdsmith.cypher.standard_ast.Label;
import org.example.gdsmith.cypher.standard_ast.Pattern;
import org.example.gdsmith.cypher.standard_ast.RelationType;
import org.example.gdsmith.cypher.ast.Direction;
import org.example.gdsmith.cypher.ast.INodeIdentifier;
import org.example.gdsmith.cypher.ast.IPattern;
import org.example.gdsmith.cypher.ast.IRelationIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Subgraph {

    private List<AbstractNode> nodes = new ArrayList<>();
    private List<AbstractRelationship> relationships = new ArrayList<>();

    public List<AbstractNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<AbstractNode> nodes) {
        this.nodes = nodes;
    }

    public List<AbstractRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<AbstractRelationship> relationships) {
        this.relationships = relationships;
    }

    public void addNode(AbstractNode node){
        nodes.add(node);
    }

    public void addRelationship(AbstractRelationship relationship){
        relationships.add(relationship);
    }

    public IPattern translateMatch(IIdentifierBuilder identifierBuilder, Map<AbstractNode, INodeIdentifier> nodeToString,
                                   Map<AbstractRelationship, IRelationIdentifier> relationToString){
        Pattern.PatternBuilder patternBuilder = new Pattern.PatternBuilder(identifierBuilder);
        Pattern.PatternBuilder.OngoingRelation lastRelationship = null;

        List<AbstractNode> abstractNodes = new ArrayList<>();
        List<AbstractRelationship> abstractRelations = new ArrayList<>();


        for(int i = 0; i < nodes.size(); i++){
            Pattern.PatternBuilder.OngoingNode ongoingNode = null;
            if(nodeToString.containsKey(nodes.get(i))){
                if(lastRelationship != null){
                    ongoingNode = lastRelationship.newNodeRef(nodeToString.get(nodes.get(i)));
                }
                else {
                    ongoingNode = patternBuilder.newRefDefinedNode(nodeToString.get(nodes.get(i)));
                }
            }
            else{
                if(lastRelationship != null){
                    ongoingNode = lastRelationship.newNamedNode();
                }
                else {
                    ongoingNode = patternBuilder.newNamedNode();
                }
                for(ILabelInfo labelInfo : nodes.get(i).getLabelInfos()){
                    ongoingNode.withLabels(new Label(labelInfo.getName()));
                }
            }
            abstractNodes.add(nodes.get(i));

            if(i == nodes.size() - 1){
                IPattern pattern = ongoingNode.build();
                resolveAndAddMap(pattern, abstractNodes, abstractRelations, nodeToString, relationToString);
                return pattern;
            }

            Direction direction;
            if(relationships.get(i).getFrom() == nodes.get(i)){
                direction =  Direction.RIGHT;
            }
            else {
                direction = Direction.LEFT;
            }
            Pattern.PatternBuilder.OngoingRelation relation = null;

            if(relationToString.containsKey(relationships.get(i))){
                relation = ongoingNode.newAnonymousRelation().withDirection(direction);
            }
            else {
                relation = ongoingNode.newNamedRelation().withDirection(direction);
                if(relationships.get(i).getType() != null){
                    relation.withType(new RelationType(relationships.get(i).getType().getName()));
                }
            }
            abstractRelations.add(relationships.get(i));


            lastRelationship = relation;
        }
        throw new RuntimeException();
    }

    public IPattern translateMerge(IIdentifierBuilder identifierBuilder, Map<AbstractNode, INodeIdentifier> nodeToString,
                                   Map<AbstractRelationship, IRelationIdentifier> relationToString){
        Pattern.PatternBuilder patternBuilder = new Pattern.PatternBuilder(identifierBuilder);
        Pattern.PatternBuilder.OngoingRelation lastRelationship = null;

        List<AbstractNode> abstractNodes = new ArrayList<>();
        List<AbstractRelationship> abstractRelations = new ArrayList<>();

        //we assume that there is only one node which is not in the nodeToString
        for(int i = 0; i < nodes.size(); i++){
            Pattern.PatternBuilder.OngoingNode ongoingNode = null;

            if(i != nodes.size() - 1){
                if(relationToString.containsKey(relationships.get(i))){
                    continue;
                }
            }

            if(nodeToString.containsKey(nodes.get(i))){
                if(lastRelationship != null){
                    ongoingNode = lastRelationship.newNodeRef(nodeToString.get(nodes.get(i)));
                }
                else {
                    ongoingNode = patternBuilder.newRefDefinedNode(nodeToString.get(nodes.get(i)));
                }
            }
            else{
                if(lastRelationship != null){
                    ongoingNode = lastRelationship.newNamedNode();
                }
                else {
                    ongoingNode = patternBuilder.newNamedNode();
                }
                for(ILabelInfo labelInfo : nodes.get(i).getLabelInfos()){
                    ongoingNode.withLabels(new Label(labelInfo.getName()));
                }
            }

            abstractNodes.add(nodes.get(i));

            if(i == nodes.size() - 1){
                IPattern pattern = ongoingNode.build();
                resolveAndAddMap(pattern, abstractNodes, abstractRelations, nodeToString, relationToString);
                return pattern;
            }

            Direction direction;
            if(relationships.get(i).getFrom() == nodes.get(i)){
                direction =  Direction.RIGHT;
            }
            else {
                direction = Direction.LEFT;
            }
            Pattern.PatternBuilder.OngoingRelation relation = null;

            if(relationToString.containsKey(relationships.get(i))){
                relation = ongoingNode.newAnonymousRelation();
            }
            else {
                relation = ongoingNode.newNamedRelation().withDirection(direction);
                if(relationships.get(i).getType() != null){
                    relation.withType(new RelationType(relationships.get(i).getType().getName()));
                }
            }

            abstractRelations.add(relationships.get(i));


            lastRelationship = relation;
        }
//        for(int i = 0; i < nodes.size(); i++){
//            Pattern.PatternBuilder.OngoingNode ongoingNode = null;
//            if(nodeToString.containsKey(nodes.get(i))){
//                if(i != nodes.size() - 1 && nodeToString.containsKey(nodes.get(i + 1))){
//                    if(i == 0 || nodeToString.containsKey(nodes.get(i - 1))){
//                        continue;
//                    }
//                }
//                if(lastRelationship != null){
//                    ongoingNode = lastRelationship.newNodeRef(nodeToString.get(nodes.get(i)));
//                }
//                else {
//                    ongoingNode = patternBuilder.newRefDefinedNode(nodeToString.get(nodes.get(i)));
//                }
//            }
//            else{
//                if(lastRelationship != null){
//                    ongoingNode = lastRelationship.newNamedNode();
//                }
//                else {
//                    ongoingNode = patternBuilder.newNamedNode();
//                }
//                for(ILabelInfo labelInfo : nodes.get(i).getLabelInfos()){
//                    ongoingNode.withLabels(new Label(labelInfo.getName()));
//                }
//            }
//
//            if(i == nodes.size() - 1){
//                return ongoingNode.build();
//            }
//            if(i != 0 && !nodeToString.containsKey(nodes.get(i - 1))){
//                return ongoingNode.build();
//            }
//
//            Direction direction;
//            if(relationships.get(i).getFrom() == nodes.get(i)){
//                direction =  Direction.RIGHT;
//            }
//            else {
//                direction = Direction.LEFT;
//            }
//            Pattern.PatternBuilder.OngoingRelation relation = null;
//
//            if(relationToString.containsKey(relationships.get(i))){
//                relation = ongoingNode.newRelationRef(relationToString.get(relationships.get(i))).withDirection(direction);
//            }
//            else {
//                relation = ongoingNode.newNamedRelation().withDirection(direction);
//                if(relationships.get(i).getType() != null){
//                    relation.withType(new RelationType(relationships.get(i).getType().getName()));
//                }
//            }
//
//
//            lastRelationship = relation;
//        }
        throw new RuntimeException();
    }

    private static void resolveAndAddMap(IPattern pattern, List<AbstractNode> nodes, List<AbstractRelationship> relationships,Map<AbstractNode, INodeIdentifier> nodeToString,
                                        Map<AbstractRelationship, IRelationIdentifier> relationToString){
//        StringBuilder sb = new StringBuilder();
//        pattern.toTextRepresentation(sb);
//        sb.append("\n");
//        for(int i = 0; i < nodes.size(); i++){
//            sb.append(nodes.get(i).)
//        }
        for(int i = 0; i < nodes.size(); i++){
            if(!nodeToString.containsKey(nodes.get(i))){
                nodeToString.put(nodes.get(i), (INodeIdentifier) pattern.getPatternElements().get(i * 2));
            }

        }
        for(int i = 0; i < relationships.size(); i++){
            if(!relationToString.containsKey(relationships.get(i))){
                relationToString.put(relationships.get(i), (IRelationIdentifier) pattern.getPatternElements().get(i * 2 + 1));
            }

        }
    }

    public IPattern translateCreate(IIdentifierBuilder identifierBuilder){
        Pattern.PatternBuilder patternBuilder = new Pattern.PatternBuilder(identifierBuilder);
        Pattern.PatternBuilder.OngoingRelation lastRelationship = null;
        for(int i = 0; i < nodes.size(); i++){
            Pattern.PatternBuilder.OngoingNode ongoingNode = null;
            if(lastRelationship != null){
                ongoingNode = lastRelationship.newNamedNode();
            }
            else {
                ongoingNode = patternBuilder.newNamedNode();
            }
            for(ILabelInfo labelInfo : nodes.get(i).getLabelInfos()){
                ongoingNode.withLabels(new Label(labelInfo.getName()));
            }
            if(i == nodes.size() - 1){
                return ongoingNode.build();
            }

            Direction direction;
            if(relationships.get(i).getFrom() == nodes.get(i)){
                direction =  Direction.RIGHT;
            }
            else {
                direction = Direction.LEFT;
            }
            Pattern.PatternBuilder.OngoingRelation relation = ongoingNode.newNamedRelation().withDirection(direction);
            if(relationships.get(i).getType() != null){
                relation.withType(new RelationType(relationships.get(i).getType().getName()));
            }
            lastRelationship = relation;
        }
        throw new RuntimeException();
    }

    private List<List<Integer>> idLists = new ArrayList<>();

    public void putInstance(List<Integer> ids){
        idLists.add(ids);
    }

}
