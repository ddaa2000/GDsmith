package org.example.gdsmith.exceptions;

public class DatabaseCrashException extends RuntimeException{

    private int index;

    public DatabaseCrashException(Exception e, int index){
        super("database "+index+" crashed",e);
    }
}
