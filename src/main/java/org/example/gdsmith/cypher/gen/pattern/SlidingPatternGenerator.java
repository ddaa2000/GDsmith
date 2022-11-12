package org.example.gdsmith.cypher.gen.pattern;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.INodeIdentifier;
import org.example.gdsmith.cypher.ast.IPattern;
import org.example.gdsmith.cypher.ast.IPatternElement;
import org.example.gdsmith.cypher.ast.IRelationIdentifier;
import org.example.gdsmith.cypher.ast.analyzer.IMatchAnalyzer;
import org.example.gdsmith.cypher.dsl.BasicPatternGenerator;
import org.example.gdsmith.cypher.dsl.IIdentifierBuilder;
import org.example.gdsmith.cypher.gen.*;
import org.example.gdsmith.cypher.gen.*;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.schema.ILabelInfo;
import org.example.gdsmith.cypher.standard_ast.Label;
import org.example.gdsmith.cypher.standard_ast.RelationType;
import org.example.gdsmith.cypher.standard_ast.expr.GraphObjectVal;

import java.util.*;

public class SlidingPatternGenerator <S extends CypherSchema<?,?>> extends BasicPatternGenerator<S> {

    private List<SubgraphTreeNode> selected = new ArrayList<>();
    //    private Map<AbstractNode, INodeIdentifier> nodeMap = new HashMap<>();
//    private Map<AbstractRelationship, IRelationIdentifier> relationMap = new HashMap<>();
//    private List<SubgraphTreeNode> treeNodes;
    private GraphManager graphManager;
    private int presentNum = 0;

    private boolean overrideOld;

    private Randomly randomly = new Randomly();

    private Map<String, Object> varToProperties;

    private class PatternCluster{
        List<SubgraphTreeNodeInstance> treeNodes;
        Map<AbstractNode, INodeIdentifier> nodeMap = new HashMap<>();
        Map<AbstractRelationship, IRelationIdentifier> relationMap = new HashMap<>();

        List<IPattern> recordedClusters = new ArrayList<>();
    }

    private List<PatternCluster> patternClusters = new ArrayList<>();


    public SlidingPatternGenerator(S schema, Map<String, Object> varToProperties, GraphManager graphManager, IIdentifierBuilder identifierBuilder, boolean overrideOld) {
        super(schema, identifierBuilder);
        this.graphManager = graphManager;
//        List<SubgraphTreeNode> leaves = subgraphManager.getTreeNodes().stream().filter(t->t.getDescendants().size()==0).collect(Collectors.toList());
        int num = randomly.getInteger(1, 4);

        for(int i = 0; i < num; i++){
            PatternCluster cluster = new PatternCluster();
            cluster.nodeMap = new HashMap<>();
            cluster.relationMap = new HashMap<>();
//            SubgraphTreeNode leaf = leaves.get(randomly.getInteger(0, leaves.size()));
//            SubgraphTreeNodeInstance leafInstance = leaf.getInstances().get(randomly.getInteger(0, leaf.getInstances().size()));
//            cluster.treeNodes = new ArrayList<>(leafInstance.getAncestors());
//            cluster.treeNodes.add(leafInstance);

            cluster.treeNodes = new ArrayList<>(graphManager.randomCluster());
            if(cluster.treeNodes.size() == 0){
                int z = 0;
            }
            patternClusters.add(cluster);
        }
        this.overrideOld = overrideOld;
        this.varToProperties = varToProperties;
    }

    @Override
    public List<IPattern> generatePattern(IMatchAnalyzer matchClause, IIdentifierBuilder identifierBuilder, S schema) {
        if (matchClause.getPatternTuple().size() > 0 && !overrideOld) {
            return matchClause.getPatternTuple();
        }
        List<IPattern> patterns = new ArrayList<>();
        for(int i = 0; i < randomly.getInteger(1, 4); i++){
            int clusterIndex = randomly.getInteger(0, patternClusters.size());
            PatternCluster patternCluster = patternClusters.get(clusterIndex);
            List<SubgraphTreeNodeInstance> treeNodeInstances = patternCluster.treeNodes;
            Map<AbstractNode, INodeIdentifier> nodeMap = patternCluster.nodeMap;
            Map<AbstractRelationship, IRelationIdentifier> relationMap = patternCluster.relationMap;


            SubgraphTreeNodeInstance treeNodeInstance = treeNodeInstances.get(randomly.getInteger(0, treeNodeInstances.size()));
            Subgraph subgraph = treeNodeInstance.getTreeNode().getSubgraph();
            IPattern pattern = subgraph.translateMatch(identifierBuilder, nodeMap, relationMap);

            reducePattern(treeNodeInstance, pattern, subgraph, nodeMap, relationMap);

            List<IPatternElement> patternElements = pattern.getPatternElements();

            if(patternElements.size() == 5){
                if(patternElements.get(1).isAnonymous() && patternElements.get(3).isAnonymous()){
                    if(i != 0){
                        continue;
                    }
                    else{
                        patterns.add(pattern);
                        continue;
                    }
                }
                if(patternElements.get(1).isAnonymous()){
                    pattern.setPatternElements(pattern.getPatternElements().subList(2, 5));
                }
                else if(patternElements.get(3).isAnonymous()){
                    pattern.setPatternElements(pattern.getPatternElements().subList(0, 3));
                }
            }
            if(patternElements.size() == 3){
                if(patternElements.get(1).isAnonymous()){
                    if(i != 0){
                        continue;
                    }
                    else{
                        patterns.add(pattern);
                        continue;
                    }
                }
                if(patternElements.get(1).isAnonymous()){
                    pattern.setPatternElements(pattern.getPatternElements().subList(2, 5));
                }
            }


            if(pattern.getPatternElements().size() == 5){
                shrinkPattern(pattern);
            }

            patterns.add(pattern);
        }
        return patterns;
    }

    private void shrinkPattern(IPattern pattern){
        int randNum = randomly.getInteger(0, 100);
        IRelationIdentifier relationIdentifier1 = (IRelationIdentifier) pattern.getPatternElements().get(1);
        IRelationIdentifier relationIdentifier2 = (IRelationIdentifier) pattern.getPatternElements().get(3);

        //for redisgraph
        if(relationIdentifier1.isAnonymous() && !relationIdentifier2.isAnonymous()){
            pattern.setPatternElements(pattern.getPatternElements().subList(2, 5));
            return;
        }
        if(!relationIdentifier1.isAnonymous() && relationIdentifier2.isAnonymous()){
            pattern.setPatternElements(pattern.getPatternElements().subList(0, 3));
            return;
        }
        if(relationIdentifier1.isAnonymous() && relationIdentifier2.isAnonymous()){
            int randPos = randomly.getInteger(0, 3);
            pattern.setPatternElements(pattern.getPatternElements().subList(randPos * 2, randPos * 2 + 1));
            return;
        }


        if(randNum < 30){
            if(randNum < 15){
                pattern.setPatternElements(pattern.getPatternElements().subList(2, 5));
            }
            else{
                pattern.setPatternElements(pattern.getPatternElements().subList(0, 3));
            }
        }
        else if(randNum < 45){
            if(randNum < 35){
                pattern.setPatternElements(pattern.getPatternElements().subList(0, 1));
            }
            else if(randNum < 40){
                pattern.setPatternElements(pattern.getPatternElements().subList(2, 3));
            }
            else{
                pattern.setPatternElements(pattern.getPatternElements().subList(4, 5));
            }
        }
    }

    private void editLabels(int id, INodeIdentifier nodeIdentifier, AbstractNode abstractNode){
        while (randomly.getInteger(0, 100) < 20){
            if(nodeIdentifier.getLabels().size() != 0){
                nodeIdentifier.getLabels().remove(0);
            }
        }
        for(ILabelInfo labelInfo : abstractNode.getLabelInfos()){
            if(nodeIdentifier.getLabels().stream().noneMatch(l->l.getName().equals(labelInfo.getName()))){
                if(randomly.getInteger(0, 100) < 30){
                    nodeIdentifier.getLabels().add(new Label(labelInfo.getName()));
                }
            }
        }
    }

    private void recordPropertyInfo(INodeIdentifier nodeIdentifier, AbstractNode abstractNode, Map<String, Object> properties){
        varToProperties.put(nodeIdentifier.getName(), new GraphObjectVal(nodeIdentifier.getName(), properties));
    }

    private void recordPropertyInfo(IRelationIdentifier relationIdentifier, AbstractRelationship abstractRelation, Map<String, Object> properties){
        varToProperties.put(relationIdentifier.getName(), new GraphObjectVal(relationIdentifier.getName(), properties));
    }

    private void editRelationTypes(int id, IRelationIdentifier relationIdentifier, AbstractRelationship abstractRelationship){
//        relationIdentifier.getProperties().add(new Property("id", CypherType.NUMBER, new ConstExpression(id)));
        if(relationIdentifier.getTypes().size() != 0 && randomly.getInteger(0, 100) < 20){
            relationIdentifier.getTypes().remove(0);
        }
        else if(randomly.getInteger(0, 100) < 30){
            relationIdentifier.getTypes().add(new RelationType(abstractRelationship.getType().getName()));
        }
    }

    private void reducePattern(SubgraphTreeNodeInstance instance, IPattern pattern, Subgraph subgraph, Map<AbstractNode, INodeIdentifier> nodeMap, Map<AbstractRelationship, IRelationIdentifier> relationMap){

        int patternSize = pattern.getPatternElements().size();
        int nodeSize = (patternSize + 1) / 2;
        int relationshipSize = (patternSize - 1)/2;

        for(int i = 0; i < nodeSize; i++){
            editLabels(instance.getIds().get(i * 2), (INodeIdentifier) pattern.getPatternElements().get(i * 2), subgraph.getNodes().get(i));
        }
        for(int i = 0; i < relationshipSize; i++){
            editRelationTypes(instance.getIds().get(i * 2 + 1), (IRelationIdentifier) pattern.getPatternElements().get(i * 2 + 1), subgraph.getRelationships().get(i));
        }

        for(int i = 0; i < nodeSize; i++){
            recordPropertyInfo((INodeIdentifier) pattern.getPatternElements().get(i * 2), subgraph.getNodes().get(i), instance.getProperties().get(i * 2));
        }
        for(int i = 0; i < relationshipSize; i++){
            recordPropertyInfo((IRelationIdentifier) pattern.getPatternElements().get(i * 2 + 1), subgraph.getRelationships().get(i), instance.getProperties().get(i * 2 + 1));
        }

    }
}
