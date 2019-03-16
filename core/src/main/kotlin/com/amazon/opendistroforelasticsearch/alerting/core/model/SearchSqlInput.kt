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

package com.amazon.opendistroforelasticsearch.alerting.core.model

import org.elasticsearch.common.CheckedFunction
import org.elasticsearch.common.ParseField
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

data class SearchSqlInput(val outputFormat: String, val sqlQuery: String) : Input {
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
                .startObject(SEARCH_SQL_FIELD)
                .field(OUTPUT_FORMAT_FIELD, outputFormat)
                .field(SQL_QUERY_FIELD, sqlQuery)
                .endObject()
                .endObject()
    }

    override fun name(): String {
        return SEARCH_SQL_FIELD
    }

    companion object {
        const val OUTPUT_FORMAT_FIELD = "output_format"
        const val SQL_QUERY_FIELD = "sql_query"
        const val SEARCH_SQL_FIELD = "search_sql"

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(Input::class.java,
            ParseField(SEARCH_SQL_FIELD), CheckedFunction { parseInner(it) })

        @JvmStatic @Throws(IOException::class)
        private fun parseInner(xcp: XContentParser): SearchSqlInput {
            var outputFormat: String? = null
            var sqlQuery: String? = null

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp::getTokenLocation)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    OUTPUT_FORMAT_FIELD -> {
                        outputFormat = xcp.textOrNull()
                    }
                    SQL_QUERY_FIELD -> {
                        sqlQuery = xcp.textOrNull()
                    }
                }
            }

            return SearchSqlInput(requireNotNull(outputFormat) { "SearchSqlInput $OUTPUT_FORMAT_FIELD is null" },
                    requireNotNull(sqlQuery) { "SearchInput $SQL_QUERY_FIELD is null" })
        }
    }
}
