package com.pearson.statsagg.globals;

import com.pearson.statsagg.controller.threads.AlertInvokerThread;
import com.pearson.statsagg.controller.threads.CleanupInvokerThread;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.gauges.Gauge;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v2;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetric;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetric;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetricAggregated;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jeffrey Schmidt
 */
public class GlobalVariables {

    // the prefixes (added on by StatsAgg) for the various types of metrics
    public static String graphiteAggregatedPrefix = "";
    public static String graphitePassthroughPrefix = "";
    public static String openTsdbPrefix = "";
    public static String influxdbPrefix = "";
        
    // the 'invoker' thread for the alert routine. this is global so that the webui can trigger the alert-routine.
    public static AlertInvokerThread alertInvokerThread = null;
    
    // the 'invoker' thread for the cleanup routine. this is global so that the webui can trigger the cleanup-routine.
    public static CleanupInvokerThread cleanupInvokerThread = null;
    
    // A flag indicating whether statsagg has finished going through its initialization routine. This will only be true if it has gone through the initialization routine successfully.
    public static AtomicBoolean isApplicationInitializeSuccess = new AtomicBoolean(false);
    
    // A flag that indicates whether the StatsAgg is using an in-memory database or not
    public static AtomicBoolean isStatsaggUsingInMemoryDatabase = new AtomicBoolean(false);
    
    // Used to determine how long StatsAgg has been running. Should be set by the initialization routine.
    public final static AtomicLong statsaggStartTimestamp = new AtomicLong(0);
    
    // Used to track how many metrics are flowing into StatsAgg
    public final static AtomicLong incomingMetricsCount = new AtomicLong(0);
    
    // Used to track how many metrics are flowing into StatsAgg
    public final static AtomicLong incomingMetricsRollingAverage = new AtomicLong(0);
    
    // Used to track how many metrics are known to StatsAgg that have a valid association & a stored datapoint
    public final static AtomicLong associatedMetricsWithValuesCount = new AtomicLong(0);
    
    // Used to generate hash keys for incoming metrics
    public final static AtomicLong metricHashKeyGenerator = new AtomicLong(Long.MIN_VALUE);

    // k="Value assigned at metric arrival from the appropriate 'MetricsHashKeyGenerator' object", v="metric object"
    public final static ConcurrentHashMap<Long,StatsdMetric> statsdNotGaugeMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,StatsdMetric> statsdGaugeMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,GraphiteMetric> graphiteAggregatorMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,GraphiteMetric> graphitePassthroughMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,OpenTsdbMetric> openTsdbMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,InfluxdbMetric_v1> influxdbV1Metrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,InfluxdbMetric_v2> influxdbV2Metrics = new ConcurrentHashMap<>();

    // k=MetricKey, v="Aggregated metric object"
    public final static ConcurrentHashMap<String,StatsdMetricAggregated> statsdMetricsAggregatedMostRecentValue = new ConcurrentHashMap<>(16, 0.75f, 3);

    // k=MetricKey, v=Gauge (kept in sync with the database)
    public final static ConcurrentHashMap<String,Gauge> statsdGaugeCache = new ConcurrentHashMap<>(16, 0.75f, 3);
    
    // k=MetricKey, v=MetricKey (k=v. The cleanup routine will cleanup these metrics ASAP (regardless of whether they're tracked an alert or not).
    public final static ConcurrentHashMap<String,String> immediateCleanupMetrics = new ConcurrentHashMap<>();
    
    // k=MetricGroupId, v="codes for "New, "Remove", "Alter" 
    public final static ConcurrentHashMap<Integer,Byte> metricGroupChanges = new ConcurrentHashMap<>();
            
    // k=MetricKey, v="The most timestamp that this metric was received by this program"
    public final static ConcurrentHashMap<String,Long> metricKeysLastSeenTimestamp = new ConcurrentHashMap<>(16, 0.75f, 6); 
    
    // k=MetricKey, v="The most timestamp that this metric was received by this program. Gets updated if the metric is configured to send 0 or previous value when no new metrics were received."
    public final static ConcurrentHashMap<String,Long> metricKeysLastSeenTimestamp_UpdateOnResend = new ConcurrentHashMap<>(16, 0.75f, 6); 
    
    // k=MetricGroupId, v=Set<MetricKey> "is the metric key associated with a specific metric group? only include in the set if the assocation/match is true.">
    public final static ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithMetricGroup = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v="Boolean for "is this metric key associated with ANY metric group"?
    public final static ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithAnyMetricGroup = new ConcurrentHashMap<>(); 
    
    // k=MetricGroupId, v=string representing a single, merged, match regex statement that is composed of the metric group's associated regexes
    public final static ConcurrentHashMap<Integer,String> mergedMatchRegexesForMetricGroups = new ConcurrentHashMap<>(); 
    
    // k=MetricGroupId, v=string representing a single, merged, blacklist regex statement that is composed of the metric group's associated regexes
    public final static ConcurrentHashMap<Integer,String> mergedBlacklistRegexesForMetricGroups = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v=List<MetricTimestampAndValue> (should be -- synchronizedList(ArrayList<MetricTimestampAndValue>()))
    public final static ConcurrentHashMap<String,List<MetricTimestampAndValue>> recentMetricTimestampsAndValuesByMetricKey = new ConcurrentHashMap<>(16, 0.75f, 6); 

    // k=MetricGroupRegex-pattern, v="MetricGroupRegex-pattern compiled pattern. This is a cache for compiled regex patterns."
    public final static ConcurrentHashMap<String,Pattern> metricGroupRegexPatterns = new ConcurrentHashMap<>(); 
    
    // k=MetricGroupRegex-pattern, v="MetricGroupRegex-pattern. If a regex pattern is bad (doesn't compile), then it is stored here so we don't try to recompile it."
    public final static ConcurrentHashMap<String,String> metricGroupRegexBlacklist = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=MetricKey
    public final static ConcurrentHashMap<Integer,List<String>> activeCautionAlertMetricKeysByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=MetricKey
    public final static ConcurrentHashMap<Integer,List<String>> activeDangerAlertMetricKeysByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=Alert
    public final static ConcurrentHashMap<Integer,Alert> pendingCautionAlertsByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=Alert
    public final static ConcurrentHashMap<Integer,Alert> pendingDangerAlertsByAlertId = new ConcurrentHashMap<>(); 

    // k=MetricKey, v=MetricKey
    public final static ConcurrentHashMap<String,String> activeAvailabilityAlerts = new ConcurrentHashMap<>();
   
    // k=AlertId, v=Set<MetricKey>
    public final static ConcurrentHashMap<Integer,Set<String>> activeCautionAvailabilityAlerts = new ConcurrentHashMap<>();
    
    // k=AlertId, v=Set<MetricKey>
    public final static ConcurrentHashMap<Integer,Set<String>> activeDangerAvailabilityAlerts = new ConcurrentHashMap<>();
    
    // k="{metricKey}-{alertId}", v='Alert routine calculated metric value'
    public final static ConcurrentHashMap<String,BigDecimal> activeCautionAlertMetricValues = new ConcurrentHashMap<>(); 
    
    // k="{metricKey}-{alertId}", v='Alert routine calculated metric value'
    public final static ConcurrentHashMap<String,BigDecimal> activeDangerAlertMetricValues = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v='is alert suspended (as of last alert routine run)?'
    public final static ConcurrentHashMap<Integer,Boolean> alertSuspensionStatusByAlertId = new ConcurrentHashMap<>();
    
    // k=AlertId, v=alert suspension ids that are currently associated with a specific alert
    public final static ConcurrentHashMap<Integer,Set<Integer>> alertSuspensionIdAssociationsByAlertId = new ConcurrentHashMap<>();
    
    // k=AlertId, v=the alert suspension level (ALERT_NOT_SUSPENDED, SUSPEND_ALERT_NOTIFICATION_ONLY, SUSPEND_ENTIRE_ALERT)
    public final static ConcurrentHashMap<Integer,Integer> alertSuspensionLevelsByAlertId = new ConcurrentHashMap<>();
    
    // The timestamp of the last time the alert routine finished executing. This variable does not persist across application restarts.
    public final static AtomicLong alertRountineLastExecutedTimestamp = new AtomicLong(0);
    
    // Used to lock down the alert routine so that the alert routine can have exclusive access to the data structures it uses
    public final static Object alertRoutineLock = new Object();
    
    // Used to lock down the cleanup routine so that it doesn't clear out any metrics while the metric association routine is running
    public final static Object cleanupOldMetricsLock = new Object();

}
