package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.IIdentifierBuilder;
import org.example.gdsmith.cypher.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Pattern implements IPattern {
    private List<IPatternElement> patternElements;

    Pattern(List<IPatternElement> pattern){
        this.patternElements = pattern;
    }

    @Override
    public List<IPatternElement> getPatternElements() {
        return patternElements;
    }

    @Override
    public void setPatternElements(List<IPatternElement> elements) {
        patternElements = elements;
    }

    @Override
    public IPattern getCopy() {
        if(patternElements != null){
            return new Pattern(patternElements.stream().map(p->p.getCopy()).collect(Collectors.toList()));
        }
        return new Pattern(new ArrayList<>());

    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        if(patternElements != null){
            for(IPatternElement element : patternElements){
                element.toTextRepresentation(sb);
            }
        }
    }

    public static class  PatternBuilder {
        private final IIdentifierBuilder identifierBuilder;
        private final List<IPatternElement> patternElements;

        public PatternBuilder(IIdentifierBuilder identifierBuilder){
            this.identifierBuilder = identifierBuilder;
            this.patternElements = new ArrayList<>();
        }

        public static class OngoingNode {
            private PatternBuilder patternBuilder;
            private NodeIdentifier.NodeBuilder nodeBuilder;

            private OngoingNode(PatternBuilder patternBuilder, String name){
                this.patternBuilder = patternBuilder;
                nodeBuilder = NodeIdentifier.NodeBuilder.newNodeBuilder(name);
            }

            public OngoingNode withLabels(ILabel...labels){
                nodeBuilder.withLabels(labels);
                return this;
            }

            public OngoingNode withProperties(IProperty...properties){
                nodeBuilder.withProperties(properties);
                return this;
            }

            public OngoingRelation newNamedRelation(){
                INodeIdentifier node = nodeBuilder.build();
                patternBuilder.patternElements.add(node);
                OngoingRelation ongoingRelation = new OngoingRelation(patternBuilder, patternBuilder.identifierBuilder.getNewRelationName());
                return ongoingRelation;
            }

            public OngoingRelation newNamedRelation(IRelationIdentifier relationIdentifier){
                INodeIdentifier node = nodeBuilder.build();
                patternBuilder.patternElements.add(node);
                OngoingRelation ongoingRelation = new OngoingRelation(patternBuilder, relationIdentifier.getName());
                ongoingRelation.withDirection(relationIdentifier.getDirection());
                ongoingRelation.withProperties(relationIdentifier.getProperties().
                        toArray(new IProperty[relationIdentifier.getProperties().size()]));
                ongoingRelation.withType(relationIdentifier.getTypes().get(0));
                return ongoingRelation;
            }

            public OngoingRelation newAnonymousRelation(){
                INodeIdentifier node = nodeBuilder.build();
                patternBuilder.patternElements.add(node);
                OngoingRelation ongoingRelation = new OngoingRelation(patternBuilder, "");
                return ongoingRelation;
            }


            public OngoingRelation newRelationRef(IRelationIdentifier relation){
                INodeIdentifier node = nodeBuilder.build();
                patternBuilder.patternElements.add(node);
                OngoingRelation ongoingRelation = new OngoingRelation(patternBuilder, relation.getName());
                return ongoingRelation;
            }

            public IPattern build(){
                INodeIdentifier node = nodeBuilder.build();
                patternBuilder.patternElements.add(node);
                return new Pattern(patternBuilder.patternElements);
            }
        }

        public static class OngoingRelation {
            private PatternBuilder patternBuilder;
            private RelationIdentifier.RelationBuilder relationBuilder;

            private OngoingRelation(PatternBuilder patternBuilder, String name){
                this.patternBuilder = patternBuilder;
                relationBuilder = RelationIdentifier.RelationBuilder.newRelationBuilder(name);
            }

            public OngoingRelation withType(IType relationType){
                relationBuilder.withType(relationType);
                return this;
            }

            public OngoingRelation withDirection(Direction direction){
                relationBuilder.withDirection(direction);
                return this;
            }

            public OngoingRelation withProperties(IProperty ...properties){
                relationBuilder.withProperties(properties);
                return this;
            }

            public OngoingRelation withOnlyLengthUpperBound(int lengthUpperBound){
                relationBuilder.withOnlyLengthUpperBound(lengthUpperBound);
                return this;
            }

            public OngoingRelation withOnlyLengthLowerBound(int lengthLowerBound){
                relationBuilder.withOnlyLengthLowerBound(lengthLowerBound);
                return this;
            }

            public OngoingRelation withLength(int length){
                relationBuilder.withLength(length);
                return this;
            }

            public OngoingRelation withLengthUnbounded(){
                relationBuilder.withLengthUnbounded();
                return this;
            }

            public OngoingNode newNamedNode(){
                IRelationIdentifier relation = relationBuilder.build();
                patternBuilder.patternElements.add(relation);
                return patternBuilder.newNamedNode();
            }

            public OngoingNode newNamedNode(INodeIdentifier nodeIdentifier){
                IRelationIdentifier relation = relationBuilder.build();
                patternBuilder.patternElements.add(relation);
                return patternBuilder.newNamedNode(nodeIdentifier);
            }

            public OngoingNode newAnonymousNode(){
                IRelationIdentifier relation = relationBuilder.build();
                patternBuilder.patternElements.add(relation);
                return patternBuilder.newAnonymousNode();
            }

            public OngoingNode newNodeRef(INodeIdentifier node){
                IRelationIdentifier relation = relationBuilder.build();
                patternBuilder.patternElements.add(relation);
                return patternBuilder.newRefDefinedNode(node);
            }
        }

        public OngoingNode newNamedNode(){
            OngoingNode builder = new OngoingNode(this, identifierBuilder.getNewNodeName());
            return builder;
        }

        public OngoingNode newNamedNode(INodeIdentifier nodeIdentifier){
            OngoingNode builder = new OngoingNode(this, identifierBuilder.getNewNodeName());
            builder.withProperties(nodeIdentifier.getProperties().toArray(new IProperty[nodeIdentifier.getProperties().size()]));
            builder.withLabels(nodeIdentifier.getLabels().toArray(new ILabel[nodeIdentifier.getProperties().size()]));
            return builder;
        }

        public OngoingNode newAnonymousNode(){
            OngoingNode builder = new OngoingNode(this, "");
            return builder;
        }

        public OngoingNode newRefDefinedNode(INodeIdentifier node){
            OngoingNode builder = new OngoingNode(this, node.getName());
            return builder;
        }
    }
}
