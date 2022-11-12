package org.example.gdsmith.gremlin;

import org.opencypher.gremlin.translation.TranslationFacade;

public class CypherGremlinTranslater {
    public static String translate(String cypherQuery){
        TranslationFacade cfog = new TranslationFacade();
        String gremlin = cfog.toGremlinGroovy(cypherQuery);
        return gremlin;
    }
}
