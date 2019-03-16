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
import java.util.Locale

data class TriggerSQLAggregations(
    val match: MATCH,
    val triggerConditionSQLs: List<TriggerConditionSQL>
) : ToXContentObject {
    enum class MATCH { ALL, ANY }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(MATCH_FIELD, match.toString())
            .field(CONDITION_FIELD, triggerConditionSQLs)
            .endObject()
        return builder
    }

    companion object {
        const val MATCH_FIELD = "match"
        const val CONDITION_FIELD = "conditions"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): TriggerSQLAggregations {
            lateinit var match: MATCH
            val triggerConditionSQLs = mutableListOf<TriggerConditionSQL>()
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp::getTokenLocation)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    MATCH_FIELD -> match = validateMatchField(xcp.text())
                    CONDITION_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, xcp.currentToken(), xcp::getTokenLocation)
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            triggerConditionSQLs.add(TriggerConditionSQL.parse(xcp))
                        }
                    }
                }
            }
            return TriggerSQLAggregations(requireNotNull(match) { "SQL Aggregation $MATCH_FIELD is null" },
                requireNotNull(triggerConditionSQLs) { "SQL Aggregation $CONDITION_FIELD is null" })
        }

        private fun validateMatchField(matchField: String): MATCH {
            return MATCH.valueOf(matchField.toUpperCase(Locale.US))
        }
    }
}
