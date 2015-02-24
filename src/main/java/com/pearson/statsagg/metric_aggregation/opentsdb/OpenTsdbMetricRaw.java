package com.pearson.statsagg.metric_aggregation.opentsdb;

import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbMetricRaw implements GraphiteMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbMetricRaw.class.getName());
    
    private Long hashKey_ = null;
    
    private final String metricKey_;
    private final String metricTimestamp_;
    private final String metricValue_;
    private final List<OpenTsdbTag> tags_;
    private Long metricReceivedTimestampInMilliseconds_ = null;
    
    private Long metricTimestampInMilliseconds_ = null;
    private BigDecimal metricValueBigDecimal_ = null;
    
    public OpenTsdbMetricRaw(String metricPath, String metricTimestamp, String metricValue, List<OpenTsdbTag> tags, Long metricReceivedTimestampInMilliseconds) {
        this.metricKey_ = metricPath;
        this.metricTimestamp_ = metricTimestamp;
        this.metricValue_ = metricValue;
        this.tags_ = tags;
        
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }
    
    public OpenTsdbMetricRaw(String metricPath, String metricTimestamp, String metricValue, List<OpenTsdbTag> tags, Long metricTimestampInMilliseconds, Long metricReceivedTimestampInMilliseconds) {
        this.metricKey_ = metricPath;
        this.metricTimestamp_ = metricTimestamp;
        this.metricValue_ = metricValue;
        this.tags_ = tags;
        
        this.metricTimestampInMilliseconds_ = metricTimestampInMilliseconds;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }    
    
    public BigDecimal createAndGetMetricValueBigDecimal() {
        
        try {
            if (metricValueBigDecimal_ == null) {
                metricValueBigDecimal_ = new BigDecimal(metricValue_);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricValueBigDecimal_ = null;
        }
        
        return metricValueBigDecimal_;
    }

    public Long createAndGetMetricTimestamp() {
        
        try {
            if ((metricTimestampInMilliseconds_ == null)) {
                if (metricTimestamp_.length() == 10) metricTimestampInMilliseconds_ = Long.valueOf(metricTimestamp_) * 1000;
                else if (metricTimestamp_.length() == 13) metricTimestampInMilliseconds_ = Long.valueOf(metricTimestamp_);
                else logger.warn("OpenTSDB metricKey=\"" + metricKey_ + "\" has an invalid timestamp: \"" + metricTimestamp_ + "\"");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricTimestampInMilliseconds_ = null;
        }
        
        return metricTimestampInMilliseconds_;
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append(metricKey_).append(" ").append(metricTimestamp_).append(" ").append(metricValue_).append(" ");

        if (tags_ != null) {
            for (int i = 0; i < tags_.size(); i++) {
                stringBuilder.append(tags_.get(i).getUnparsedTag());
                if ((i + 1) != tags_.size()) stringBuilder.append(" ");
            }
        }
        
        return stringBuilder.toString();
    }

    @Override
    public String getGraphiteFormatString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append(metricKey_).append(" ").append(metricValue_).append(" ").append(metricTimestamp_);
        
        return stringBuilder.toString();
    }

    public static OpenTsdbMetricRaw parseOpenTsdbMetricRaw(String unparsedMetric, long metricReceivedTimestampInMilliseconds) {
        
        if (unparsedMetric == null) {
            return null;
        }
        
        try {
            int metricPathIndexRange = unparsedMetric.indexOf(' ', 0);
            String metricPath = null;
            if (metricPathIndexRange > 0) {
                metricPath = unparsedMetric.substring(0, metricPathIndexRange);
            }

            int metricTimestampIndexRange = unparsedMetric.indexOf(' ', metricPathIndexRange + 1);
            String metricTimestamp = null;
            if (metricTimestampIndexRange > 0) {
                metricTimestamp = unparsedMetric.substring(metricPathIndexRange + 1, metricTimestampIndexRange);
            }

            int metricValueIndexRange = unparsedMetric.indexOf(' ', metricTimestampIndexRange + 1);
            String metricValue = null;
            if (metricValueIndexRange > 0) {
                metricValue = unparsedMetric.substring(metricTimestampIndexRange + 1, metricValueIndexRange);
            }
            
            String metricTags_String = unparsedMetric.substring(metricValueIndexRange + 1, unparsedMetric.length());
            List<OpenTsdbTag> openTsdbTags = OpenTsdbTag.parseRawTags(metricTags_String);
            
            if ((metricPath == null) || metricPath.isEmpty() || 
                    (metricValue == null) || metricValue.isEmpty() || 
                    (metricTimestamp == null) || metricTimestamp.isEmpty() || 
                    ((metricTimestamp.length() != 10) && (metricTimestamp.length() != 13))) {
                logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
                return null;
            }
            else {
                if ((openTsdbTags == null) || openTsdbTags.isEmpty()) {
                    logger.warn("No tags associated with metric: \"" + metricPath + "\"");
                }
                
                OpenTsdbMetricRaw openTsdbRaw = new OpenTsdbMetricRaw(metricPath.trim(), metricTimestamp.trim(), metricValue.trim(), openTsdbTags, metricReceivedTimestampInMilliseconds); 
                return openTsdbRaw;
            }
        }
        catch (Exception e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            return null;
        }
    }
    
    public static List<OpenTsdbMetricRaw> parseOpenTsdbMetricsRaw(String unparsedMetrics, long metricReceivedTimestampInMilliseconds) {
        
        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = new ArrayList();
            
        try {
            int currentIndex = 0;
            int newLineLocation = 0;

            while(newLineLocation != -1) {
                newLineLocation = unparsedMetrics.indexOf('\n', currentIndex);

                String unparsedMetric = null;

                if (newLineLocation == -1) {
                    unparsedMetric = unparsedMetrics.substring(currentIndex, unparsedMetrics.length());
                }
                else {
                    unparsedMetric = unparsedMetrics.substring(currentIndex, newLineLocation);
                    currentIndex = newLineLocation + 1;
                }

                if ((unparsedMetric != null) && !unparsedMetric.isEmpty()) {
                    OpenTsdbMetricRaw openTsdbMetricRaw = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric.trim(), metricReceivedTimestampInMilliseconds);

                    if (openTsdbMetricRaw != null) {
                        openTsdbMetricsRaw.add(openTsdbMetricRaw);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return openTsdbMetricsRaw;
    }
    
    /*
    For every unique metric key, get the OpenTsdbMetricRaw with the most recent 'metric timestamp'. 
    
    In the event that multiple OpenTsdbMetricRaws share the same 'metric key' and 'metric timestamp', 
    then 'metric received timestamp' is used as a tiebreaker. 
    
    In the event that multiple OpenTsdbMetricRaws also share the same 'metric received timestamp', 
    then this method will return the first OpenTsdbMetricRaw that it scanned that met these criteria
    */
    public static Map<String,OpenTsdbMetricRaw> getMostRecentOpenTsdbMetricRawByMetricKey(List<OpenTsdbMetricRaw> openTsdbMetricsRaw) {
        
        if (openTsdbMetricsRaw == null || openTsdbMetricsRaw.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, OpenTsdbMetricRaw> mostRecentOpenTsdbMetricsByMetricKey = new HashMap<>();

        for (OpenTsdbMetricRaw openTsdbMetricRaw : openTsdbMetricsRaw) {
            try {
                boolean doesAlreadyContainMetricKey = mostRecentOpenTsdbMetricsByMetricKey.containsKey(openTsdbMetricRaw.getMetricKey());

                if (doesAlreadyContainMetricKey) {
                    OpenTsdbMetricRaw currentMostRecentOpenTsdbMetricRaw = mostRecentOpenTsdbMetricsByMetricKey.get(openTsdbMetricRaw.getMetricKey());

                    if (openTsdbMetricRaw.getMetricTimestampInMilliseconds() > currentMostRecentOpenTsdbMetricRaw.getMetricTimestampInMilliseconds()) {
                        mostRecentOpenTsdbMetricsByMetricKey.put(openTsdbMetricRaw.getMetricKey(), openTsdbMetricRaw);
                    }
                    else if (openTsdbMetricRaw.getMetricTimestampInMilliseconds().equals(currentMostRecentOpenTsdbMetricRaw.getMetricTimestampInMilliseconds())) {
                        if (openTsdbMetricRaw.getMetricReceivedTimestampInMilliseconds() > currentMostRecentOpenTsdbMetricRaw.getMetricReceivedTimestampInMilliseconds()) {
                            mostRecentOpenTsdbMetricsByMetricKey.put(openTsdbMetricRaw.getMetricKey(), openTsdbMetricRaw);
                        }
                    }
                }
                else {
                    mostRecentOpenTsdbMetricsByMetricKey.put(openTsdbMetricRaw.getMetricKey(), openTsdbMetricRaw);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return mostRecentOpenTsdbMetricsByMetricKey;
    }
    
    public static List<OpenTsdbMetricRaw> createPrefixedOpenTsdbMetricsRaw(List<OpenTsdbMetricRaw> openTsdbMetricsRaw, 
            boolean useGlobalPrefix, String globalPrefixValue,
            boolean useOpenTsdbPrefix, String openTsdbPrefixValue) {
        
        if (openTsdbMetricsRaw == null || openTsdbMetricsRaw.isEmpty()) {
            return new ArrayList<>();
        }
        
        int prefixedOpenTsdbMetricsRawListInitialSize = (int) (openTsdbMetricsRaw.size() * 1.3);
        List<OpenTsdbMetricRaw> prefixedOpenTsdbMetricsRaw = new ArrayList<>(prefixedOpenTsdbMetricsRawListInitialSize);
        
        for (OpenTsdbMetricRaw openTsdbMetricRaw : openTsdbMetricsRaw) {
            String prefixedMetricPath = createPrefixedOpenTsdbMetricPath(openTsdbMetricRaw, useGlobalPrefix, globalPrefixValue, 
                    useOpenTsdbPrefix, openTsdbPrefixValue);
            
            OpenTsdbMetricRaw prefixedOpenTsdbMetricRaw = new OpenTsdbMetricRaw(prefixedMetricPath, openTsdbMetricRaw.getMetricTimestamp(), 
                    openTsdbMetricRaw.getMetricValue(), openTsdbMetricRaw.getTags(), openTsdbMetricRaw.getMetricTimestampInMilliseconds(), 
                    openTsdbMetricRaw.getMetricReceivedTimestampInMilliseconds());

            prefixedOpenTsdbMetricRaw.setHashKey(openTsdbMetricRaw.getHashKey());
            
            prefixedOpenTsdbMetricsRaw.add(prefixedOpenTsdbMetricRaw);
        }
        
        return prefixedOpenTsdbMetricsRaw;
    }
    
    public static String createPrefixedOpenTsdbMetricPath(OpenTsdbMetricRaw openTsdbMetricRaw, 
            boolean useGlobalPrefix, String globalPrefixValue,
            boolean useOpenTsdbPrefix, String openTsdbPrefixValue) {
        
        if (openTsdbMetricRaw == null) {
            return null;
        }
        
        if (!useGlobalPrefix && !useOpenTsdbPrefix) {
            return openTsdbMetricRaw.getMetricKey();
        }
        
        StringBuilder prefix = new StringBuilder("");
        
        if (useGlobalPrefix) prefix.append(globalPrefixValue).append(".");
        if (useOpenTsdbPrefix) prefix.append(openTsdbPrefixValue).append(".");
        
        prefix.append(openTsdbMetricRaw.getMetricKey());
        
        return prefix.toString();
    }
    
    public Long getHashKey() {
        return this.hashKey_;
    }
    
    @Override
    public Long getMetricHashKey() {
        return getHashKey();
    }
    
    public void setHashKey(Long hashKey) {
        this.hashKey_ = hashKey;
    }
    
    @Override
    public String getMetricKey() {
        return metricKey_;
    }
    
    public String getMetricTimestamp() {
        return metricTimestamp_;
    }
    
    @Override
    public Long getMetricTimestampInMilliseconds() {
        return metricTimestampInMilliseconds_;
    }
    
    public String getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return createAndGetMetricValueBigDecimal();
    }

    public List<OpenTsdbTag> getTags() {
        return tags_;
    }
    
    @Override
    public Long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }

    public void setMetricReceivedTimestampInMilliseconds(Long metricReceivedTimestampInMilliseconds) {
        metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }

}
