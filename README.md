# GDsmith

We propose GDsmith, to the best of our knowledge the first black-box approach for testing graph database engines.

# Getting Started

Requirements:
* Java 11
* [Maven](https://maven.apache.org/)
* The graph database engines that you want to test (now supporting Neo4j, RedisGraph, and Memgraph)



# Project Structure

* src
    * main/java/org.example.gdsmith
        * common:  infrastructure
        * cypher
          * ast/standard_ast: implementation of ast structure
          * algorithm: algorithms for GDsmith
            * Compared3AlgorithmNew.java: the original algorithm used by GDsmith
            * Compared1AlgorithmNew.java: the algorithm used by the baseline1
            * Compared2AlgorithmNew.java: the algorithm used by the baseline2
          * gen: generators for queries, graphs, patterns, expressions
            * condition
              * GuidedConditionGenerator.java: the condition generator for GDsmith
              * RandomConditionGenerator.java: the condition generator for baseline1 and baseline2
            * graph
              * SlidingGraphGenerator.java: the graph generator for GDsmith, baseline1 and baseline2 (baseline1 uses two generators to remove guidance strategy, see Compared1AlgorithmNew.java)
            * expr
              * NonEmptyExpressionGenerator.java: the expression generator for GDsmith
              * RandomExpressionGenerator.java: the expression generator for baseline1 and baseline2
          * oracle: oracles
            * DifferentialNonEmptyBranchOracle.java: the differential oracle used by GDsmith, baseline1 and baseline2
        * support for different databases[neo4j, redisGraph, memgraph, ...]
* out: the executable jar file GDsmith.jar
* NewExperiment: the experiment results.
    * compare123.ipynb: get the version-distinct number of discrepancies
    * compare123.svg: the result of compare123.ipynb


# Quick Start
In this section we are going to do differential testing on MemGraph and RedisGraph.
Run the docker images of MemGraph and RedisGraph:

```shell
docker run -d -p 7687:7687 -p 7444:7444 memgraph/memgraph:2.4.0 --query-execution-timeout-sec=1
docker run -d -e REDISGRAPH_ARGS=\"TIMEOUT=1000\" -p 6379:6379 -it --rm redislabs/redisgraph:2.8.20
```

Create the file config.json and paste the following content:
```json
{
  "memgraph@2.4.0": {
    "port": 7687,
    "host": "localhost",
    "username": "neo4j",
    "password": "sqlancer",
    "restart-command": "docker run -d -p 7687:7687 -p 7444:7444 memgraph/memgraph:2.4.0 --query-execution-timeout-sec=1"
  },
  "redisgraph@2.8.17": {
    "port": 6379,
    "host": "localhost",
    "username": "neo4j",
    "password": "sqlancer",
    "restart-command": "docker run -d -e REDISGRAPH_ARGS=\"TIMEOUT=1000\" -p 6379:6379 -it --rm redislabs/redisgraph:2.8.20"
  }
}
```
Then run the following command:
```shell
java -jar GDsmith.jar --algorithm compared3 --num-tries 100 -num-queries 1000 composite
```
The testing should begin, GDsmith will generate 100 graphs and for each graph it will generate 1000 queries.
All the failures found will be recorded in ```logs``` directory.


# Using GDsmith

Generally GDsmith can be configured and executed using the following command:

```bash
java -jar GDsmith.jar [database] --[database_option1] --[database_option2] ...
```

Here are some examples of database options:

```
--algorithm <algorithm-name> // the algorithm for testing
--num-tries <num-tries> // the number of graphs to generate
--num-queries <num-queries> // the number of queries generated for each graph
```

For example, if you want to test Neo4j and use the crash oracle, you can use:

```bash
java -jar GDsmith.jar neo4j --port 7687 --username xxx --password ### --oracle RANDOM_CRASH
```

For the ```composite``` database, as each database may use a different set of configurations, the configuraion for each database is specified in the ```config.json``` file:
```json
{
  "neo4j@4.4.13": {
    "port": 7687,
    "host": "localhost",
    "username": "neo4j",
    "password": "sqlancer"
  },
  "neo4j@4.4.12": {
    "port": 10101,
    "host": "localhost",
    "username": "neo4j",
    "password": "sqlancer"
  }
}
```
GDsmith identify the name of the database by the key in the json file before the "@" character. For example "neo4j@4.4.12" tells neo4j to connect to a datbase using the Neo4j driver. So you can add multiple databases of the same database and name them freely as long as the database name before "@" is corresponded with the actual database:
```json
{
  "neo4j@latest": {
    "port": 7687,
    "host": "localhost",
    "username": "neo4j",
    "password": "sqlancer"
  },
  "neo4j@optimization": {
    "port": 10101,
    "host": "localhost",
    "username": "neo4j",
    "password": "sqlancer"
  },
  "redisgraph@2.8.17": {
    "port": 6379,
    "host": "localhost",
    "username": "neo4j",
    "password": "sqlancer",
    "restart-command": "docker run -d -e REDISGRAPH_ARGS=\"TIMEOUT=1000\" -p 6379:6379 -it --rm redislabs/redisgraph:2.8.20"
  }
}
```

Notice that GDsmith will not automatically create database user, as a result, you might need to manually create a user and grant it with the privilege for remote connection, executing queries, writing to databases and creating/deleting new databases.



Here are the set of databases supported by GDsmith:

```
neo4j
redisgraph
memgraph
compposite  \\ a special abstract database that represents multiple database instances used for differential tesing
```



# Experiment

In the paper we run GDsmith for 12 hours on Neo4j-3.5.0 and compare the result with Neo4j-4.4.12 and we further get version-distinct discrepancy number by running the these discrepancy-revealing graphs and queries on multiple versions between Neo4j-3.5.0 and Neo4j-4.4.12. The results can be seen on our paper.

In addition, we run GDsmith for 12 hours on Neo4j-3.5.0, Neo4j-4.0.0, Neo4j-4.1.0, Neo4j-4.2.0, Neo4j-4.3.0 and Neo4j-4.4.0. We compare the results with Neo4j-4.4.12. We divide the experiment into 3 groups (which is [Neo4j-3.5.0], [Neo4j-4.0.0, Neo4j-4.1.0, Neo4j-4.2.0], [Neo4j-4.3.0, Neo4j-4.4.0]) and we run each group for 12 hours. Then we compare the results on multiple versions between Neo4j-3.5.0 and Neo4j-4.4.12 to get the version-distinct discrepancy number. We believe this experiment can reveal more precise results as more version are chosen during testing. The results are shown in NewExperiment/compare123.svg.

We also run GDsmith on old versions of RedisGraph and MemGraph. However we don't get the version-distinct discrepancy number of them because there are too many discrepancies and it takes to much time to re-run them on all old versions.
