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

import com.amazon.opendistroforelasticsearch.alerting.model.trigger.SingleValueScript
import com.amazon.opendistroforelasticsearch.alerting.model.trigger.PredefinedCondition
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptType
import java.io.IOException
import java.util.Locale

data class TriggerConditionSQL(
    val column: String,
    val description: DESCRIPTION,
    val value: String,
    val script: Script
) : ToXContentObject {
    enum class DESCRIPTION { IS_BELOW, IS_ABOVE, IS_EXACTLY }
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(COLUMN_NAME_FIELD, column)
            .field(DESCRIPTION_FIELD, description)
            .field(VALUE_FIELD, value)
            .endObject()
        return builder
    }

    companion object {
        const val COLUMN_NAME_FIELD = "column_name"
        const val DESCRIPTION_FIELD = "description"
        const val VALUE_FIELD = "value"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): TriggerConditionSQL {
            lateinit var column: String
            lateinit var description: DESCRIPTION
            lateinit var value: String
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp::getTokenLocation)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val scriptField = xcp.currentName()
                xcp.nextToken()
                when (scriptField) {
                    COLUMN_NAME_FIELD -> column = xcp.text()
                    DESCRIPTION_FIELD -> description = validateDescriptionField(xcp.text())
                    VALUE_FIELD -> value = xcp.text()
                }
            }
            val script = Script(ScriptType.INLINE,
                Script.DEFAULT_SCRIPT_LANG,
                getStatScript(column, description, value).getPainless(),
                mapOf(),
                mapOf())
            return TriggerConditionSQL(column, description, value, script)
        }

        private fun getStatScript(column: String, description: DESCRIPTION, value: String): PredefinedCondition {
            return SingleValueScript(requireNotNull(column) { "Trigger $COLUMN_NAME_FIELD is null" },
                requireNotNull(description) { "Trigger $DESCRIPTION_FIELD is null" },
                requireNotNull(value) { "Trigger $VALUE_FIELD is null" })
        }

        private fun validateDescriptionField(descriptionField: String): DESCRIPTION {
            return DESCRIPTION.valueOf(descriptionField.toUpperCase(Locale.US))
        }
    }
}
