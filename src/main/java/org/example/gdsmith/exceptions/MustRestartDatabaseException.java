package org.example.gdsmith.exceptions;

public class MustRestartDatabaseException extends RuntimeException{

    public MustRestartDatabaseException(Exception cause){
        super(cause);
    }
}
