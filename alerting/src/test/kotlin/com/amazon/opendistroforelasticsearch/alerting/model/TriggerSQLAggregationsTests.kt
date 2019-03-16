/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.alerting.model

import org.elasticsearch.common.xcontent.LoggingDeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.test.ESTestCase
import org.junit.Assert
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class TriggerSQLAggregationsTests : ESTestCase() {
    val testString = "{\"match\":\"ALL\",\"conditions\":[{\"column_name\":\"AVG(latency)\",\"description\":" +
        "\"IS_BELOW\",\"value\":\"10\"}," +
        "{\"column_name\":\"Percentiles(CPU, 99.0)\",\"description\":\"IS_ABOVE\",\"value\":\"10\"}]}"
    fun `test simple`() {
        val testInput = ByteArrayInputStream(testString.toByteArray(StandardCharsets.UTF_8))
        val parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, testInput)
        parser.nextToken()
        val response = TriggerSQLAggregations.parse(parser)
        val bytesReference = XContentHelper.toXContent(response, XContentType.JSON, false)
        Assert.assertEquals(testString, bytesReference.utf8ToString())
        XContentHelper.convertToMap(bytesReference, false, XContentType.JSON).v2()
    }
}
