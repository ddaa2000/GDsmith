package org.example.gdsmith.cypher.gen.query;

import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.ManualClauseSequence;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class ManualQueryGenerator <S extends CypherSchema<G,?>,G extends CypherGlobalState<?,S>> implements IQueryGenerator<S, G> {

    private int current = 0;
    private String filePath;
    public List<String> queries;

    public ManualQueryGenerator(){

    }

    public void loadFile(String filePath){
        this.filePath = filePath;
        File file = new File(filePath);
        if(!file.exists()){
            throw new RuntimeException();
        }

        try(FileReader fileReader = new FileReader(file)){
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)){
                queries = bufferedReader.lines().collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public IClauseSequence generateQuery(G globalState) {
        if(current < queries.size()){
            current++;
            return new ManualClauseSequence(queries.get(current - 1));
        }
//        else{
//            current = 0;
//            return new ManualClauseSequence(queries.get(0));
//        }
        System.exit(0);
        return null;
    }

    @Override
    public void addExecutionRecord(IClauseSequence clauseSequence, boolean isBugDetected, int resultSize) {

    }

    @Override
    public void addNewRecord(IClauseSequence sequence, boolean bugDetected, int resultLength, byte[] branchInfo, byte[] branchPairInfo) {

    }
}
