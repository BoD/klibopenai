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

package org.jraf.klibopenai.json.chat.completions

import kotlinx.serialization.Serializable

@Serializable
data class JsonChatCompletionsResponse(
  val id: String,
  val choices: List<JsonChoice>,
)

@Serializable
data class JsonChoice(
  val message: JsonChatCompletionsMessage,
  val finish_reason: String?,
) {
  companion object {
    const val FINISH_REASON_STOP = "stop"
    const val FINISH_REASON_LENGTH = "length"
    const val FINISH_REASON_CONTENT_FILTER = "content_filter"
  }
}
