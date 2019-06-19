// Copyright 2019 Verizon Media Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.config.luthier.factories

import com.yahoo.bard.webservice.application.ObjectMappersSuite
import com.yahoo.bard.webservice.config.luthier.LuthierIndustrialPark
import com.yahoo.bard.webservice.data.config.metric.makers.ArithmeticMaker
import com.yahoo.bard.webservice.data.config.metric.makers.MetricMaker
import com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation

import com.fasterxml.jackson.databind.node.ObjectNode

import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper

import spock.lang.Specification

class ArithmeticMakerFactorySpec extends Specification {

    LuthierIndustrialPark luthierIndustrialPark = new LuthierIndustrialPark.Builder().build()

    String plusConfig = """
{
  "arithmeticPLUS": {
    "type": "ArithmeticMaker",
    "operation": "PLUS"
  }
}
"""

    ObjectMapper objectReader = new ObjectMappersSuite().mapper

    def "Test creating factory from JSON string"() {
        setup: "Build a factory"
        JsonNode jsonNode = objectReader.readTree(plusConfig);

        Factory<MetricMaker> factory = new ArithmeticMakerFactory()

        ArithmeticMaker expected = new ArithmeticMaker(
                luthierIndustrialPark.getMetricDictionary(),
                ArithmeticPostAggregation.ArithmeticPostAggregationFunction.PLUS
        )

        when:
        ArithmeticMaker actual = factory.build("arithmeticPLUS", (ObjectNode) plusConfig.get("arithmeticPLUS"))

        then: "parse plusConfig"
        actual.function == expected.function
    }
}
