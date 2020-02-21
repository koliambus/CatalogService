package com.packtpub.monitoring;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class CloudwatchMetricsEmitter {

    private static final String ENVIRONMENT_DIMENSION_NAME = "ENV";

    private final AmazonCloudWatch cloudWatchClient;

    private final String environmentName;

    public CloudwatchMetricsEmitter(
            AmazonCloudWatch cloudWatchClient,
            String environmentName) {
        this.cloudWatchClient = cloudWatchClient;
        this.environmentName = environmentName;
    }

    public void emitMetric(final String metricNamespace, final String metricName, double value) {
        Dimension dimension = new Dimension()
                .withName(ENVIRONMENT_DIMENSION_NAME)
                .withValue(environmentName);

        MetricDatum datum = new MetricDatum()
                .withMetricName(metricName)
                .withUnit(StandardUnit.None)
                .withValue(value)
                .withDimensions(dimension);

        PutMetricDataRequest request = new PutMetricDataRequest()
                .withNamespace(metricNamespace)
                .withMetricData(datum);

        cloudWatchClient.putMetricData(request);
    }
}
