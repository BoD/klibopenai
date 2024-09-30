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

package org.jraf.klibopenai.json.threads

import kotlinx.serialization.Serializable

@Serializable
data class JsonThreadsRun(
  val id: String,
  val created_at: Long,
  val thread_id: String,
  val status: String,
  val started_at: Long,
  val expires_at: Long?,
  val cancelled_at: Long?,
  val failed_at: Long?,
  val completed_at: Long?,
  val last_error: String?,
) {
  companion object {
    const val STATUS_QUEUED = "queued"
    const val STATUS_IN_PROGRESS = "in_progress"
    const val STATUS_REQUIRES_ACTION = "requires_action"
    const val STATUS_CANCELLING = "cancelling"
    const val STATUS_CANCELLED = "cancelled"
    const val STATUS_FAILED = "failed"
    const val STATUS_COMPLETED = "completed"
    const val STATUS_EXPIRED = "expired"
  }
}
