package org.example.gdsmith;

public interface GDSmithDBConnection extends AutoCloseable {

    String getDatabaseVersion() throws Exception;
}
