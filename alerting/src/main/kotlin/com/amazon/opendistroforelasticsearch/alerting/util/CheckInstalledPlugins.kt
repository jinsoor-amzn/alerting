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
package com.amazon.opendistroforelasticsearch.alerting.util

import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest
import org.elasticsearch.client.Client
import org.elasticsearch.cluster.ClusterChangedEvent
import org.elasticsearch.cluster.ClusterStateListener
import org.elasticsearch.cluster.service.ClusterService
import org.elasticsearch.threadpool.ThreadPool

class CheckInstalledPlugins(
    private val client: Client,
    private val clusterService: ClusterService,
    private val threadPool: ThreadPool
) : ClusterStateListener {
    private val logger = LogManager.getLogger(CheckInstalledPlugins::class.java)
    val OPENDISTRO_SQL: String = "opendistro_sql"
    val OPENDISTRO_SECURITY: String = "opendistro_security"

    companion object {
        var isSQLInstalled: Boolean = false
            private set
        var isSecurityInstalled: Boolean = false
            private set
    }

    init {
        clusterService.addListener(this)
    }

    override fun clusterChanged(event: ClusterChangedEvent) {
        if (event.isNewCluster) {
            threadPool.generic().submit { doCatRequest() }
        }
    }

    private fun doCatRequest() {
        // Get plugin information only for local node.
        val nodesInfoRequest = NodesInfoRequest(clusterService.localNode().id)
        nodesInfoRequest.clear().plugins(true)
        val response = client.admin().cluster().nodesInfo(nodesInfoRequest).actionGet()
        for (node in response.nodes) {
            for (pluginInfo in node.plugins.pluginInfos) {
                if (OPENDISTRO_SQL == pluginInfo.name) {
                    isSQLInstalled = true
                } else if (OPENDISTRO_SECURITY == pluginInfo.name) {
                    isSecurityInstalled = true
                }
                logger.info("Node: ${node.hostname}," +
                    "isSQLInstalled: $isSQLInstalled," +
                    "isSecurityInstalled: $isSecurityInstalled")
            }
        }
    }
}
