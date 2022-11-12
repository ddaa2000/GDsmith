package org.example.gdsmith.composite;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.IGeneratorFactory;
import org.example.gdsmith.IGraphGeneratorFactory;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.composite.gen.CompositeGraphGenerator;
import org.example.gdsmith.composite.gen.CompositePatternBasedGraphGenerator;
import org.example.gdsmith.composite.oracle.*;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.cypher.dsl.IGraphGenerator;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.cypher.gen.query.AdvancedQueryGenerator;
import org.example.gdsmith.cypher.gen.graph.ManualGraphGenerator;
import org.example.gdsmith.cypher.gen.query.ManualQueryGenerator;
import org.example.gdsmith.cypher.gen.query.RandomQueryGenerator;
import org.example.gdsmith.composite.oracle.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Parameters(separators = "=", commandDescription = "Composite (default port: " + CompositeOptions.DEFAULT_PORT
        + ", default host: " + CompositeOptions.DEFAULT_HOST)
public class CompositeOptions implements DBMSSpecificOptions<CompositeOptions.CompositeOracleFactory> {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 2424; //todo 改


    @Parameter(names = "--oracle")
    public List<CompositeOracleFactory> oracles = Arrays.asList(CompositeOracleFactory.DIFFERENTIAL);

    @Parameter(names = "--generator")
    public CompositeGeneratorFactory generator = CompositeGeneratorFactory.RANDOM;

    @Parameter(names = "--graph")
    public CompositeGraphGeneratorFactory graphGenerator = CompositeGraphGeneratorFactory.RANDOM;

    public String getConfigPath() {
        return configPath;
    }

    @Parameter(names = "--config")
    public String configPath = "./config.json";

    @Override
    public List<CompositeOracleFactory> getTestOracleFactory() {
        return oracles;
    }

    @Override
    public IQueryGenerator<CompositeSchema, CompositeGlobalState> getQueryGenerator() {
        return generator.create();
    }

    public enum CompositeGraphGeneratorFactory implements IGraphGeneratorFactory<CompositeGlobalState, IGraphGenerator<CompositeGlobalState>>{
        RANDOM{
            @Override
            public IGraphGenerator<CompositeGlobalState> create(CompositeGlobalState globalState) {
                return new CompositeGraphGenerator(globalState);
            }
        },
        MANUAL{
            @Override
            public IGraphGenerator<CompositeGlobalState> create(CompositeGlobalState globalState) {
                ManualGraphGenerator<CompositeGlobalState> manualGraphGenerator = new ManualGraphGenerator<>();
                manualGraphGenerator.loadFile("./graph.txt");
                return manualGraphGenerator;
            }
        },
        PATTERN_BASED{
            @Override
            public IGraphGenerator<CompositeGlobalState> create(CompositeGlobalState globalState) {
                return new CompositePatternBasedGraphGenerator(globalState);
            }
        }
    }




    public enum CompositeGeneratorFactory implements IGeneratorFactory<IQueryGenerator<CompositeSchema,CompositeGlobalState>>{
        RANDOM {
            @Override
            public IQueryGenerator<CompositeSchema, CompositeGlobalState> create() {
                return new RandomQueryGenerator<>();
            }
        },

        ADVANCED{
            @Override
            public IQueryGenerator<CompositeSchema, CompositeGlobalState> create() {
                return new AdvancedQueryGenerator<>();
            }
        },

        MANUAL{
            @Override
            public IQueryGenerator<CompositeSchema, CompositeGlobalState> create() {
                ManualQueryGenerator<CompositeSchema, CompositeGlobalState> generator = new ManualQueryGenerator<>();
                generator.loadFile("./query.txt");
                return generator;
            }
        }
    }

    public enum CompositeOracleFactory implements OracleFactory<CompositeGlobalState> {

        ALWAYS_TRUE {

            @Override
            public TestOracle create(CompositeGlobalState globalState) throws SQLException {
                return new CompositeAlwaysTrueOracle(globalState);
            }
        },
        DIFFERENTIAL {
            @Override
            public TestOracle create(CompositeGlobalState globalState) throws SQLException{
                return new CompositeDifferentialOracle(globalState);
            }
        },
        PERFORMANCE {
            @Override
            public TestOracle create(CompositeGlobalState globalState) throws SQLException{
                return new CompositePerformanceOracle(globalState);
            }
        },
        PURE_PERFORMANCE{
            @Override
            public TestOracle create(CompositeGlobalState globalState) throws SQLException{
                return new CompositePurePerformanceOracle(globalState);
            }
        },
        MCTS {
            @Override
            public TestOracle create(CompositeGlobalState globalState) throws SQLException{
                return new CompositeMCTSOracle(globalState);
            }
        }
    }
}
