package com.pearson.statsagg.globals;

import com.pearson.statsagg.utilities.StackTrace;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseConnections {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnections.class.getName());

    protected static BoneCP connectionPool_ = null;
    private static String jdbc_ = null;
    
    public static void setDriver() {
        String driver = null;
        
        try {      
            if (DatabaseConfiguration.getType() == DatabaseConfiguration.DERBY_EMBEDDED) {
                driver = "org.apache.derby.jdbc.EmbeddedDriver";
                Class.forName(driver).newInstance();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
    }
    
    public static void createConnectionPool() {
 	BoneCPConfig config = new BoneCPConfig();
        config.setMaxConnectionsPerPartition(DatabaseConfiguration.getCpMaxConnections());
        config.setStatisticsEnabled(DatabaseConfiguration.isCpEnableStatistics());
        config.setDisableConnectionTracking(true); // set this to true to avoid bonecp closing connections erroniously
        
 	config.setJdbcUrl(jdbc_);
        config.setUsername(DatabaseConfiguration.getUsername());
        config.setPassword(DatabaseConfiguration.getPassword());
        config.setDefaultAutoCommit(DatabaseConfiguration.getDefaultAutoCommit());
 
        if (ApplicationConfiguration.isDebugModeEnabled()) {
            config.setCloseConnectionWatch(true);
            config.setCloseConnectionWatchTimeout(10, TimeUnit.SECONDS);
            config.setDetectUnresolvedTransactions(true);
            config.setDetectUnclosedStatements(true);
            config.setCloseOpenStatements(false);
            config.setDisableConnectionTracking(false); 
        }
        
        try {
            connectionPool_ = new BoneCP(config);
        }
        catch (Exception e) {
            connectionPool_ = null;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
    }
    
    public static Connection getConnection() {
        
        if ((jdbc_ == null) || (connectionPool_ == null)) {
            return null;
        }
        
        Connection connection = null;
        
        try {           
            connection = connectionPool_.getConnection();
            return connection;
        }
        catch (Exception e) {
            connection = null;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            
            return connection;
        }
        
    }

    public static boolean disconnect(Connection connection) {
        
       try {
            if (connection != null) {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));            
        }
       
        try {
            if (connection != null) {
                connection.close();
            }
            
            connection = null;
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));            
            
            connection = null;
            return false;
        }
        
    }
    
    public static void disconnectAndShutdown() {

        try {
            if (connectionPool_ != null) {
                connectionPool_.close();
                connectionPool_ = null;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        if (DatabaseConfiguration.getType() == DatabaseConfiguration.DERBY_EMBEDDED) {
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true;deregister=true");
            }
            catch (Exception e) {
                if (e.toString().contains("java.sql.SQLException: Derby system shutdown")) {
                    logger.info("Database successfully shutdown");
                }
                else if (e.toString().contains("No suitable driver found for jdbc:derby")) {
                    logger.info("No suitable driver found for jdbc:derby");
                }
                else {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        
    }

    public static String getJdbc() {
        return jdbc_;
    }

    public static void setJdbc(String jdbc) {
        jdbc_ = jdbc;
    }

}
