package org.example.gdsmith.janusGraph.gen;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.Direction;
import org.example.gdsmith.cypher.ast.INodeIdentifier;
import org.example.gdsmith.cypher.ast.IPattern;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.cypher.standard_ast.*;
import org.example.gdsmith.cypher.standard_ast.expr.ConstExpression;
import org.example.gdsmith.janusGraph.JanusGlobalState;
import org.example.gdsmith.janusGraph.schema.JanusSchema;
import org.example.gdsmith.cypher.standard_ast.*;

import java.util.ArrayList;
import java.util.List;

public class JanusGraphGenerator {
    private static int minNumOfNodes = 5;
    private static int maxNumOfNodes = 10;
    private static double percentOfEdges = 0.001;
    private static List<IPattern> INodesPattern;

    private final JanusGlobalState globalState;

    // todo(rly): handle Exception
    private ConstExpression generatePropertyValue(Randomly r, CypherType type) throws Exception {
        switch (type){
            case NUMBER: return new ConstExpression(r.getInteger());
            case STRING: return new ConstExpression(r.getString());
            case BOOLEAN: return new ConstExpression(r.getInteger(0, 2) == 0);
            default:
                throw new Exception("undefined type in generator!");
        }
    }

    public JanusGraphGenerator(JanusGlobalState globalState){
        this.globalState = globalState;
    }

    public static List<CypherQueryAdapter> createGraph(JanusGlobalState globalState) throws Exception {
        return new JanusGraphGenerator(globalState).generateGraph(globalState.getSchema());
    }

    public List<CypherQueryAdapter> generateGraph(JanusSchema schema) throws Exception {
        List<CypherQueryAdapter> queries = new ArrayList<>();
        IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();

        Randomly r = new Randomly();

        // create nodes
        INodesPattern = new ArrayList<>();
        int numOfNodes = r.getInteger(minNumOfNodes, maxNumOfNodes);
        List<CypherSchema.CypherLabelInfo> labels = schema.getLabels();
        for (int i = 0; i < numOfNodes; ++i) {
            Pattern.PatternBuilder.OngoingNode n = new Pattern.PatternBuilder(builder.getIdentifierBuilder()).newNamedNode();
            CypherSchema.CypherLabelInfo l = labels.get(r.getInteger(0, labels.size() - 1)); // choose label

            n = n.withLabels(new Label(l.getName()));
            for (IPropertyInfo p : l.getProperties()) {
                if (r.getBooleanWithRatherLowProbability()) { // choose property
                    n = n.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));
                }
            }

            IPattern pattern = n.build();
            INodesPattern.add(pattern);
            ClauseSequence sequence = (ClauseSequence) ClauseSequence.createClauseSequenceBuilder().CreateClause(pattern).ReturnClause(Ret.createStar()).build();
            StringBuilder sb = new StringBuilder();
            sequence.toTextRepresentation(sb);
            queries.add(new CypherQueryAdapter(sb.toString()));
        }

        // create relations
        List<CypherSchema.CypherRelationTypeInfo> relationTypes = schema.getRelationTypes();
        for (int i = 0; i < numOfNodes; ++i) {
            for (int j = 0; j < numOfNodes; ++j) {
                for (CypherSchema.CypherRelationTypeInfo relationType : relationTypes) {
                    if (r.getInteger(0, 1000000) < percentOfEdges * 1000000) { // choose this type
                        IPattern patternI = INodesPattern.get(i);
                        IPattern patternJ = INodesPattern.get(j);
                        INodeIdentifier nodeI = (INodeIdentifier) patternI.getPatternElements().get(0);
                        INodeIdentifier nodeJ = (INodeIdentifier) patternJ.getPatternElements().get(0);

                        Pattern.PatternBuilder.OngoingRelation rel = new Pattern.PatternBuilder(builder.getIdentifierBuilder())
                                .newRefDefinedNode(nodeI)
                                .newNamedRelation().withType(new RelationType(relationType.getName()));

                        IPropertyInfo p = relationType.getProperties().get(r.getInteger(0, relationType.getProperties().size() - 1)); // choose this property

                        rel = rel.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));

                        int dirChoice = r.getInteger(0, 2); // generate direction
                        Direction dir = (dirChoice == 0) ? Direction.LEFT : Direction.RIGHT; // For generate in AgensGraph, ALL relationships should be directed.
                        rel = rel.withDirection(dir);

                        IPattern merge = rel.newNodeRef(nodeJ).build();

                        ClauseSequence sequence = (ClauseSequence) ClauseSequence.createClauseSequenceBuilder()
                                .MatchClause(null, patternI, patternJ).MergeClause(merge).ReturnClause(Ret.createStar()).build();
                        StringBuilder sb = new StringBuilder();
                        sequence.toTextRepresentation(sb);
                        queries.add(new CypherQueryAdapter(sb.toString()));
                    }
                }
            }
        }

        return queries;
    }
}
