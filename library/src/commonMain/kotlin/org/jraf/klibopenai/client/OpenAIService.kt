/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2024-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jraf.klibopenai.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.jraf.klibopenai.json.chat.completions.JsonChatCompletionsRequest
import org.jraf.klibopenai.json.chat.completions.JsonChatCompletionsResponse
import org.jraf.klibopenai.json.threads.JsonThreadsCreateResponse
import org.jraf.klibopenai.json.threads.JsonThreadsMessage
import org.jraf.klibopenai.json.threads.JsonThreadsMessagesCreateRequest
import org.jraf.klibopenai.json.threads.JsonThreadsRun
import org.jraf.klibopenai.json.threads.JsonThreadsRunsCreateRequest

class OpenAIService(
  private val httpClient: HttpClient,
) {
  companion object {
    private const val URL_BASE = "https://api.openai.com/v1"

    private fun HttpRequestBuilder.assistantsV1Header() {
      header("OpenAI-Beta", "assistants=v1")
    }
  }

  suspend fun chatCompletions(request: JsonChatCompletionsRequest): JsonChatCompletionsResponse {
    return httpClient.post("$URL_BASE/chat/completions") {
      contentType(ContentType.Application.Json)
      setBody(request)
    }.body()
  }

  suspend fun threadsCreate(): JsonThreadsCreateResponse {
    return httpClient.post("$URL_BASE/threads") {
      contentType(ContentType.Application.Json)
      assistantsV1Header()
    }.body()
  }

  suspend fun threadsMessagesCreate(threadId: String, messageCreateRequest: JsonThreadsMessagesCreateRequest): JsonThreadsMessage {
    return httpClient.post("$URL_BASE/threads/$threadId/messages") {
      contentType(ContentType.Application.Json)
      assistantsV1Header()
      setBody(messageCreateRequest)
    }.body()
  }

  suspend fun threadsMessagesList(threadId: String): List<JsonThreadsMessage> {
    return httpClient.post("$URL_BASE/threads/$threadId/messages") {
      contentType(ContentType.Application.Json)
      assistantsV1Header()
    }.body()
  }

  suspend fun threadsRunsCreate(threadId: String, runCreateRequest: JsonThreadsRunsCreateRequest): JsonThreadsRun {
    return httpClient.post("$URL_BASE/threads/$threadId/runs") {
      contentType(ContentType.Application.Json)
      assistantsV1Header()
      setBody(runCreateRequest)
    }.body()
  }

  suspend fun threadsRunsRetrieve(threadId: String, runId: String): JsonThreadsRun {
    return httpClient.post("$URL_BASE/threads/$threadId/runs/$runId") {
      contentType(ContentType.Application.Json)
      assistantsV1Header()
    }.body()
  }
}
