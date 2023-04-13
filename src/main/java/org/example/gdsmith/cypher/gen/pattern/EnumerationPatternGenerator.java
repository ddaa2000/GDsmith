package org.example.gdsmith.cypher.gen.pattern;

import org.example.gdsmith.cypher.ast.Direction;
import org.example.gdsmith.cypher.ast.ILabel;
import org.example.gdsmith.cypher.ast.IPattern;
import org.example.gdsmith.cypher.ast.IType;
import org.example.gdsmith.cypher.ast.analyzer.IMatchAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.INodeAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IRelationAnalyzer;
import org.example.gdsmith.cypher.gen.EnumerationSeq;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.Label;
import org.example.gdsmith.cypher.standard_ast.Pattern;
import org.example.gdsmith.cypher.standard_ast.RelationType;
import org.example.gdsmith.cypher.dsl.BasicPatternGenerator;
import org.example.gdsmith.cypher.dsl.IIdentifierBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnumerationPatternGenerator<S extends CypherSchema<?,?>> extends BasicPatternGenerator<S> {

    private static final int MAX_ENTITY_NUM = 5;
    private static final int MAX_LABEL_NUM = 2;
    private EnumerationSeq enumerationSeq;
    private int presentEntityNum = 0;

    public EnumerationPatternGenerator(S schema, IIdentifierBuilder identifierBuilder, EnumerationSeq enumerationSeq) {
        super(schema, identifierBuilder);
        this.enumerationSeq = enumerationSeq;
    }

    private List<ILabel> generateLabels(S schema){
        List<ILabel> labels = schema.getLabels().stream().map(l->new Label(l.getName())).collect(Collectors.toList());
        List<ILabel> results = new ArrayList<>();

        int presentLabelNum = 0;
        while (enumerationSeq.getDecision() && presentLabelNum < MAX_LABEL_NUM){
            ILabel label = labels.get(enumerationSeq.getRange(labels.size()));
            presentLabelNum++;
            results.add(label);
        }

        return results;
    }

    private IType generateRelationType(S schema){
        List<IType> relationTypes = schema.getRelationTypes().stream().map(t->new RelationType(t.getName())).collect(Collectors.toList());
        return enumerationSeq.getElement(relationTypes);
    }


    public IPattern generateSinglePattern(IMatchAnalyzer matchClause, IIdentifierBuilder identifierBuilder, S schema){
        ILabel[] labels = generateLabels(schema).toArray(new ILabel[0]);

        Pattern.PatternBuilder.OngoingNode node;

        List<INodeAnalyzer> idNodes = matchClause.getExtendableNodeIdentifiers();
        if(enumerationSeq.getDecision() && idNodes.size() > 0){
            node = new Pattern.PatternBuilder(identifierBuilder).newRefDefinedNode(enumerationSeq.getElement(idNodes));
        }
        else if(enumerationSeq.getDecision()){
            node = new Pattern.PatternBuilder(identifierBuilder).newNamedNode();
        }
        else{
            node = new Pattern.PatternBuilder(identifierBuilder).newAnonymousNode();
        }
        node = node.withLabels(labels);
        presentEntityNum++;

        while (enumerationSeq.getDecision() && presentEntityNum < MAX_ENTITY_NUM - 2){
            Pattern.PatternBuilder.OngoingRelation relation;
            List<IRelationAnalyzer> relations = matchClause.getExtendableRelationIdentifiers();
            if(enumerationSeq.getDecision() && relations.size() > 0){
                relation = node.newRelationRef(enumerationSeq.getElement(relations));
            }
            else if(enumerationSeq.getDecision()){
                relation = node.newNamedRelation();
            }
            else{
                relation = node.newAnonymousRelation();
            }

            if(enumerationSeq.getDecision()){
                relation.withDirection(Direction.BOTH);
            }
            else if(enumerationSeq.getDecision()){
                relation.withDirection(Direction.LEFT);
            }
            relation.withDirection(Direction.RIGHT);
            if(enumerationSeq.getDecision()){
                relation.withType(generateRelationType(schema));
            }

            idNodes = matchClause.getExtendableNodeIdentifiers();
            if(enumerationSeq.getDecision() && idNodes.size() > 0){
                node = relation.newNodeRef(enumerationSeq.getElement(idNodes));
            }
            else if(enumerationSeq.getDecision()){
                node = relation.newNamedNode();
            }
            else{
                node = relation.newAnonymousNode();
            }
            node = node.withLabels(labels);
            presentEntityNum += 2;
        }

        return node.build();
    }
    @Override
    public List<IPattern> generatePattern(IMatchAnalyzer matchClause, IIdentifierBuilder identifierBuilder, S schema) {
        List<IPattern> patternTuple = new ArrayList<>();
        presentEntityNum--;
        patternTuple.add(generateSinglePattern(matchClause, identifierBuilder, schema));
        if(enumerationSeq.getDecision() && presentEntityNum < MAX_ENTITY_NUM){
            patternTuple.add(generateSinglePattern(matchClause, identifierBuilder, schema));
        }
        return patternTuple;
    }
}
