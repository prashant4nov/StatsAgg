package com.pearson.statsagg.database.alerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertsSql {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertsSql.class.getName());
    
    protected final static String DropTable_Alerts = 
                    "DROP TABLE ALERTS";
    
    protected final static String CreateTable_Alerts =  
                    "CREATE TABLE ALERTS (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "NAME VARCHAR(32000) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(32000) NOT NULL, " + 
                    "DESCRIPTION VARCHAR(32000), " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL, " +
                    "NOTIFICATION_GROUP_ID INTEGER, " +
                    "IS_ENABLED BOOLEAN NOT NULL, " +
                    "ALERT_ON_POSITIIVE BOOLEAN NOT NULL, " + 
                    "ALLOW_RESEND_ALERT BOOLEAN NOT NULL, " + 
                    "SEND_ALERT_EVERY_NUM_MILLISECONDS INTEGER, " + 
                    "CAUTION_ALERT_TYPE INTEGER, " +
                    "CAUTION_OPERATOR INTEGER, " + 
                    "CAUTION_COMBINATION INTEGER, " + 
                    "CAUTION_COMBINATION_COUNT INTEGER, " + 
                    "CAUTION_THRESHOLD DECIMAL(31,7), " + 
                    "CAUTION_WINDOW_DURATION INTEGER, " + 
                    "CAUTION_MINIMUM_SAMPLE_COUNT INTEGER, " + 
                    "IS_CAUTION_ALERT_ACTIVE BOOLEAN NOT NULL, " +
                    "CAUTION_ALERT_LAST_SENT_TIMESTAMP TIMESTAMP, " +
                    "CAUTION_ACTIVE_ALERTS_SET CLOB, " +
                    "DANGER_ALERT_TYPE INTEGER, " +
                    "DANGER_OPERATOR INTEGER, " + 
                    "DANGER_COMBINATION INTEGER, " + 
                    "DANGER_COMBINATION_COUNT INTEGER, " + 
                    "DANGER_THRESHOLD DECIMAL(31,7), " + 
                    "DANGER_WINDOW_DURATION INTEGER, " + 
                    "DANGER_MINIMUM_SAMPLE_COUNT INTEGER, " + 
                    "IS_DANGER_ALERT_ACTIVE BOOLEAN NOT NULL, " +
                    "DANGER_ALERT_LAST_SENT_TIMESTAMP TIMESTAMP, " +
                    "DANGER_ACTIVE_ALERTS_SET CLOB " +
                    ")";
    
    protected final static String CreateIndex_Alerts_PrimaryKey =
                    "ALTER TABLE ALERTS " +
                    "ADD CONSTRAINT A_PK PRIMARY KEY (ID)";
    
    protected final static String CreateIndex_Alerts_Unique_Name =
                    "ALTER TABLE ALERTS ADD CONSTRAINT A_U_NAME UNIQUE (" + 
                    "NAME" + 
                    ")";
    
    protected final static String CreateIndex_Alerts_Unique_UppercaseName =
                    "ALTER TABLE ALERTS ADD CONSTRAINT A_U_UPPERCASE_NAME UNIQUE (" + 
                    "UPPERCASE_NAME" + 
                    ")";
    
    protected final static String CreateIndex_Alerts_ForeignKey_MetricGroupId =
                    "ALTER TABLE ALERTS " +
                    "ADD CONSTRAINT A_MGID_FK FOREIGN KEY (METRIC_GROUP_ID) " + 
                    "REFERENCES METRIC_GROUPS(ID)";
    
    protected final static String CreateIndex_Alerts_ForeignKey_NotificationGroupId =
                    "ALTER TABLE ALERTS " +
                    "ADD CONSTRAINT A_N_FK FOREIGN KEY (NOTIFICATION_GROUP_ID) " + 
                    "REFERENCES NOTIFICATION_GROUPS(ID)";
    
    protected final static String Select_Alert_ByPrimaryKey = 
                    "SELECT * FROM ALERTS " +
                    "WHERE ID = ?";
    
    protected final static String Select_Alert_ByName = 
                    "SELECT * FROM ALERTS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_Alert_Names = 
                    "SELECT NAME FROM ALERTS " +
                    "WHERE NAME LIKE ?";
    
    protected final static String Select_AllAlerts = 
                    "SELECT * FROM ALERTS";
    
    protected final static String Select_DistinctMetricGroupIds = 
                    "SELECT DISTINCT(METRIC_GROUP_ID) FROM ALERTS";
    
    protected final static String Select_DistinctNotificationGroupIds = 
                    "SELECT DISTINCT(NOTIFICATION_GROUP_ID) FROM ALERTS";
    
    protected final static String Insert_Alert =
                    "INSERT INTO ALERTS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION, METRIC_GROUP_ID, NOTIFICATION_GROUP_ID, IS_ENABLED, ALERT_ON_POSITIIVE, ALLOW_RESEND_ALERT, SEND_ALERT_EVERY_NUM_MILLISECONDS, " + 
                    "CAUTION_ALERT_TYPE, CAUTION_OPERATOR, CAUTION_COMBINATION, CAUTION_COMBINATION_COUNT, CAUTION_THRESHOLD, CAUTION_WINDOW_DURATION, " +
                    "CAUTION_MINIMUM_SAMPLE_COUNT, IS_CAUTION_ALERT_ACTIVE, CAUTION_ALERT_LAST_SENT_TIMESTAMP, CAUTION_ACTIVE_ALERTS_SET, " + 
                    "DANGER_ALERT_TYPE, DANGER_OPERATOR, DANGER_COMBINATION, DANGER_COMBINATION_COUNT, DANGER_THRESHOLD, DANGER_WINDOW_DURATION, " +
                    "DANGER_MINIMUM_SAMPLE_COUNT, IS_DANGER_ALERT_ACTIVE, DANGER_ALERT_LAST_SENT_TIMESTAMP, DANGER_ACTIVE_ALERTS_SET) " + 
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    protected final static String Update_Alert_ByPrimaryKey =
                    "UPDATE ALERTS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ?, METRIC_GROUP_ID = ?, NOTIFICATION_GROUP_ID = ?, IS_ENABLED = ?, ALERT_ON_POSITIIVE = ?, " +
                    "ALLOW_RESEND_ALERT = ?, SEND_ALERT_EVERY_NUM_MILLISECONDS = ?, " +
                    "CAUTION_ALERT_TYPE = ?, CAUTION_OPERATOR = ?, CAUTION_COMBINATION = ?, CAUTION_COMBINATION_COUNT = ?, CAUTION_THRESHOLD = ?, CAUTION_WINDOW_DURATION = ?, " +
                    "CAUTION_MINIMUM_SAMPLE_COUNT = ?, IS_CAUTION_ALERT_ACTIVE = ?, CAUTION_ALERT_LAST_SENT_TIMESTAMP = ?, CAUTION_ACTIVE_ALERTS_SET = ?, " +
                    "DANGER_ALERT_TYPE = ?, DANGER_OPERATOR = ?, DANGER_COMBINATION = ?, DANGER_COMBINATION_COUNT = ?, DANGER_THRESHOLD = ?, DANGER_WINDOW_DURATION = ?, " +
                    "DANGER_MINIMUM_SAMPLE_COUNT = ?, IS_DANGER_ALERT_ACTIVE = ?, DANGER_ALERT_LAST_SENT_TIMESTAMP = ?, DANGER_ACTIVE_ALERTS_SET = ?" +
                    "WHERE ID = ?";
    
    protected final static String Delete_Alert_ByPrimaryKey =
                    "DELETE FROM ALERTS " +
                    "WHERE ID = ?";
    
}
