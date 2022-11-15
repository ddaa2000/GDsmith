package org.example.gdsmith;

import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;

import org.example.gdsmith.arcadeDB.ArcadeDBProvider;
import org.example.gdsmith.common.log.Loggable;
import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.common.query.Query;
import org.example.gdsmith.composite.CompositeProvider;
import org.example.gdsmith.composite.oracle.CompositeMCTSOracle;
import org.example.gdsmith.janusGraph.JanusProvider;
import org.example.gdsmith.memGraph.MemGraphProvider;
import org.example.gdsmith.neo4j.Neo4jProvider;
import org.example.gdsmith.tinkerGraph.TinkerProvider;
import org.example.gdsmith.agensGraph.AgensGraphProvider;
import org.example.gdsmith.redisGraph.RedisGraphProvider;
import jxl.Workbook;
import jxl.write.*;

public final class Main {

    public static final File LOG_DIRECTORY = new File("logs");
    public static volatile AtomicLong nrQueries = new AtomicLong();
    public static volatile AtomicLong nrDatabases = new AtomicLong();
    public static volatile AtomicLong nrSuccessfulActions = new AtomicLong();
    public static volatile AtomicLong nrUnsuccessfulActions = new AtomicLong();
    static int threadsShutdown;
    static boolean progressMonitorStarted;

    static {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
        if (!LOG_DIRECTORY.exists()) {
            LOG_DIRECTORY.mkdir();
        }
    }

    private Main() {
    }

    public static final class StateLogger {

        private File loggerFile;

        private int loggerNum = 0;
        private File curFile;
        private FileWriter logFileWriter;
        public FileWriter currentFileWriter;
        private static final List<String> INITIALIZED_PROVIDER_NAMES = new ArrayList<>();
        private final boolean logEachSelect;
        private final DatabaseProvider<?, ?, ?> databaseProvider;

        private File dir;

        private String databaseName;

        private static final class AlsoWriteToConsoleFileWriter extends FileWriter {

            AlsoWriteToConsoleFileWriter(File file) throws IOException {
                super(file);
            }

            @Override
            public Writer append(CharSequence arg0) throws IOException {
                System.err.println(arg0);
                return super.append(arg0);
            }

            @Override
            public void write(String str) throws IOException {
                System.err.println(str);
                super.write(str);
            }
        }

        public StateLogger(String databaseName, DatabaseProvider<?, ?, ?> provider, MainOptions options) {
            this.databaseName = databaseName;
            dir = new File(LOG_DIRECTORY, provider.getDBMSName());
            if (dir.exists() && !dir.isDirectory()) {
                throw new AssertionError(dir);
            }
            ensureExistsAndIsEmpty(dir, provider);
            loggerFile = new File(dir, databaseName + "-" + loggerNum + ".log");
            loggerNum++;
            logEachSelect = options.logEachSelect();
            if (logEachSelect) {
                curFile = new File(dir, databaseName + "-cur.log");
            }
            this.databaseProvider = provider;
        }

        private void ensureExistsAndIsEmpty(File dir, DatabaseProvider<?, ?, ?> provider) {
            if (INITIALIZED_PROVIDER_NAMES.contains(provider.getDBMSName())) {
                return;
            }
            synchronized (INITIALIZED_PROVIDER_NAMES) {
                if (!dir.exists()) {
                    try {
                        Files.createDirectories(dir.toPath());
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                }
                File[] listFiles = dir.listFiles();
                assert listFiles != null : "directory was just created, so it should exist";
                for (File file : listFiles) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }
                INITIALIZED_PROVIDER_NAMES.add(provider.getDBMSName());
            }
        }

        private FileWriter getLogFileWriter() {
            if (logFileWriter == null) {
                try {
                    logFileWriter = new AlsoWriteToConsoleFileWriter(loggerFile);
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }
            return logFileWriter;
        }

        public FileWriter getCurrentFileWriter() {
            if (!logEachSelect) {
                throw new UnsupportedOperationException();
            }
            if (currentFileWriter == null) {
                try {
                    currentFileWriter = new FileWriter(curFile, false);
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }
            return currentFileWriter;
        }

        public void writeCurrent(StateToReproduce state) {
            if (!logEachSelect) {
                throw new UnsupportedOperationException();
            }
            printState(getCurrentFileWriter(), state);
            try {
                currentFileWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void writeCurrent(String input) {
            write(databaseProvider.getLoggableFactory().createLoggable(input));
        }

        public void writeCurrentNoLineBreak(String input) {
            write(databaseProvider.getLoggableFactory().createLoggableWithNoLinebreak(input));
        }

        private void write(Loggable loggable) {
            if (!logEachSelect) {
                throw new UnsupportedOperationException();
            }
            try {
                getCurrentFileWriter().write(loggable.getLogString());

                currentFileWriter.flush();
            } catch (IOException e) {
                throw new AssertionError();
            }
        }

        public void logException(Throwable reduce, StateToReproduce state) {
            Loggable stackTrace = getStackTrace(reduce);
            FileWriter logFileWriter2 = getLogFileWriter();
            try {
                logFileWriter2.write(stackTrace.getLogString());
                printState(logFileWriter2, state);
            } catch (IOException e) {
                throw new AssertionError(e);
            } finally {
                try {
                    logFileWriter2.flush();
                    logFileWriter2.close();
                    logFileWriter = null;
                    loggerFile = new File(dir, databaseName + "-" + loggerNum + ".log");
                    loggerNum++;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void logExceptionPartial(Throwable reduce, StateToReproduce state, Query<?> query) {
            Loggable stackTrace = getStackTrace(reduce);
            FileWriter logFileWriter2 = getLogFileWriter();
            try {
                logFileWriter2.write(stackTrace.getLogString());
                printCreateState(logFileWriter2, state);

                StringBuilder sb = new StringBuilder();

                sb.append("\n\n");

                sb.append(query.getLogString());
                logFileWriter2.write(sb.toString());
            } catch (IOException e) {
                throw new AssertionError(e);
            } finally {
                try {
                    logFileWriter2.flush();
                    logFileWriter2.close();
                    logFileWriter = null;
                    loggerFile = new File(dir, databaseName + "-" + loggerNum + ".log");
                    loggerNum++;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        private Loggable getStackTrace(Throwable e1) {
            return databaseProvider.getLoggableFactory().convertStacktraceToLoggable(e1);
        }

        private void printState(FileWriter writer, StateToReproduce state) {
            StringBuilder sb = new StringBuilder();

            sb.append(databaseProvider.getLoggableFactory()
                    .getInfo(state.getDatabaseName(), state.getDatabaseVersion(), state.getSeedValue()).getLogString());

            for (Query<?> s : state.getStatements()) {
                sb.append(s.getLogString());
                sb.append('\n');
            }
            try {
                writer.write(sb.toString());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        private void printCreateState(FileWriter writer, StateToReproduce state) {
            StringBuilder sb = new StringBuilder();

            sb.append(databaseProvider.getLoggableFactory()
                    .getInfo(state.getDatabaseName(), state.getDatabaseVersion(), state.getSeedValue()).getLogString());

            for (Query<?> s : state.getCreateStatements()) {
                sb.append(s.getLogString());
                sb.append('\n');
            }
            try {
                writer.write(sb.toString());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

    }

    public static class QueryManager<C extends GDSmithDBConnection> {

        private final GlobalState<?, ?, C> globalState;

        QueryManager(GlobalState<?, ?, C> globalState) {
            this.globalState = globalState;
        }

        public boolean execute(Query<C> q, String... fills) throws Exception {
            globalState.getState().logStatement(q);
            boolean success;
            success = q.execute(globalState, fills);
            Main.nrSuccessfulActions.addAndGet(1);
            return success;
        }

        public List<GDSmithResultSet> executeAndGet(Query<C> q, String... fills) throws Exception {
            globalState.getState().logStatement(q);
            List<GDSmithResultSet> result;
            result = q.executeAndGet(globalState, fills);
            Main.nrSuccessfulActions.addAndGet(1);
            return result;
        }

        public void incrementSelectQueryCount() {
            Main.nrQueries.addAndGet(1);
        }

        public void incrementCreateDatabase() {
            Main.nrDatabases.addAndGet(1);
        }

        public List<Long> executeAndGetTime(Query<C> q, String... fills) throws Exception {
            globalState.getState().logStatement(q);
            List<Long> result;
            result = q.executeAndGetTime(globalState, fills);
            Main.nrSuccessfulActions.addAndGet(1);
            return result;
        }
    }

    public static void main(String[] args) throws IOException {
        System.exit(executeMain(args));
    }

    public static class DBMSExecutor<G extends GlobalState<O, ?, C>, O extends DBMSSpecificOptions<?>, C extends GDSmithDBConnection> {

        private final DatabaseProvider<G, O, C> provider;
        private final MainOptions options;
        private final O command;
        private final String databaseName;
        private StateLogger logger;
        private StateToReproduce stateToRepro;
        private final Randomly r;

        public DBMSExecutor(DatabaseProvider<G, O, C> provider, MainOptions options, O dbmsSpecificOptions,
                String databaseName, Randomly r) {
            this.provider = provider;
            this.options = options;
            this.databaseName = databaseName;
            this.command = dbmsSpecificOptions;
            this.r = r;
        }

        private G createGlobalState() {
            try {
                return provider.getGlobalStateClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        public O getCommand() {
            return command;
        }

        public void testConnection() throws Exception {
            G state = getInitializedGlobalState(options.getRandomSeed());
            try (GDSmithDBConnection con = provider.createDatabase(state)) {
                return;
            }
        }

        public void run() throws Exception { //todo 主要逻辑
            G state = createGlobalState(); //G是global state的子类
            stateToRepro = provider.getStateToReproduce(databaseName);
            stateToRepro.seedValue = r.getSeed();
            state.setState(stateToRepro);
            logger = new StateLogger(databaseName, provider, options);
            state.setRandomly(r);
            state.setDatabaseName(databaseName);
            state.setMainOptions(options);
            state.setDbmsSpecificOptions(command);
            System.gc();
            try (C con = provider.createDatabase(state)) { //创建数据库
                QueryManager<C> manager = new QueryManager<>(state); //todo QueryManager
                try {
                    stateToRepro.databaseVersion = con.getDatabaseVersion();
                } catch (Exception e) {
                    // ignore
                }
                state.setConnection(con);//把连接存在了globalState中
                state.setStateLogger(logger);
                state.setManager(manager);
                if (options.logEachSelect()) {
                    logger.writeCurrent(state.getState());
                }
                provider.generateAndTestDatabase(state); //todo 主逻辑？
                try {
                    logger.getCurrentFileWriter().close();
                    logger.currentFileWriter = null;
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }
        }

        private G getInitializedGlobalState(long seed) {
            G state = createGlobalState();
            stateToRepro = provider.getStateToReproduce(databaseName);
            stateToRepro.seedValue = seed;
            state.setState(stateToRepro);
            logger = new StateLogger(databaseName, provider, options);
            Randomly r = new Randomly(seed);
            state.setRandomly(r);
            state.setDatabaseName(databaseName);
            state.setMainOptions(options);
            state.setDbmsSpecificOptions(command);
            return state;
        }

        public StateLogger getLogger() {
            return logger;
        }

        public StateToReproduce getStateToReproduce() {
            return stateToRepro;
        }
    }

    public static class DBMSExecutorFactory<G extends GlobalState<O, ?, C>, O extends DBMSSpecificOptions<?>, C extends GDSmithDBConnection> {

        private final DatabaseProvider<G, O, C> provider;
        private final MainOptions options;
        private final O command;

        public DBMSExecutorFactory(DatabaseProvider<G, O, C> provider, MainOptions options) {
            this.provider = provider;
            this.options = options;
            this.command = createCommand();
        }

        private O createCommand() {
            try {
                return provider.getOptionClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        public O getCommand() {
            return command;
        }

        @SuppressWarnings("unchecked")
        public DBMSExecutor<G, O, C> getDBMSExecutor(String databaseName, Randomly r) {
            try {
                return new DBMSExecutor<G, O, C>(provider.getClass().getDeclaredConstructor().newInstance(), options,
                        command, databaseName, r);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        public DatabaseProvider<G, O, C> getProvider() {
            return provider;
        }

    }

    public static int executeMain(String... args) throws AssertionError {
        List<DatabaseProvider<?, ?, ?>> providers = getDBMSProviders(); // 不同的数据库的provider
        Map<String, DBMSExecutorFactory<?, ?, ?>> nameToProvider = new HashMap<>();
        MainOptions options = new MainOptions();
        Builder commandBuilder = JCommander.newBuilder().addObject(options);
        for (DatabaseProvider<?, ?, ?> provider : providers) {
            String name = provider.getDBMSName();
            if (!name.toLowerCase().equals(name)) {
                throw new AssertionError(name + " should be in lowercase!");
            }
            DBMSExecutorFactory<?, ?, ?> executorFactory = new DBMSExecutorFactory<>(provider, options);
            commandBuilder = commandBuilder.addCommand(name, executorFactory.getCommand());
            nameToProvider.put(name, executorFactory);
        }
        JCommander jc = commandBuilder.programName("SQLancer").build();
        jc.parse(args);

        if (jc.getParsedCommand() == null || options.isHelp()) {
            jc.usage();
            return options.getErrorExitCode();
        }

        Randomly.initialize(options);
        if (options.printProgressInformation()) {
            startProgressMonitor();
            if (options.printProgressSummary()) {
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("Overall execution statistics");
                        System.out.println("============================");
                        System.out.println(formatInteger(nrQueries.get()) + " queries");
                        System.out.println(formatInteger(nrDatabases.get()) + " databases");
                        System.out.println(
                                formatInteger(nrSuccessfulActions.get()) + " successfully-executed statements");
                        System.out.println(
                                formatInteger(nrUnsuccessfulActions.get()) + " unsuccessfuly-executed statements");
                    }

                    private String formatInteger(long intValue) {
                        if (intValue > 1000) {
                            return String.format("%,9dk", intValue / 1000);
                        } else {
                            return String.format("%,10d", intValue);
                        }
                    }
                }));
            }
        }

        ExecutorService execService = Executors.newFixedThreadPool(options.getNumberConcurrentThreads());
        DBMSExecutorFactory<?, ?, ?> executorFactory = nameToProvider.get(jc.getParsedCommand());

        if (options.performConnectionTest()) {
            try {
                executorFactory.getDBMSExecutor(options.getDatabasePrefix() + "connectiontest", new Randomly())
                        .testConnection();
            } catch (Exception e) {
                System.err.println(
                        "SQLancer failed creating a test database, indicating that SQLancer might have failed connecting to the DBMS. In order to change the username, password, host and port, you can use the --username, --password, --host and --port options.\n\n");
                e.printStackTrace();
                return options.getErrorExitCode();
            }
        }

        for (int i = 0; i < options.getTotalNumberTries(); i++) {
            final String databaseName = options.getDatabasePrefix() + i;
            final long seed;
            if (options.getRandomSeed() == -1) {
                seed = System.currentTimeMillis() + i;
            } else {
                seed = options.getRandomSeed() + i;
            }
            execService.execute(new Runnable() {

                @Override
                public void run() {
                    Thread.currentThread().setName(databaseName);
                    runThread(databaseName);
                }

                private void runThread(final String databaseName) {
                    Randomly r = new Randomly(seed);
                    try {
                        if (options.getMaxGeneratedDatabases() == -1) {
                            // run without a limit
                            boolean continueRunning = true;
                            while (continueRunning) {
                                continueRunning = run(options, execService, executorFactory, r, databaseName);
                            }
                        } else {
                            for (int i = 0; i < options.getMaxGeneratedDatabases(); i++) {
                                boolean continueRunning = run(options, execService, executorFactory, r, databaseName);
                                if (!continueRunning) {
                                    break;
                                }
                            }
                        }
                    } finally {
                        threadsShutdown++;
                        if (threadsShutdown == options.getTotalNumberTries()) {
                            execService.shutdown();
                        }
                    }
                }

                private boolean run(MainOptions options, ExecutorService execService,
                        DBMSExecutorFactory<?, ?, ?> executorFactory, Randomly r, final String databaseName) {
                    DBMSExecutor<?, ?, ?> executor = executorFactory.getDBMSExecutor(databaseName, r); //todo 似乎是这个
                    try {
                        executor.run();
                        return true;
                    } catch (IgnoreMeException e) {
                        return true;
                    } catch (Throwable reduce) {
                        reduce.printStackTrace();
                        executor.getStateToReproduce().exception = reduce.getMessage();
                        executor.getLogger().logFileWriter = null;
                        executor.getLogger().logException(reduce, executor.getStateToReproduce());
                        return false;
                    } finally {
                        try {
                            if (options.logEachSelect()) {
                                if (executor.getLogger().currentFileWriter != null) {
                                    executor.getLogger().currentFileWriter.close();
                                }
                                executor.getLogger().currentFileWriter = null;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        try {
            if (options.getTimeoutSeconds() == -1) {
                execService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } else {
                execService.awaitTermination(options.getTimeoutSeconds(), TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return threadsShutdown == 0 ? 0 : options.getErrorExitCode();
    }

    static List<DatabaseProvider<?, ?, ?>> providers = new ArrayList<>();
    public static List<DatabaseProvider<?, ?, ?>> getDBMSProviders() {
        if(providers.size()==0){
            providers.add(new Neo4jProvider());
            providers.add(new AgensGraphProvider());
            providers.add(new RedisGraphProvider());
            providers.add(new MemGraphProvider());
            providers.add(new ArcadeDBProvider());
            providers.add(new JanusProvider());
            providers.add(new TinkerProvider());
            providers.add(new CompositeProvider());
        }
        return providers;
    }

    private static synchronized void startProgressMonitor() {
        if (progressMonitorStarted) {
            /*
             * it might be already started if, for example, the main method is called multiple times in a test (see
             * https://github.com/sqlancer/sqlancer/issues/90).
             */
            return;
        } else {
            progressMonitorStarted = true;
        }
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        /*scheduler.scheduleAtFixedRate(new Runnable() {

            private long timeMillis = System.currentTimeMillis();
            private long lastNrQueries;
            private long lastNrDbs;

            {
                timeMillis = System.currentTimeMillis();
            }

            @Override
            public void run() {
                long elapsedTimeMillis = System.currentTimeMillis() - timeMillis;
                long currentNrQueries = nrQueries.get();
                long nrCurrentQueries = currentNrQueries - lastNrQueries;
                double throughput = nrCurrentQueries / (elapsedTimeMillis / 1000d);
                long currentNrDbs = nrDatabases.get();
                long nrCurrentDbs = currentNrDbs - lastNrDbs;
                double throughputDbs = nrCurrentDbs / (elapsedTimeMillis / 1000d);
                long successfulStatementsRatio = (long) (100.0 * nrSuccessfulActions.get()
                        / (nrSuccessfulActions.get() + nrUnsuccessfulActions.get()));
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                System.out.println(String.format(
                        "[%s] Executed %d queries (%d queries/s; %.2f/s dbs, successful statements: %2d%%). Threads shut down: %d.",
                        dateFormat.format(date), currentNrQueries, (int) throughput, throughputDbs,
                        successfulStatementsRatio, threadsShutdown));
                timeMillis = System.currentTimeMillis();
                lastNrQueries = currentNrQueries;
                lastNrDbs = currentNrDbs;
            }
        }, 5, 5, TimeUnit.SECONDS);*/

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                /*List<String> listTest = Arrays.asList(dateFormat.format(date), String.valueOf(CompositePerformanceOracle.maxRatio),
                        String.valueOf(CompositePerformanceOracle.numof1to10), String.valueOf(CompositePerformanceOracle.numof10to100), String.valueOf(CompositePerformanceOracle.numof100to1000),
                        String.valueOf(CompositePerformanceOracle.numGreaterThan1000), String.valueOf(CompositePerformanceOracle.numofTimeOut));*/
                List<String> listTest = Arrays.asList(dateFormat.format(date), String.valueOf(CompositeMCTSOracle.maxDc),
                        String.valueOf(CompositeMCTSOracle.maxSeq), String.valueOf(CompositeMCTSOracle.maxDr), String.valueOf(CompositeMCTSOracle.maxDa),
                        String.valueOf(CompositeMCTSOracle.maxT1),String.valueOf(CompositeMCTSOracle.maxT2),
                        String.valueOf(CompositeMCTSOracle.numofQueries), String.valueOf(CompositeMCTSOracle.numofTimeOut));

                WritableWorkbook wwb = null;
                InputStream io = null;
                Workbook wb = null;
//                try {
//                    //File file2 = new File("./logs/Neo4jRedisGraphRS.xls");
//                    File file2 = new File("./metric.xls");
//                    io = Files.newInputStream(file2.toPath());
//                    //获取工作簿
//                    wb = Workbook.getWorkbook(io);
//                    //Sheet1: 0 ; Sheet2:1 ; Sheet3:2
//                    Sheet readsheet = wb.getSheet(0);
//                    //创建一个可写入的工作簿
//                    wwb = Workbook.createWorkbook(file2, wb);
//                    // 内容显示格式
//                    WritableCellFormat wcf = new WritableCellFormat();
//                    wcf.setWrap(true);   //设置单元格可以换行
//                    wcf.setAlignment(Alignment.CENTRE);   //水平居中
//                    wcf.setVerticalAlignment(VerticalAlignment.CENTRE);  // 垂直居中
//                    wcf.setFont(new WritableFont(WritableFont.TIMES, 11)); // 内容字体 11号
//                    WritableSheet ws = wwb.getSheet(0);
//                    //获取表格行数，从空白行开始写入数据
//                    int k = readsheet.getRows();
//                    for (int i = 0; i < listTest.size(); i++) {
//                        ws.addCell(new Label(i, k, listTest.get(i), wcf));
//                    }
//                    wwb.write();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        if (wwb != null) {
//                            wwb.close();
//                        }
//                        if (io != null) {
//                            io.close();
//                        }
//                        if (wb != null) {
//                            wb.close();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }, 20, 60, TimeUnit.SECONDS);
    }

}
