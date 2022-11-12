package org.example.gdsmith.cypher.mutation;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.ClauseVisitor;
import org.example.gdsmith.cypher.dsl.IContext;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.schema.IPropertyInfo;

import java.util.List;
import java.util.stream.Collectors;

public class GraphCreatingMutator  <S extends CypherSchema<?,?>> extends ClauseVisitor<GraphCreatingMutator.GraphCreatingContext> implements IClauseMutator  {

    private S schema;
    private List<IPropertyInfo> propertyInfos;
    private IPropertyInfo propertyForDeletion;

    public GraphCreatingMutator(IClauseSequence clauseSequence, S schema, IPropertyInfo propertyForDeletion) {
        super(clauseSequence, new GraphCreatingContext());
        this.schema = schema;
        this.propertyForDeletion = propertyForDeletion;
    }

    @Override
    public void visitMatch(IMatch matchClause, GraphCreatingContext context) {
        if(propertyForDeletion == null){
            return;
        }
        List<IPattern> patterns = matchClause.getPatternTuple();
        patterns.forEach(
            pattern->{
                pattern.getPatternElements().forEach(
                    ele->{
                        if(ele instanceof INodeIdentifier){
                            INodeIdentifier node = (INodeIdentifier) ele;
                            List<IProperty> filteredProperties = node.getProperties().stream()
                                    .filter(p->!p.getKey().equals(propertyForDeletion.getKey())).collect(Collectors.toList());
                            node.setProperties(filteredProperties);
                        }
                        if(ele instanceof IRelationIdentifier){
                            IRelationIdentifier relation = (IRelationIdentifier) ele;
                            List<IProperty> filteredProperties = relation.getProperties().stream()
                                    .filter(p->!p.getKey().equals(propertyForDeletion.getKey())).collect(Collectors.toList());
                            relation.setProperties(filteredProperties);
                        }
                    }
                );
            }
        );
    }

    public void visitMerge(IMerge mergeClause, GraphCreatingContext context){
        if(propertyForDeletion == null){
            return;
        }
        IPattern pattern = mergeClause.getPattern();
        pattern.getPatternElements().forEach(
                ele->{
                    if(ele instanceof INodeIdentifier){
                        INodeIdentifier node = (INodeIdentifier) ele;
                        List<IProperty> filteredProperties = node.getProperties().stream()
                                .filter(p->!p.getKey().equals(propertyForDeletion.getKey())).collect(Collectors.toList());
                        node.setProperties(filteredProperties);
                    }
                    if(ele instanceof IRelationIdentifier){
                        IRelationIdentifier relation = (IRelationIdentifier) ele;
                        List<IProperty> filteredProperties = relation.getProperties().stream()
                                .filter(p->!p.getKey().equals(propertyForDeletion.getKey())).collect(Collectors.toList());
                        relation.setProperties(filteredProperties);
                    }
                }
        );
    }

    @Override
    public void visitCreate(ICreate createClause, GraphCreatingContext context) {
        if(propertyForDeletion == null){
            return;
        }
        IPattern pattern = createClause.getPattern();
        pattern.getPatternElements().forEach(
            ele->{
                if(ele instanceof INodeIdentifier){
                    INodeIdentifier node = (INodeIdentifier) ele;
                    List<IProperty> filteredProperties = node.getProperties().stream()
                            .filter(p->!p.getKey().equals(propertyForDeletion.getKey())).collect(Collectors.toList());
                    node.setProperties(filteredProperties);
                }
                if(ele instanceof IRelationIdentifier){
                    IRelationIdentifier relation = (IRelationIdentifier) ele;
                    List<IProperty> filteredProperties = relation.getProperties().stream()
                            .filter(p->!p.getKey().equals(propertyForDeletion.getKey())).collect(Collectors.toList());
                    relation.setProperties(filteredProperties);
                }
            }
        );
    }

    @Override
    public void visitUnwind(IUnwind unwindClause, GraphCreatingContext context) {

    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class GraphCreatingContext implements IContext {

    }
}
