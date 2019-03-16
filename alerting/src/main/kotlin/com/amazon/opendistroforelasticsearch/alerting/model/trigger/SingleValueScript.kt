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

package com.amazon.opendistroforelasticsearch.alerting.model.trigger

import com.amazon.opendistroforelasticsearch.alerting.model.TriggerConditionSQL.DESCRIPTION

data class SingleValueScript(val column: String, val description: DESCRIPTION, val value: String) : PredefinedCondition {
    private val descriptionString = mapOf(DESCRIPTION.IS_BELOW to "<", DESCRIPTION.IS_ABOVE to ">", DESCRIPTION.IS_EXACTLY to "==")

    val scriptTemplate: String = """
int columnIndex = -1;
String valueType;
for (int i = 0; i < ctx.results[0].schema.length; ++i) {
    if ("$column".equals(ctx.results[0].schema[i].name)) {
        columnIndex = i;
        valueType = ctx.results[0].schema[i].type;
        break;
    }
}
if (columnIndex < 0) {
    throw new IllegalArgumentException("$column is not found.")
}

return ctx.results[0].datarows[0][columnIndex] ${descriptionString[description]} $value;
""".trimIndent()

    override fun getPainless(): String {
        return scriptTemplate
    }
}
