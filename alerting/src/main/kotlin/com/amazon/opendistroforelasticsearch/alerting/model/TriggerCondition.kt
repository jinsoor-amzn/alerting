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
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptType
import java.io.IOException

data class TriggerCondition(val script: Script?, val aggregation: TriggerSQLAggregations?) : ToXContentObject {

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
        if (script != null) builder.field(SCRIPT_FIELD, script)
        if (aggregation != null) builder.field(SQL_AGGREGATIONS_FIELD, aggregation)
        builder.endObject()
        return builder
    }

    companion object {
        const val SCRIPT_FIELD = "script"
        const val SQL_AGGREGATIONS_FIELD = "sql_aggregations"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): TriggerCondition {
            var script: Script? = null
            var aggregation: TriggerSQLAggregations? = null
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp::getTokenLocation)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val conditionFieldName = xcp.currentName()
                xcp.nextToken()
                when (conditionFieldName) {
                    SCRIPT_FIELD -> {
                        lateinit var source: String
                        lateinit var lang: String
                        while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                            val scriptField = xcp.currentName()
                            xcp.nextToken()
                            when (scriptField) {
                                Script.SOURCE_PARSE_FIELD.preferredName -> source = xcp.text()
                                Script.LANG_PARSE_FIELD.preferredName -> lang = xcp.text()
                            }
                        }
                        script = Script(ScriptType.INLINE,
                            requireNotNull(lang) { "Trigger ${Script.LANG_PARSE_FIELD.preferredName} is null" },
                            requireNotNull(source) { "Trigger ${Script.SOURCE_PARSE_FIELD.preferredName} is null" },
                            mapOf(),
                            mapOf())
                    }
                    SQL_AGGREGATIONS_FIELD -> {
                        aggregation = TriggerSQLAggregations.parse(xcp)
                    }
                }
            }

            return TriggerCondition(script, aggregation)
        }
    }
}
