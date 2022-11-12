package org.example.gdsmith.neo4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.cypher.CypherConnection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Neo4jProxyConnection extends CypherConnection {


    private Neo4jOptions options;

    public Neo4jProxyConnection(Neo4jOptions options){
        this.options = options;
    }


    @Override
    public String getDatabaseVersion() throws Exception {
        //todo complete
        return "neo4j";
    }

    @Override
    public void close() throws Exception {
//        Neo4jDriverManager.closeDriver(driver);
    }

    @Override
    public void executeStatement(String arg) throws Exception{
        executeStatementAndGet(arg);
    }

    private static class Request{
        String username;
        String password;
        String host;
        int port;
        String query;
    }

    private static class Response{
        public GDSmithResultSet gdSmithResultSet;
        public String exceptionMsg;
    }


    @Override
    public List<GDSmithResultSet> executeStatementAndGet(String arg) throws Exception{
        try (Socket socket = new Socket("localhost", options.proxyPort)){
            OutputStream outputStream = socket.getOutputStream();
            Request request = new Request();
            request.username = options.getUsername();
            request.password = options.getPassword();
            request.port = options.getPort();
            request.host = options.getHost();
            request.query = arg;
            Gson gson = new GsonBuilder().serializeNulls().create();
            String requestString = gson.toJson(request);
            outputStream.write(requestString.getBytes());
            outputStream.flush();
            socket.shutdownOutput();

            InputStream inputStream = socket.getInputStream();
            byte[] responseBytes = inputStream.readAllBytes();
//            System.out.println(new String(responseBytes));
            Response response = gson.fromJson(new String(responseBytes), Response.class);
            if(!response.exceptionMsg.equals("")){
                throw new RuntimeException(response.exceptionMsg);
            }
            response.gdSmithResultSet.resolveFloat();
            return new ArrayList<>(Arrays.asList(response.gdSmithResultSet));
        }
    }
}
