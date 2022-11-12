package org.example.gdsmith;

import org.example.gdsmith.common.schema.AbstractSchema;

public interface IDatabaseTestingAlgorithm  <G extends GlobalState<O, ? extends AbstractSchema<G, ?>, C>, O extends DBMSSpecificOptions<? extends OracleFactory<G>>, C extends GDSmithDBConnection>{
    void generateAndTestDatabase(G globalState) throws Exception;
}
