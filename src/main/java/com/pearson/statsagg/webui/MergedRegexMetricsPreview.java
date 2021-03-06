package com.pearson.statsagg.webui;

import com.pearson.statsagg.globals.GlobalVariables;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
import static com.pearson.statsagg.webui.CreateMetricGroup.getMetricGroupNewlineDelimitedParameterValues;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "MergedRegexMetricsPreview", urlPatterns = {"/MergedRegexMetricsPreview"})
public class MergedRegexMetricsPreview extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MergedRegexMetricsPreview.class.getName());
    
    public static final String PAGE_NAME = "Merged Regex - Metrics Preview";
    
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
        
        TreeSet<String> matchRegexes = getMetricGroupNewlineDelimitedParameterValues(request, "MatchRegexes");
        TreeSet<String> blacklistRegexes = getMetricGroupNewlineDelimitedParameterValues(request, "BlacklistRegexes");
        
        List matchRegexes_List = null, blacklistRegexes_List = null;
        if ((matchRegexes != null) && !matchRegexes.isEmpty()) matchRegexes_List = new ArrayList<>(matchRegexes);
        if ((blacklistRegexes != null) && !blacklistRegexes.isEmpty()) blacklistRegexes_List = new ArrayList<>(blacklistRegexes);

        String mergedMatchRegex = StringUtilities.createMergedRegex(matchRegexes_List);
        String mergedBlacklistRegex = StringUtilities.createMergedRegex(blacklistRegexes_List);

        Set<String> matchMetricKeys = RegexTester.getRegexMatches(GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.keySet(), mergedMatchRegex, mergedBlacklistRegex, 1001);
        String regexMatchesHtml = RegexTester.getRegexMatchesHtml(matchMetricKeys, 1000);
            
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody =
            "<body>" +
            "  <div id=\"page-content-wrapper\">\n" +
            "    <!-- Keep all page content within the page-content inset div! -->\n" +
            "    <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "      <div class=\"statsagg_force_word_wrap\">" +
            regexMatchesHtml +
            "      </div>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "</body>";
            
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
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
    
}
