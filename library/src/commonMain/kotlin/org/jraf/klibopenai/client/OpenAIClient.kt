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
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.jraf.klibopenai.client.configuration.ClientConfiguration
import org.jraf.klibopenai.client.configuration.HttpLoggingLevel
import org.jraf.klibopenai.json.chat.completions.JsonChatCompletionsMessage
import org.jraf.klibopenai.json.chat.completions.JsonChatCompletionsRequest
import org.jraf.klibopenai.json.threads.JsonThreadsMessage
import org.jraf.klibopenai.json.threads.JsonThreadsMessagesCreateRequest
import org.jraf.klibopenai.json.threads.JsonThreadsRun
import org.jraf.klibopenai.json.threads.JsonThreadsRunsCreateRequest

class OpenAIClient(private val clientConfiguration: ClientConfiguration) {
  private val service: OpenAIService by lazy {
    OpenAIService(
      provideHttpClient(clientConfiguration)
    )
  }

  private fun provideHttpClient(clientConfiguration: ClientConfiguration): HttpClient {
    return HttpClient {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            useAlternativeNames = false
          }
        )
      }
      install(Auth) {
        bearer {
          loadTokens {
            BearerTokens(clientConfiguration.authBearerToken, "")
          }
        }
      }
      install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 60_000
        socketTimeoutMillis = 60_000
      }
      engine {
        // Setup a proxy if requested
        clientConfiguration.httpConfiguration.httpProxy?.let { httpProxy ->
          proxy = ProxyBuilder.http(URLBuilder().apply {
            host = httpProxy.host
            port = httpProxy.port
          }.build())
        }
      }
      // Setup logging if requested
      if (clientConfiguration.httpConfiguration.loggingLevel != HttpLoggingLevel.NONE) {
        install(Logging) {
          logger = Logger.DEFAULT
          level = when (clientConfiguration.httpConfiguration.loggingLevel) {
            HttpLoggingLevel.NONE -> LogLevel.NONE
            HttpLoggingLevel.INFO -> LogLevel.INFO
            HttpLoggingLevel.HEADERS -> LogLevel.HEADERS
            HttpLoggingLevel.BODY -> LogLevel.BODY
            HttpLoggingLevel.ALL -> LogLevel.ALL
          }
        }
      }
    }
  }

  sealed interface Message {
    val content: String

    data class User(override val content: String) : Message
    data class Assistant(override val content: String) : Message
  }

  suspend fun chatCompletion(
    model: String,
    systemMessage: String,
    messages: List<Message>,
  ): String? {
    val chatCompletions = service.chatCompletions(
      JsonChatCompletionsRequest(
        model = model,
        messages = buildList {
          add(JsonChatCompletionsMessage(role = JsonChatCompletionsMessage.ROLE_SYSTEM, content = systemMessage))
          addAll(
            messages.map {
              JsonChatCompletionsMessage(
                role = when (it) {
                  is Message.User -> JsonChatCompletionsMessage.ROLE_USER
                  is Message.Assistant -> JsonChatCompletionsMessage.ROLE_ASSISTANT
                },
                content = it.content
              )
            }
          )
        }
      )
    )
    return chatCompletions.choices.singleOrNull()?.message?.content
  }

  suspend fun createThread(): String {
    return service.threadsCreate().id
  }

  suspend fun addMessageToThread(threadId: String, content: String): String {
    return service.threadsMessagesCreate(
      threadId = threadId,
      messageCreateRequest = JsonThreadsMessagesCreateRequest(
        role = JsonThreadsMessage.ROLE_USER,
        content = content,
      )
    ).id
  }

  suspend fun runThread(threadId: String, assistantId: String): List<String> {
    var run = service.threadsRunsCreate(
      threadId = threadId,
      runCreateRequest = JsonThreadsRunsCreateRequest(
        assistant_id = assistantId
      )
    )
    var count = 0
    while ((run.status == JsonThreadsRun.STATUS_QUEUED || run.status == JsonThreadsRun.STATUS_IN_PROGRESS) && count < 15) {
      delay(1000 + count * 500L)
      run = service.threadsRunsRetrieve(threadId = threadId, runId = run.id)
      count++
    }
    if (run.status == JsonThreadsRun.STATUS_QUEUED || run.status == JsonThreadsRun.STATUS_IN_PROGRESS) {
      throw Exception("Run ${run.id} on Thread ${threadId} is still queued or in progress after 15 tries")
    }
    if (run.status != JsonThreadsRun.STATUS_COMPLETED) {
      throw Exception("Run ${run.id} on Thread ${threadId} has status ${run.status} (expected ${JsonThreadsRun.STATUS_COMPLETED})")
    }
    return service.threadsMessagesList(threadId = threadId)
      .takeWhile { it.role == JsonThreadsMessage.ROLE_ASSISTANT }
      .flatMap { it.content.map { content -> content.text.value } }
  }
}
