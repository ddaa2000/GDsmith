package org.example.gdsmith.cypher.mutation;

import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.*;
import org.example.gdsmith.cypher.gen.alias.RandomAliasGenerator;
import org.example.gdsmith.cypher.gen.condition.RandomConditionGenerator;
import org.example.gdsmith.cypher.gen.list.RandomListGenerator;
import org.example.gdsmith.cypher.gen.pattern.RandomPatternGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.dsl.QueryFiller;

public class ClauseRefillMutator<S extends CypherSchema<?,?>> extends QueryFiller<S> implements IClauseMutator {
    public ClauseRefillMutator(IClauseSequence clauseSequence, S schema) {
        super(clauseSequence,
                new RandomPatternGenerator<>(schema, clauseSequence.getIdentifierBuilder(), true),
                new RandomConditionGenerator<>(schema, true),
                new RandomAliasGenerator<>(schema, clauseSequence.getIdentifierBuilder(), true),
                new RandomListGenerator<>(schema, clauseSequence.getIdentifierBuilder(), true),
                schema, clauseSequence.getIdentifierBuilder());
    }

    @Override
    public void mutate() {
        startVisit();
    }
}
