# Android Promise UI [![](https://jitpack.io/v/android-promise/ui.svg)](https://jitpack.io/#android-promise/ui)

Adapters, notifications, loading layout, Chain processors


### Table of Contents
**[Setup](##Setup)**<br>
**[Initialization](##Initialization)**<br>
**[Adapters](##Adapters)**<br>
**[Loading Layout](##LoadingLayout)**<br>
**[Chain Processors](##Transactions)**<br>
**[Next Steps, Credits, Feedback, License](#next-steps)**<br>

## Setup
##### build.gradle
```

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

android {
    ...
    compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
}

dependencies {
     ...
     implementation 'com.github.android-promise:ui:TAG'
     implementation 'com.github.android-promise:commons:1.1-alpha03'
}
```

### Initialization
Initialize Promise in your main application file, entry point

##### App.java
```java
public class App extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    // 10 is the number of threads allowed to run in the background
    AndroidPromise.init(this, 10, BuildConfig.DEBUG);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    AndroidPromise.instance().terminate();
  }
}
```

## Adapters
In this example, a simple use of the promise adapter, the adapter renders a list of viewable pojo

```kotlin
lateinit var adapter: PromiseAdapter<ViewablePoJo>
```
The adapter can optionally a click listner to fire when an viewable pojo is clicked
In this scenario, the adapter also takes a map of object class to viewholder class.
 
```kotlin
adapter = PromiseAdapter(ArrayMap<Class<*>, KClass<out Viewable>>().apply {
      put(ViewablePoJo::class.java, ViewablePoJoViewable::class)
    }, this, true)

```
To pass the right object to the viewholder, to properly a viewholder to object type, one of the following three ways apply
#### Mapping with ViewHolder class
The viewholder class is as, it must implement Viewable and have one constructor accepting the object instance to be displayed
```kotlin

class ViewablePoJoViewable(private val viewablePoJo: ViewablePoJo) : Viewable {

  lateinit var imageView: ImageView
  lateinit var textView: TextView
  lateinit var shimmerFrameLayout: ShimmerFrameLayout

  override fun layout(): Int = R.layout.pojo_layout

  override fun init(view: View) {
    shimmerFrameLayout = view.findViewById(R.id.shimmers)
    textView = view.findViewById(R.id.pojo_text)
    imageView = view.findViewById(R.id.action_image)
  }

  override fun bind(view: View?, args: Any?) {
    if (args is Boolean) {
      shimmerFrameLayout.startShimmer()
      return
    }
    shimmerFrameLayout.stopShimmer()
    shimmerFrameLayout.setShimmer(null)
    textView.text = viewablePoJo.text
  }
}
```
#### Using ViewableEntity annotation
The object class should have a viewable entity annotation as follows
```kotlin
// this viewholder must have a constructor accepting an instance of viewable pojo
class ViewablePoJoViewHolder(private val viewablePoJo: ViewablePoJo) : ViewHolder {

  override fun init(view: View) {
    textView = view.findViewById(R.id.pojo_text)
  }

  override fun bind(view: View, args: Any?) {
    textView.text = viewablePoJo.text
  }

  lateinit var textView: TextView
}

//this entity tells the adapter to use ViewablePoJoViewHolder when rendering items of type ViewablePoJo
@ViewableEntity(layoutResource = R.layout.pojo_layout, viewHolderClass = ViewablePoJoViewHolder::class)
class ViewablePoJo(val text: String): Searchable {
  @SuppressLint("DefaultLocale")
  override fun onSearch(query: String): Boolean = text.toLowerCase().contains(query.toLowerCase())
}
```
#### Or the entity should be Viewable
```kotlin
class ImplementingViewable(): Viewable {
  // returns the layout for this instance 
  override fun layout(): Int {
    return R.layout.pojo_layout
  }

  override fun init(view: View?) {
    // initialize the views
  }

  override fun bind(view: View?, args: Any?) {
    //bind data to the views
  }
}
```

### Adapter click events
The PromiseAdapter.Listener delivers click events for all public views in the viewholder,
```kotlin
class MainActivity : AppCompatActivity(), PromiseAdapter.Listener<ViewablePoJo> {

  override fun onClick(t: ViewablePoJo, id: Int) {
    Toast.makeText(this, "Clicked ${t.text}", Toast.LENGTH_LONG).show()
    LogUtil.e("_MainActivity", "clicked the pojo", t, " id ", id)
  }
  ...
  ```
When a view is declared as public, a click listener is injected to it by the adapter
```kotlin

class ViewablePoJoViewable(private val viewablePoJo: ViewablePoJo) : Viewable {
  // this views are public
  lateinit var imageView: ImageView
  lateinit var textView: TextView
  lateinit var shimmerFrameLayout: ShimmerFrameLayout
  ...
}
```
> declaring a view as private, a click listener won't be injectet into it
The id of the clicked view and the object instance are both delivered in the listener onClick handler

```kotlin
class MainActivity : AppCompatActivity(), PromiseAdapter.Listener<ViewablePoJo> {
  // fired when item is clicked
  override fun onClick(t: ViewablePoJo, id: Int) {
    Toast.makeText(this, "Clicked ${t.text}", Toast.LENGTH_LONG).show()
    LogUtil.e("_MainActivity", "clicked the pojo", t, " id ", id)
  }
  ...
}
```
The full illusration below...
```kotlin
class MainActivity : AppCompatActivity(), PromiseAdapter.Listener<ViewablePoJo> {

  override fun onClick(t: ViewablePoJo, id: Int) {
    Toast.makeText(this, "Clicked ${t.text}", Toast.LENGTH_LONG).show()
    LogUtil.e("_MainActivity", "clicked the pojo", t, " id ", id)
  }

  lateinit var adapter: PromiseAdapter<ViewablePoJo>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    searchEditText.addTextChangedListener(object: TextWatcher {
      override fun afterTextChanged(s: Editable) {
        this.onTextChanged(s.toString(), 0, 0, 0)
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        adapter.search(s.toString()){
          if(!it) loading_view.showEmpty(R.drawable.ic_launcher_background,
              "Searched list is empty",
              "No item found with the query provided")
          else loading_view.showContent()
        }
      }
    })

    adapter = PromiseAdapter(ArrayMap<Class<*>, KClass<out Viewable>>().apply {
      put(ViewablePoJo::class.java, ViewablePoJoViewable::class)
    }, this, true)

    recycler_view.layoutManager = LinearLayoutManager(this)

    recycler_view.adapter = adapter

    loading_view.showContent()
    adapter.add(List.generate(10) {
      ViewablePoJo("test $it")
    })

    AndroidPromise.instance().executeOnUi({
      adapter.args = null

      adapter setList List.generate(50) {
        ViewablePoJo("test $it")
      }
    }, 5000)
  }
}
```

## LoadingLayout

This is useful when you have to display another view when loading items from a repository

```xml
...
<promise.ui.loading.LoadingLayout
      android:id="@+id/loading_view"
      android:layout_weight="1"
      android:layout_height="0dp"
      android:layout_width="match_parent">
      <androidx.recyclerview.widget.RecyclerView
          android:id="@+id/recycler_view"
          android:layout_margin="16dp"
          android:layout_height="match_parent"
          android:layout_width="match_parent"/>
    </promise.ui.loading.LoadingLayout>
...
```
> The loading layout only takes one sigle child in the xml tree

#### Using it in the source code
When requesting data from repository, initialize loading as follows
```kotlin

    // using default loader progress bar
    loading_view.showLoading(null)

    // can also pass an object that is viewable
    class LoadingViewable(private val message: String): Viewable {
      override fun layout(): Int {
        return R.layout.loading_viewable
      }

      override fun init(view: View?) {
        // init loading layout views
      }

      override fun bind(view: View?, args: Any?) {
        // bind message to the views
      }
    }

    loading_view.showLoading(LoadingViewable("Loading items, please wait"))

```

After receiving data
```kotlin
 loading_view.showContent()
```
More apis
```kotlin
    ...
    loading_view.showEmpty(...)
    loading_view.showError(...)
    ...
```

## New features on the way
watch this repo to stay updated 

# Developed By
* Peter Vincent - <dev4vin@gmail.com>
# Donations
If you'd like to support this library development, you could buy me coffee here:
* [![Become a Patreon]("https://c6.patreon.com/becomePatronButton.bundle.js")](https://www.patreon.com/bePatron?u=31932751)

Thank you very much in advance!

#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!<br/>

# License

    Copyright 2018 Peter Vincent

    Licensed under the Apache License, Version 2.0 Android Promise;
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

