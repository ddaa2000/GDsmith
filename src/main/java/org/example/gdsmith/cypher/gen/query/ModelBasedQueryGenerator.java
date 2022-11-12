package org.example.gdsmith.cypher.gen.query;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.*;
import org.example.gdsmith.cypher.dsl.*;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.ClauseSequence;
import org.example.gdsmith.cypher.standard_ast.IClauseSequenceBuilder;

import java.util.Arrays;
import java.util.List;

public abstract class ModelBasedQueryGenerator<S extends CypherSchema<G,?>,G extends CypherGlobalState<?,S>> implements IQueryGenerator<S, G> {


    public IClauseSequenceBuilder generateClauses(IClauseSequenceBuilder seq, int len, List<String> generateClause) {
        if (len == 0) {
            return seq;
        }
        Randomly r = new Randomly();
        String generate = generateClause.get(r.getInteger(0, generateClause.size()));
        if (generate == "MATCH") {
            return generateClauses(seq.MatchClause(), len - 1, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
        } else if (generate == "OPTIONAL MATCH") {
            // return generateClauses(seq.OptionalMatchClause(), len - 1, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
            return generateClauses(seq.OptionalMatchClause(), len - 1, Arrays.asList("OPTIONAL MATCH", "WITH")); //todo
        } else if (generate == "WITH") {
            return generateClauses(seq.WithClause(), len - 1, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
        } else {
            return generateClauses(seq.UnwindClause(), len - 1, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
        }
    }

    public abstract IPatternGenerator createPatternGenerator(G globalState, IIdentifierBuilder identifierBuilder);
    public abstract IConditionGenerator createConditionGenerator(G globalState);
    public abstract IAliasGenerator createAliasGenerator(G globalState, IIdentifierBuilder identifierBuilder);
    public abstract IListGenerator createListGenerator(G globalState, IIdentifierBuilder identifierBuilder);

    public abstract boolean shouldDoMutation(G globalState);

    public abstract IClauseSequence doMutation(G globalState);

    protected void beforeGeneration(G globalState){

    }

    protected IClauseSequence postProcessing(G globalState, IClauseSequence clauseSequence){
        return clauseSequence;
    }


    public IClauseSequence generateQuery(G globalState){
        S schema = globalState.getSchema();
        Randomly r = new Randomly();
        IClauseSequence sequence = null;

        if (!shouldDoMutation(globalState)) {
            IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();
            int numOfClauses = r.getInteger(1, globalState.getOptions().getMaxClauseSize());
            sequence = generateClauses(builder.MatchClause(), numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
            new QueryFiller<S>(sequence,
                    createPatternGenerator(globalState, builder.getIdentifierBuilder()),
                    createConditionGenerator(globalState),
                    createAliasGenerator(globalState, builder.getIdentifierBuilder()),
                    createListGenerator(globalState, builder.getIdentifierBuilder()),
                    schema, builder.getIdentifierBuilder()).startVisit();
        } else {
            sequence = doMutation(globalState);
        }
        return postProcessing(globalState, sequence);
    }


}
