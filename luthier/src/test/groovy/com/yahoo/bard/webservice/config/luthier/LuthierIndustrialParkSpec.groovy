// Copyright 2019 Verizon Media Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.config.luthier


import com.yahoo.bard.webservice.config.luthier.factories.KeyValueStoreDimensionFactory
import com.yahoo.bard.webservice.data.config.LuthierResourceDictionaries
import com.yahoo.bard.webservice.data.dimension.Dimension
import spock.lang.Specification

class LuthierIndustrialParkSpec extends Specification {
    LuthierIndustrialPark industrialPark
    void setup() {
        Map<String, Factory<Dimension>> dimensionFactoriesMap = new HashMap<>()
        dimensionFactoriesMap.put("KeyValueStoreDimension", new KeyValueStoreDimensionFactory())

        LuthierResourceDictionaries resourceDictionaries = new LuthierResourceDictionaries()
        industrialPark = (new LuthierIndustrialPark.Builder(resourceDictionaries))
                .withFactories(ConceptType.DIMENSION, dimensionFactoriesMap)
                .build()

    }

    def "An industrialPark instance is loaded up without error."() {
        when:
            industrialPark.load()
        then:
            true
    }
}
