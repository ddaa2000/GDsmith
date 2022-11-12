package org.example.gdsmith.cypher.mutation;

import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.mutation.expression.*;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.mutation.expression.*;

public enum MutatorType {
    CLAUSE_SCISSORS, CONDITION_REFILL, CLAUSE_REFILL, CLAUSE_EXPANSION,
    AND_REMOVAL, COMPARISON_REVERSE, CONDITION_REVERSE, OR_REMOVAL, STRING_MATCH_REDUCTION,
    LABEL_ADDITION, PATTERN_ADDITION,
    LIMIT_EXPANDING, LIMIT_SHRINKING, OPTIONAL_ADDITION, OPTIONAL_REMOVAL, WHERE_ADDITION, WHERE_REFILL, WHERE_REMOVAL;

    public static <S extends CypherSchema<G,?>,G extends CypherGlobalState<?,S>> void mutate(MutatorType selectedType, G glbalState, IClauseSequence seedSeq){
        S schema = glbalState.getSchema();
        switch (selectedType){
            case OR_REMOVAL:
                new OrRemovalMutator(seedSeq).mutate();
                break;
            case AND_REMOVAL:
                new AndRemovalMutator(seedSeq).mutate();
                break;
            case COMPARISON_REVERSE:
                new ComparisonReverseMutator(seedSeq).mutate();
                break;
            case CONDITION_REVERSE:
                new ConditionReverseMutator(seedSeq).mutate();
                break;
            case STRING_MATCH_REDUCTION:
                new StringMatchReductionMutator(seedSeq).mutate();
                break;
            case WHERE_REFILL:
                new WhereRefillMutator<S>(seedSeq, schema).mutate();
                break;
            case WHERE_ADDITION:
                new WhereAdditionMutator<S>(seedSeq, schema).mutate();
                break;
            case WHERE_REMOVAL:
                new WhereRemovalMutator<S>(seedSeq).mutate();
                break;
            case LABEL_ADDITION:
                new LabelAdditionMutator<>(seedSeq, schema).mutate();
                break;
            case LIMIT_EXPANDING:
                new LimitExpandingMutator(seedSeq).mutate();
                break;
            case LIMIT_SHRINKING:
                new LimitShrinkingMutator().mutate();
                break;
            case CONDITION_REFILL:
                break;
            case OPTIONAL_REMOVAL:
                new OptionalRemovalMutator(seedSeq).mutate();
                break;
            case OPTIONAL_ADDITION:
                new OptionalAdditionMutator(seedSeq).mutate();
                break;
            case PATTERN_ADDITION:
                new PatternAdditionMutator<>(seedSeq, schema).mutate();
                break;
            case CLAUSE_REFILL:
                new ClauseRefillMutator<>(seedSeq, schema).mutate();
                break;
            case CLAUSE_SCISSORS:
                new ClauseScissorsMutator<>(seedSeq).mutate();
                break;
            case CLAUSE_EXPANSION:
                new ClauseExpansionMutator<>(seedSeq, schema).mutate();
                break;
        }
    }
}
