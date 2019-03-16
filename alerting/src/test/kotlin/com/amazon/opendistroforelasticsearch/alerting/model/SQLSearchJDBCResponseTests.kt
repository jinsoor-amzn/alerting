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

import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.test.ESTestCase
import org.junit.Assert
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class SQLSearchJDBCResponseTests : ESTestCase() {
    val TEST = "{\"schema\":[{\"name\":\"title\",\"type\":\"text\"}," +
        "{\"name\":\"creationDate\",\"type\":\"date\"}],\"total\":7," +
        "\"datarows\":[[\"Selecting group of children with all the same element tag\",\"2012-10-11T00:00:40.137\"]," +
        "[\"Inserting error in an hashtable of queues as linked list?\",\"2012-10-11T00:00:43.030\"]," +
        "[\"Can I use volatile sig_atomic_t to avoid a mutex in C++03?\",\"2012-10-11T00:00:55.893\"]," +
        "[\"xml reader limit result\",\"2012-10-11T00:01:09.777\"]," +
        "[\"Incorrect Syntax in a Pivot\",\"2012-10-11T00:01:10.403\"]," +
        "[\"Android: How do I get the device DPI from native code\",\"2012-10-11T00:01:12.837\"]," +
        "[\"django urls regular expressions\",\"2012-10-11T00:01:20.807\"]]," +
        "\"size\":7,\"status\":200}"
    fun `test simple`() {
        val testInput = ByteArrayInputStream(TEST.toByteArray(StandardCharsets.UTF_8))
        val parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, testInput)
        val response = SQLSearchJDBCResponse.parse(parser)
        val bytesReference = XContentHelper.toXContent(response, XContentType.JSON, false)
        Assert.assertEquals(TEST, bytesReference.utf8ToString())
        val result1 = XContentHelper.convertToMap(bytesReference, false, XContentType.JSON).v2()
        val result2 = XContentHelper.convertToMap(BytesArray(TEST), false, XContentType.JSON).v2()
        Assert.assertEquals(result1, result2)
    }
}
