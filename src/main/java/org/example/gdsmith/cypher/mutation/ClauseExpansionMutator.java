package org.example.gdsmith.cypher.mutation;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.QueryFiller;
import org.example.gdsmith.cypher.gen.alias.RandomAliasGenerator;
import org.example.gdsmith.cypher.gen.condition.RandomConditionGenerator;
import org.example.gdsmith.cypher.gen.list.RandomListGenerator;
import org.example.gdsmith.cypher.gen.pattern.RandomPatternGenerator;
import org.example.gdsmith.cypher.gen.query.RandomQueryGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.ClauseSequence;
import org.example.gdsmith.cypher.standard_ast.IClauseSequenceBuilder;

import java.util.Arrays;

public class ClauseExpansionMutator<S extends CypherSchema<G,?>, G extends CypherGlobalState<?, S>> implements IClauseMutator{

    private IClauseSequence sequence;
    private S schema;

    public ClauseExpansionMutator(IClauseSequence sequence, S schema){
        this.sequence = sequence;
        this.schema = schema;
    }

    @Override
    public void mutate() {
        IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();
        int numOfClauses = Randomly.smallNumber();
        sequence = new RandomQueryGenerator<S, G>().generateClauses(builder.WithClause(), numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
        new QueryFiller<S>(sequence,
                new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
                new RandomConditionGenerator<>(schema, false),
                new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
                new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
                schema, builder.getIdentifierBuilder()).startVisit();
    }
}
