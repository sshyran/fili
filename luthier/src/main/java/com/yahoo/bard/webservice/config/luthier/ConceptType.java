package com.yahoo.bard.webservice.config.luthier;

import com.yahoo.bard.webservice.data.config.metric.makers.MetricMaker;
import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.data.metric.LogicalMetric;

/**
 * Concepts represent the categories of things that have configuration files in Luthier, as well as the corresponding
 * factories for producing those types of objects.
 *
 * This is not an enum, because we don't want to block change.
 *
 * This is not an interface, because it doesn't need to be yet.
 */
public class ConceptType<T> {

    public static final ConceptType<Dimension> DIMENSION = new ConceptType<>("dimension");
    public static final ConceptType<LogicalMetric> METRIC = new ConceptType<>("logicalMetric");
    public static final ConceptType<MetricMaker> METRIC_MAKER = new ConceptType<>("metricMaker");

    String conceptKey;

    public ConceptType(String name) {
        this.conceptKey = name;
    }

    public String getConceptKey() {
        return conceptKey;
    }
}
