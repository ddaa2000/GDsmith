package org.example.gdsmith.cypher.gen.graph;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.Direction;
import org.example.gdsmith.cypher.ast.INodeIdentifier;
import org.example.gdsmith.cypher.ast.IPattern;
import org.example.gdsmith.cypher.dsl.IGraphGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.*;
import org.example.gdsmith.cypher.standard_ast.*;
import org.example.gdsmith.cypher.standard_ast.expr.ConstExpression;
import org.example.gdsmith.cypher.schema.IPropertyInfo;

import java.util.ArrayList;
import java.util.List;

public class RandomGraphGenerator <G extends CypherGlobalState<?,S>, S extends CypherSchema<G,?>> implements IGraphGenerator<G> {
    private static int minNumOfNodes = 200;
    private static int maxNumOfNodes = 200;
    private static double percentOfEdges = 0.001;
    private static List<IPattern> INodesPattern;

    private final G globalState;

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

    public RandomGraphGenerator(G globalState){
        this.globalState = globalState;
    }

//    public static List<CypherQueryAdapter> createGraph(CompositeGlobalState globalState) {
//        return new CompositeGraphGenerator(globalState).generateGraph(globalState.getSchema());
//    }

    public List<CypherQueryAdapter> createGraph(G globalState) throws Exception {
        S schema = globalState.getSchema();
        List<CypherQueryAdapter> queries = new ArrayList<>();
        IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();

        Randomly r = new Randomly();

        // create nodes
        INodesPattern = new ArrayList<>();
        int numOfNodes = r.getInteger(minNumOfNodes, maxNumOfNodes);
        List<CypherSchema.CypherLabelInfo> labels = schema.getLabels();
        for (int i = 0; i < numOfNodes; ++i) {
            Pattern.PatternBuilder.OngoingNode n = new Pattern.PatternBuilder(builder.getIdentifierBuilder()).newNamedNode();
            /*for (CypherSchema.CypherLabelInfo l : labels) {
                if (r.getBooleanWithRatherLowProbability()) { // choose label
                    n = n.withLabels(new Label(l.getName()));
                    for (IPropertyInfo p : l.getProperties()) {
                        if (r.getBooleanWithRatherLowProbability()) { // choose property
                            n = n.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));
                        }
                    }
                }
            }*///todo
            CypherSchema.CypherLabelInfo l = labels.get(r.getInteger(0, labels.size() - 1)); // choose label
            n = n.withLabels(new Label(l.getName()));
            for (IPropertyInfo p : l.getProperties()) {
                if (r.getBooleanWithRatherLowProbability()) { // choose property
                    n = n.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));
                }
            }
            n = n.withProperties(new Property("id", CypherType.NUMBER, new ConstExpression(i)));
            IPattern pattern = n.build();
            INodesPattern.add(pattern);
            ClauseSequence sequence = (ClauseSequence) ClauseSequence.createClauseSequenceBuilder().CreateClause(pattern).build();
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

                        for (IPropertyInfo p : relationType.getProperties()) {
                            if (r.getBooleanWithRatherLowProbability()) { // choose this property
                                rel = rel.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));
                            }
                        }
                        /*IPropertyInfo p = relationType.getProperties().get(r.getInteger(0, relationType.getProperties().size() - 1)); // choose this property
                        rel = rel.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));*/ //todo

                        int dirChoice = r.getInteger(0, 2); // generate direction
                        Direction dir = (dirChoice == 0) ? Direction.LEFT : Direction.RIGHT; // For generate in Neo4j, ALL relationships should be directed.
                        rel = rel.withDirection(dir);

                        IPattern merge = rel.newNodeRef(nodeJ).build();
                        ConstExpression n0 = (ConstExpression) nodeI.getProperties().get(nodeI.getProperties().size() - 1).getVal();
                        ConstExpression n1 = (ConstExpression) nodeJ.getProperties().get(nodeJ.getProperties().size() - 1).getVal();
                        StringBuilder str = new StringBuilder();
                        str.append("MATCH ("+nodeI.getName()+"), ("+nodeJ.getName()+") WHERE "+nodeI.getName()+".id = ");
                        StringBuilder n0v = new StringBuilder();
                        StringBuilder n1v = new StringBuilder();
                        n0.toTextRepresentation(n0v);
                        n1.toTextRepresentation(n1v);
                        str.append(n0v);
                        str.append(" AND "+nodeJ.getName()+".id = ");
                        str.append(n1v);

                        ClauseSequence sequence = (ClauseSequence) ClauseSequence.createClauseSequenceBuilder()
                                .CreateClause(merge).build();
                        StringBuilder sb = new StringBuilder();
                        sequence.toTextRepresentation(sb);
                        str.append(" " + sb);
                        queries.add(new CypherQueryAdapter(str.toString()));
                    }
                }
            }
        }
        //queries.add(new CypherQueryAdapter("MATCH (n) OPTIONAL MATCH (n)-[r]->() RETURN count(n.prop) + count(r.prop)"));
        return queries;
    }
}
