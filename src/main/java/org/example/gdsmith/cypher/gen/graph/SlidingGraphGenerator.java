package org.example.gdsmith.cypher.gen.graph;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IPattern;
import org.example.gdsmith.cypher.dsl.IGraphGenerator;
import org.example.gdsmith.cypher.gen.GraphManager;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.expr.ConstExpression;

import java.util.List;

public class SlidingGraphGenerator<G extends CypherGlobalState<?,S>, S extends CypherSchema<G,?>> implements IGraphGenerator<G> {

    private static int minNumOfNodes = 200;
    private static int maxNumOfNodes = 200;
    private static double percentOfEdges = 0.001;
    private static List<IPattern> INodesPattern;

    private final G globalState;


    private GraphManager graphManager;


    public SlidingGraphGenerator(G globalState){
        this.globalState = globalState;
        graphManager = new GraphManager(globalState.getSchema(), globalState.getOptions());
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

    public GraphManager getGraphManager(){
        return graphManager;
    }


    @Override
    public List<CypherQueryAdapter> createGraph(G globalState) throws Exception {
        List<CypherQueryAdapter> queries = graphManager.generateCreateGraphQueries();

        //queries.add(new CypherQueryAdapter("MATCH (n) OPTIONAL MATCH (n)-[r]->() RETURN count(n.prop) + count(r.prop)"));
        return queries;
    }
}

