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

package promise.ui.adapter

import android.view.View
import promise.commons.makeInstance
import promise.ui.Viewable
import kotlin.reflect.KClass

class ViewableInstance<T>(val t: T) {

  private var viewable: Viewable? = null

  var viewClass: KClass<*>? = null

  var viewClassObject: Any? = null

  private fun convert() {
    this.viewable = when {
      viewClass != null -> {
        viewClassObject =  makeInstance(viewClass!!, arrayOf(t as Any)) as Viewable
        viewClassObject as Viewable
      }
      t is LoadingViewable -> throw IllegalStateException("Data type must not implement LoadingViewable")
      t is Viewable -> t
      (t as Any).javaClass.isAnnotationPresent(ViewableEntity::class.java) -> {
        val annotation = t.javaClass.getAnnotation(ViewableEntity::class.java)!!
        viewClass = annotation.viewHolderClass
        viewClassObject = makeInstance(viewClass!!, arrayOf(t as Any)) as ViewHolder
        object : Viewable {
          override fun layout(): Int = annotation.layoutResource
          override fun init(view: View?) {
            (viewClassObject!! as ViewHolder).init(view)
          }
          override fun bind(view: View?, args: Any?) {
            (viewClassObject!! as ViewHolder).bind(view, args)
          }
        }
      }
      else -> throw IllegalStateException("$t must be an instance on Viewable or have Viewable annotation")
    }

  }

  fun viewable(): Viewable {
    if (viewable != null) return viewable!!
    convert()
    return viewable!!
  }
}
