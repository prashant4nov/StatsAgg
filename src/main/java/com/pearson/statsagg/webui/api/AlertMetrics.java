package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.alerts.AlertThread;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name="API_AlertMetrics", urlPatterns={"/api/alert-metrics"})
public class AlertMetrics extends HttpServlet {
   
    private static final Logger logger = LoggerFactory.getLogger(AlertMetrics.class.getName());
    
    public static final String PAGE_NAME = "API_AlertMetrics";
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        response.setContentType("application/json");
        PrintWriter out = null;

        try {  
            Alert alert = getAlert(request);
            MetricGroup metricGroup = getMetricGroup(alert);
            String json = createJson(alert, metricGroup);            

            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        }
    }
    
    private Alert getAlert(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        AlertsDao alertsDao = new AlertsDao(false);
        Alert alert = null;

        try {
            // should be equal to either "AlertName" or "AlertId"
            String queryField = request.getParameter("QueryField");

            if ((queryField != null) && queryField.equalsIgnoreCase("AlertName")) {
                String alertName = request.getParameter("AlertName");
                alert = alertsDao.getAlertByName(alertName);
            }
            else if ((queryField != null) && queryField.equalsIgnoreCase("AlertId")) {
                String alertId = request.getParameter("AlertId");
                alert = alertsDao.getAlert(Integer.valueOf(alertId));
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    
        alertsDao.close();
        
        return alert;
    }
    
    private MetricGroup getMetricGroup(Alert alert) {

        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return null;
        }
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroup = metricGroupsDao.getMetricGroup(alert.getMetricGroupId());
                
        return metricGroup;
    }
    
    private String createJson(Alert alert, MetricGroup metricGroup) {
        
        if ((alert == null) || (alert.getName() == null) || (alert.getMetricGroupId() == null) ||
                (metricGroup == null) || (metricGroup.getId() == null) || (metricGroup.getName() == null)) {
            return "[]";
        }

        StringBuilder json = new StringBuilder();
        
        json.append("[{");
        json.append("\"Alert_Name\":\"").append(StringEscapeUtils.escapeJson(alert.getName())).append("\",");
        json.append("\"Alert_Id\":").append(alert.getId()).append(",");
        json.append("\"Metric_Group_Name\":\"").append(StringEscapeUtils.escapeJson(metricGroup.getName())).append("\",");
        json.append("\"Metric_Group_Id\":").append(alert.getMetricGroupId()).append(",");
        
        
        json.append("\"Triggered_Caution_Metrics\":[");
        List<String> cautionTriggeredMetricKeys = AlertThread.getCautionTriggeredMetrics(alert);
        int i = 1;
        for (String metricKey : cautionTriggeredMetricKeys) {
            json.append("\"").append(StringEscapeUtils.escapeJson(metricKey)).append("\"");
            if (i < cautionTriggeredMetricKeys.size()) json.append(",");
            i++;
        }
        
        json.append("],");
        
        
        json.append("\"Triggered_Danger_Metrics\":[");
        List<String> dangerTriggeredMetricKeys = AlertThread.getDangerTriggeredMetrics(alert);
        i = 1;
        for (String metricKey : dangerTriggeredMetricKeys) {
            json.append("\"").append(StringEscapeUtils.escapeJson(metricKey)).append("\"");
            if (i < dangerTriggeredMetricKeys.size()) json.append(",");
            i++;
        }
        
        json.append("],");
        
        
        json.append("\"Metric_Group_Metrics\":[");
        Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(alert.getMetricGroupId());
        if (matchingMetricKeysAssociatedWithMetricGroup != null) {
            synchronized(matchingMetricKeysAssociatedWithMetricGroup) {
                i = 1;
                for (String metricKey : matchingMetricKeysAssociatedWithMetricGroup) {
                    json.append("\"").append(StringEscapeUtils.escapeJson(metricKey)).append("\"");
                    if (i < matchingMetricKeysAssociatedWithMetricGroup.size()) json.append(",");
                    i++;
                }
            }
        }
        
        json.append("]");
    
        json.append("}]");
        
        return json.toString();
    }
    
}
