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

import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

data class SQLSearchJDBCResponse(
    val schema: List<SQLSearchJDBCResponseSchema>,
    val total: Int,
    val datarows: List<List<String?>>,
    val size: Int,
    val status: Int
) : ToXContentObject {

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(SCHEMA, schema.toTypedArray())
            .field(TOTAL, total)
            .field(DATAROWS, datarows.toTypedArray())
            .field(SIZE, size)
            .field(STATUS, status)
            .endObject()
        return builder
    }

    companion object {
        val SCHEMA: String = "schema"
        val TOTAL: String = "total"
        val DATAROWS: String = "datarows"
        val SIZE: String = "size"
        val STATUS: String = "status"

        @Throws(IOException::class)
        fun parse(xcp: XContentParser): SQLSearchJDBCResponse {
            val schemas: MutableList<SQLSearchJDBCResponseSchema> = mutableListOf()
            var total: Int? = null
            val datarows: MutableList<MutableList<String?>> = mutableListOf()
            var size: Int? = null
            var status: Int? = null

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp::getTokenLocation)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()

                xcp.nextToken()
                when (fieldName) {
                    SCHEMA -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp::getTokenLocation)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            schemas.add(SQLSearchJDBCResponseSchema.parse(xcp))
                        }
                    }
                    TOTAL -> total = xcp.intValue()
                    DATAROWS -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp::getTokenLocation)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            val row: MutableList<String?> = mutableListOf()
                            XContentParserUtils.ensureExpectedToken(
                                XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp::getTokenLocation)
                            while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                                row.add(xcp.textOrNull())
                            }
                            datarows.add(row)
                        }
                    }
                    SIZE -> size = xcp.intValue()
                    STATUS -> status = xcp.intValue()
                }
            }
            return SQLSearchJDBCResponse(
                requireNotNull(schemas) { "$SCHEMA name is null" },
                requireNotNull(total) { "$TOTAL name is null" },
                requireNotNull(datarows) { "$DATAROWS name is null" },
                requireNotNull(size) { "$SIZE name is null" },
                requireNotNull(status) { "$STATUS name is null" }
            )
        }
    }
}
