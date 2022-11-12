package org.example.gdsmith.composite.gen;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.composite.CompositeGlobalState;
import org.example.gdsmith.composite.CompositeSchema;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IPattern;
import org.example.gdsmith.cypher.dsl.IGraphGenerator;
import org.example.gdsmith.cypher.gen.SubgraphManager;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.cypher.standard_ast.*;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.expr.ConstExpression;

import java.util.List;

public class CompositePatternBasedGraphGenerator implements IGraphGenerator<CompositeGlobalState> {

    private static int minNumOfNodes = 200;
    private static int maxNumOfNodes = 200;
    private static double percentOfEdges = 0.001;
    private static List<IPattern> INodesPattern;

    private final CompositeGlobalState globalState;




    public CompositePatternBasedGraphGenerator(CompositeGlobalState globalState){
        this.globalState = globalState;
    }

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

    public List<CypherQueryAdapter> createGraph(CompositeGlobalState globalState) throws Exception {
        List<CypherQueryAdapter> queries = new SubgraphManager(globalState.getSchema(), globalState.getOptions()).generateCreateGraphQueries();

        CompositeSchema schema = globalState.getSchema();
        for(CypherSchema.CypherLabelInfo l : schema.getLabels()){
            for(IPropertyInfo propertyInfo : l.getProperties()){
                for(int i = 0; i < 20; i++){
                    StringBuilder sb = new StringBuilder();
                    sb.append("MATCH (n: ").append(l.getName()).append(") WITH * WHERE rand() < 0.2 SET n.")
                            .append(propertyInfo.getKey()).append(" = ");
                    generatePropertyValue(new Randomly(), propertyInfo.getType()).toTextRepresentation(sb);
                    queries.add(new CypherQueryAdapter(sb.toString()));
                }
            }
        }

        for(CypherSchema.CypherRelationTypeInfo r : schema.getRelationTypes()){
            for(IPropertyInfo propertyInfo : r.getProperties()){
                for(int i = 0; i < 20; i++){
                    StringBuilder sb = new StringBuilder();
                    sb.append("MATCH ()-[r: ").append(r.getName()).append("]->() WITH * WHERE rand() < 0.2 SET r.")
                            .append(propertyInfo.getKey()).append(" = ");
                    generatePropertyValue(new Randomly(), propertyInfo.getType()).toTextRepresentation(sb);
                    queries.add(new CypherQueryAdapter(sb.toString()));
                }
            }
        }

        //queries.add(new CypherQueryAdapter("MATCH (n) OPTIONAL MATCH (n)-[r]->() RETURN count(n.prop) + count(r.prop)"));
        return queries;
    }
}
