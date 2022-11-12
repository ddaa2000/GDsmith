package org.example.gdsmith.common.query;


import com.alibaba.fastjson.JSONArray;
import org.neo4j.driver.Record;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class GDSmithResultSet implements Closeable {

    int resultRowNum;
    List<Map<String, Object>> result;

    public List<Map<String, Object>> getResult() {
        return result;
    }

    private String getMapAsString(Map<String, Object> m) {
        String s = "{";
        TreeSet<String> ts =new TreeSet<>(m.keySet());
        for (String key : ts) {
            if (m.get(key) == null || m.get(key).toString().contains("null")) {
                s += key + ":null,";
            } else {
//                if(!(m.get(key) instanceof Number) && !(m.get(key) instanceof Boolean) && !(m.get(key) instanceof String)){
//                    System.out.println(m.get(key).getClass());
//                    System.exit(-1);
//                }
                s += key + ":" + m.get(key).toString() + ",";
            }



//            if(m.get(key) instanceof Double || m.get(key) instanceof Float){
//                System.out.println("got double or float");
//                System.exit(-1);
//            }
        }
        s += "}";
        return s;
    }

    public List<String> resultToStringList() {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < result.size(); ++i) {
            l.add(getMapAsString(result.get(i)));
        }
        return l;
    }

    public void resolveFloat(){
        List<Map<String, Object>> newList = new ArrayList<>();
        for(Map<String, Object> map : result){
            Map<String, Object> newMap = new HashMap<>();
            for(Map.Entry<String, Object> entry : map.entrySet()){
                if(entry.getValue() instanceof Number){
                    long value = ((Number) entry.getValue()).longValue();
                    newMap.put(entry.getKey(), value);
                }
                else if(entry.getValue() instanceof List){
                    List list = (List) entry.getValue();
                    if(list.size() == 0){
                        newMap.put(entry.getKey(), entry.getValue());
                    }
                    else{
                        newMap.put(entry.getKey(), list.stream().map(
                                e->{
                                    //todo: cascade list
                                    if(e instanceof Number){
                                        long value = ((Number) e).longValue();
                                        return value;
                                    }
                                    else{
                                        return e;
                                    }
                                }
                        ).collect(Collectors.toList()));
                    }
                }
                else{
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }
            newList.add(newMap);
        }
        this.result = newList;
    }

    public boolean compare(GDSmithResultSet secondGDSmithResultSet, boolean withOrder) {
        if (getRowNum() == 0 || secondGDSmithResultSet.getRowNum() == 0) {
            if (getRowNum() == secondGDSmithResultSet.getRowNum()) {
                return true;
            } else {
                return false;
            }
        }
        if (getRowNum() != secondGDSmithResultSet.getRowNum()) {
            return false;
        }

        List<String> firstSortList = new ArrayList<>(resultToStringList());
        List<String> secondSortList = new ArrayList<>(secondGDSmithResultSet.resultToStringList());

        if (!withOrder) {
            Collections.sort(firstSortList);
            Collections.sort(secondSortList);
        }
        return firstSortList.equals(secondSortList);
    }

    public boolean compareWithOrder(GDSmithResultSet secondGDSmithResultSet) {
        return compare(secondGDSmithResultSet, true);
    }
    public boolean compareWithOutOrder(GDSmithResultSet secondGDSmithResultSet) {
        return compare(secondGDSmithResultSet, false);
    }

    public GDSmithResultSet(org.neo4j.driver.Result rs) {
        List<Record> resultList = rs.list();
        resultRowNum = resultList.size();
        result = new ArrayList<Map<String, Object>>();

        for (Record x : resultList) {
            Map<String, Object> m = x.asMap();
            // System.out.println(m);
            result.add(m);
        }
        // System.out.println("finish parse!");
        System.out.println("result_size=" + resultRowNum);
    }

    public GDSmithResultSet(ResultSet rs) throws SQLException {
        resultRowNum = 0;
        result = new ArrayList<Map<String, Object>>();

        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<String, Object>(columns);
            for(int i = 1; i <= columns; ++i){
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            resultRowNum++;
            // System.out.println(row);
            result.add(row);
        }
        System.out.println("result_size=" + resultRowNum);
    }

    public GDSmithResultSet(com.redislabs.redisgraph.ResultSet rs) throws SQLException {

        resultRowNum = 0;
        result = new ArrayList<Map<String, Object>>();
        while (rs.hasNext()) {
            com.redislabs.redisgraph.Record r = rs.next();
            Map<String, Object> row = new HashMap<String, Object>();
            for (String k : r.keys()) {
                row.put(k, r.getValue(k));
            }
            resultRowNum++;
            // System.out.println(row);
            result.add(row);
        }
        System.out.println("result_size=" + resultRowNum);
    }

    public GDSmithResultSet(redis.clients.jedis.graph.ResultSet rs) {
        resultRowNum = rs.size();
        result = new ArrayList<>();
        for (redis.clients.jedis.graph.Record r : rs) {
            Map<String, Object> row = new HashMap<String, Object>();
            for (String k : r.keys()) {
                row.put(k, r.getValue(k));
            }
            result.add(row);
        }
        System.out.println("result_size=" + resultRowNum);
    }

    public GDSmithResultSet(List<Map<String, Object>> gremlinResults) {
        resultRowNum = gremlinResults.size();
        result = gremlinResults;

        System.out.println("result_size=" + resultRowNum);
    }

    public GDSmithResultSet(String rs) {
        result = new ArrayList<>();
        if (rs.equals("null")) {
            resultRowNum = 0;
        } else {
            if(!(rs.startsWith("[") && rs.endsWith("]"))) {
                rs = "[" + rs + "]";
            }
            List<Map> maps = JSONArray.parseArray(rs, Map.class);
            resultRowNum = maps.size();

            for(Map m : maps) {
                Map<String, Object> row = new HashMap<String, Object>();
                for (Object k : m.keySet()) {
                    row.put(k.toString(), m.get(k.toString()));
                }
                result.add(row);
            }
        }
        System.out.println("result_size=" + resultRowNum);
    }

    public int getRowNum() {
        return resultRowNum;
    }

    @Override
    public void close() {

    }

    public void registerEpilogue(Runnable runnableEpilogue) {

    }

    public boolean next() throws SQLException {
        // TODO: refactor me
        return true;
    }

    public String getString(int i) throws SQLException {
        // TODO: refactor me
        return "zzz";
    }

    public long getLong(int i) throws SQLException {
        // TODO: refactor me
        return 1;
    }

    public boolean isClosed() throws SQLException {
        // TODO: not sure how to handle this method
        return true;
    }

}
