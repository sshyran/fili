// Copyright 2019 Oath Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.config.luthier;

import com.yahoo.bard.webservice.data.config.ConfigurationLoader;
import com.yahoo.bard.webservice.data.config.LuthierResourceDictionaries;
import com.yahoo.bard.webservice.data.config.ResourceDictionaries;
import com.yahoo.bard.webservice.data.config.metric.makers.MetricMaker;
import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.data.dimension.DimensionDictionary;
import com.yahoo.bard.webservice.data.dimension.KeyValueStore;
import com.yahoo.bard.webservice.data.dimension.MapStore;
import com.yahoo.bard.webservice.data.dimension.SearchProvider;
import com.yahoo.bard.webservice.data.dimension.impl.LuceneSearchProvider;
import com.yahoo.bard.webservice.data.dimension.impl.NoOpSearchProvider;
import com.yahoo.bard.webservice.data.dimension.impl.ScanSearchProvider;
import com.yahoo.bard.webservice.data.metric.MetricDictionary;
import com.yahoo.bard.webservice.table.LogicalTableDictionary;
import com.yahoo.bard.webservice.table.PhysicalTableDictionary;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Dependency Injection container for Config Objects configured via Luthier.
 */
public class LuthierIndustrialPark implements ConfigurationLoader {

    private final LuthierResourceDictionaries resourceDictionaries;

    private final FactoryPark<Dimension> dimensionFactoryPark;
    private final FactoryPark<MetricMaker> metricMakerFactoryPark;

    /**
     * Constructor.
     *
     * @param resourceDictionaries  The dictionaries to initialize the industrial park with
     * @param dimensionFactories The map of factories for creating dimensions from external config
     */
    protected LuthierIndustrialPark(
            LuthierResourceDictionaries resourceDictionaries,
            Map<String, Factory<Dimension>> dimensionFactories,
            Map<String, Factory<MetricMaker>> metricMakerFactories
    ) {
        this.resourceDictionaries = resourceDictionaries;

        Supplier<ObjectNode> dimensionConfig = new ResourceNodeSupplier("DimensionConfig.json");
        dimensionFactoryPark = new FactoryPark<>(dimensionConfig, dimensionFactories);

        Supplier<ObjectNode> metricMakerConfig = new ResourceNodeSupplier("MetricMakerConfig.json");
        metricMakerFactoryPark = new FactoryPark<>(metricMakerConfig, metricMakerFactories);
    }

/*
    LogicalTable getLogicalTable(String tableName);
    PhysicalTable getPhysicalTable(String tableName);
    LogicalMetric getLogicalMetric(String metricName);
*/

    /**
     * Retrieve or build a dimension.
     *
     * @param dimensionName the name for the dimension to be provided.
     *
     * @return the dimension instance corresponding to this name.
     */
    public Dimension getDimension(String dimensionName) {
        DimensionDictionary dimensionDictionary = resourceDictionaries.getDimensionDictionary();
        if (dimensionDictionary.findByApiName(dimensionName) == null) {
            Dimension dimension = dimensionFactoryPark.buildEntity(dimensionName, this);
            dimensionDictionary.add(dimension);
        }
        return dimensionDictionary.findByApiName(dimensionName);
    }

    /**
     * Retrieve or build a dimension.
     *
     * @param metricMakerName the name for the dimension to be provided.
     *
     * @return the dimension instance corresponding to this name.
     */
    public MetricMaker getMetricMaker(String metricMakerName) {
        Map<String, MetricMaker> metricMakerDictionary = resourceDictionaries.getMetricMakerDictionary();
        if (metricMakerDictionary.get(metricMakerName) == null) {
            MetricMaker metricMaker = metricMakerFactoryPark.buildEntity(metricMakerName, this);
            metricMakerDictionary.put(metricMakerName, metricMaker);
        }
        return metricMakerDictionary.get(metricMakerName);
    }

    /**
     * Bare minimum that can work.
     */

    // TODO: Magic values!
    private int magicQueryweightlimit = 10000;
    private String magicLuceneindexpath = "path";
    private int magicMaxresults = 10000;

    /**
     * Bare minimum.
     *
     * @param searchProviderName identifier of the searchProvider
     * @return the searchProvider that is built from the identifier passed in
     */
    public SearchProvider getSearchProvider(String searchProviderName) {
        switch (searchProviderName) {
            case "com.yahoo.bard.webservice.data.dimension.impl.NoOpSearchProvider":
                return new NoOpSearchProvider(magicQueryweightlimit);
            case "com.yahoo.bard.webservice.data.dimension.impl.LuceneSearchProvider":
                return new LuceneSearchProvider(magicLuceneindexpath, magicMaxresults);
            default:
                return new ScanSearchProvider();
        }
    }

    /**
     * Bare minimum.
     *
     * @param keyValueStoreName identifier of the keyValueStore
     * @return the keyValueStore built according to the keyValueStore identifier
     * @throws UnsupportedOperationException when passed in redisStore.
     */
    public KeyValueStore getKeyValueStore(String keyValueStoreName) throws UnsupportedOperationException {
        switch (keyValueStoreName) {
            // TODO: Magic values!
            case "com.yahoo.bard.webservice.data.dimension.RedisStore":
                throw new UnsupportedOperationException(keyValueStoreName);
            default:
                return new MapStore();
        }
    }


    @Override
    public void load() {
        dimensionFactoryPark.fetchConfig().fieldNames().forEachRemaining(this::getDimension);
    }

    @Override
    public DimensionDictionary getDimensionDictionary() {
        return resourceDictionaries.getDimensionDictionary();
    }

    @Override
    public MetricDictionary getMetricDictionary() {
        return resourceDictionaries.getMetricDictionary();
    }

    @Override
    public LogicalTableDictionary getLogicalTableDictionary() {
        return resourceDictionaries.getLogicalDictionary();
    }

    @Override
    public PhysicalTableDictionary getPhysicalTableDictionary() {
        return resourceDictionaries.getPhysicalDictionary();
    }

    @Override
    public ResourceDictionaries getDictionaries() {
        return resourceDictionaries;
    }

    /**
     * Builder object to construct a new LuthierIndustrialPark instance with.
     */
    public static class Builder {

        private Map<ConceptType<?>,Map<String, ? extends Factory<? extends Object>>> conceptFactoryMap;

        //private Map<String, Factory<Dimension>> dimensionFactories;
        //private Map<String, Factory<MetricMaker>> metricMakers;

        private final LuthierResourceDictionaries resourceDictionaries;

        /**
         * Constructor.
         *
         * @param resourceDictionaries  a class that contains resource dictionaries including
         * PhysicalTableDictionary, DimensionDictionary, etc.
         */
        public Builder(LuthierResourceDictionaries resourceDictionaries) {
            this.resourceDictionaries = resourceDictionaries;
            conceptFactoryMap = new HashMap<>();
            conceptFactoryMap.put(ConceptType.METRIC_MAKER, new HashMap<>());
            conceptFactoryMap.put(ConceptType.DIMENSION, getDefaultDimensionFactories());
            if (true) {
                System.out.println("bar");
            }
        }

        /**
         * Constructor.
         * <p>
         * Default to use an empty resource dictionary.
         */
        public Builder() {
            this(new LuthierResourceDictionaries());
        }

        public Map<String, Factory<Dimension>> getDefaultDimensionFactories() {
            return new LinkedHashMap<>();
        }

        /**
         * Registers factories for a give concept.
         * <p>
         * There should be one factory per construction pattern for a concept in the system.
         *
         * @param conceptType The configuration concept being configured (e.g. Dimension, Metric..)
         * @param factories  A mapping from names of factories to a factory that builds instances of that type
         *
         * @return the builder object
         */
        public <T> Builder withFactories(ConceptType<T> conceptType, Map<String, Factory<T>> factories) {
            conceptFactoryMap.put(conceptType, factories);
            return this;
        }

        /**
         * Registers a named factory with the Industrial Park Builder.
         * <p>
         * There should be one factory per construction pattern for a concept in the system.
         *
         * @param name  The identifier used in the configuration to identify the type of
         * dimension built by this factory
         * @param factory  A factory that builds Dimensions of the type named by {@code name}
         *
         * @return the builder object
         */
        @SuppressWarnings("unchecked")
        public <T> Builder withFactory(ConceptType<T> conceptType, String name, Factory<T> factory) {
            Map<String, Factory<T>> conceptFactory = (Map<String, Factory<T>>) conceptFactoryMap.get(conceptType);
            conceptFactory.put(name, factory);
            return this;
        }
        /**
         * Builds a LuthierIndustrialPark.
         *
         * @return the LuthierIndustrialPark with the specified resourceDictionaries and factories
         */
        @SuppressWarnings("unchecked")
        public LuthierIndustrialPark build() {
            return new LuthierIndustrialPark(
                    resourceDictionaries,
                    new LinkedHashMap<String, Factory<Dimension>>(
                            (Map<? extends String,? extends Factory<Dimension>>) conceptFactoryMap.get(
                                    ConceptType.DIMENSION
                            )
                    ),
                    new LinkedHashMap<String, Factory<MetricMaker>>(
                            (Map<? extends String, ? extends Factory<MetricMaker>>) conceptFactoryMap.get(
                                    ConceptType.METRIC_MAKER
                            )
                    )
            );
        }
    }
}
