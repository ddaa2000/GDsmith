package org.example.gdsmith.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.util.ArrayList;
import java.util.List;

public class Neo4jDriverManager {
    private static List<DriverInfo> registeredDrivers = new ArrayList<>();

    private static class DriverInfo{
        public Driver driver = null;
        public String url = "", username =  "", password = "";

        public DriverInfo(){
            this(null, "", "", "");
        }

        public DriverInfo(Driver driver, String url, String username, String password){
            this.driver = driver;
            this.url = url;
            this.username = username;
            this.password = password;
        }
    }

    public static Driver getDriver(String url, String username, String password){
        for(DriverInfo driverInfo : registeredDrivers){
            if(driverInfo.url.equals(url) && driverInfo.username.equals(username) && driverInfo.password.equals(password)){
                return driverInfo.driver;
            }
        }
        Driver driver = GraphDatabase.driver(url, AuthTokens.basic(username, password));
        registeredDrivers.add(new DriverInfo(driver, url, username, password));
        return driver;
    }

    public static void closeDriver(Driver driver){
        DriverInfo closedDriver = null;
        for(DriverInfo driverInfo : registeredDrivers){
            if(driverInfo.driver == driver){
                closedDriver = driverInfo;
                driver.close();
            }
        }
        registeredDrivers.remove(closedDriver);
    }
}
