package org.example.gdsmith.cypher.gen.graph;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IPattern;
import org.example.gdsmith.cypher.dsl.IGraphGenerator;
import org.example.gdsmith.cypher.gen.EnumerationGraphManager;
import org.example.gdsmith.cypher.gen.EnumerationSeq;
import org.example.gdsmith.cypher.gen.GraphManager;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.expr.ConstExpression;

import java.util.List;

public class EnumerationGraphGenerator<G extends CypherGlobalState<?,S>, S extends CypherSchema<G,?>> implements IGraphGenerator<G> {

    private final G globalState;
    private EnumerationSeq enumerationSeq;


    private EnumerationGraphManager enumerationGraphManager;


    public EnumerationGraphGenerator(G globalState, EnumerationSeq enumerationSeq){
        this.globalState = globalState;
        enumerationGraphManager = new EnumerationGraphManager(globalState.getSchema(), globalState.getOptions(), enumerationSeq);
    }


    @Override
    public List<CypherQueryAdapter> createGraph(G globalState) throws Exception {
        List<CypherQueryAdapter> queries = enumerationGraphManager.generateCreateGraphQueries();

        //queries.add(new CypherQueryAdapter("MATCH (n) OPTIONAL MATCH (n)-[r]->() RETURN count(n.prop) + count(r.prop)"));
        return queries;
    }
}

