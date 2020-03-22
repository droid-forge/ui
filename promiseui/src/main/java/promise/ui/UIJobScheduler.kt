/*
 * Copyright 2017, Peter Vincent
 * Licensed under the Apache License, Version 2.0, Android Promise.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package promise.ui

import android.os.Build
import android.view.Choreographer
import androidx.annotation.RequiresApi
import promise.commons.AndroidPromise
import java.util.*

object UIJobScheduler {
  private const val MAX_JOB_TIME_MS: Float = 4f

  private var elapsed = 0L
  private val jobQueue = ArrayDeque<() -> Unit>()
  private val isOverMaxTime get() = elapsed > MAX_JOB_TIME_MS * 1_000_000

  fun submitJob(job: () -> Unit) {
    jobQueue.add(job)
    if (jobQueue.size == 1) AndroidPromise.instance().executeOnUi { processJobs() }
  }

  private fun processJobs() {
    while (!jobQueue.isEmpty() && !isOverMaxTime) {
      val start = System.nanoTime()
      with(jobQueue) {
        poll().invoke()
      }
      elapsed += System.nanoTime() - start
    }
    if (jobQueue.isEmpty()) elapsed = 0 else if (isOverMaxTime) onNextFrame {
      elapsed = 0
      processJobs()
    }
  }

  private fun onNextFrame(callback: () -> Unit) =
      Choreographer.getInstance().postFrameCallback { callback() }
}