package com.retrofitmasterclass

import android.database.Observable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.retrofitmasterclass.ui.theme.RetrofitMasterClassTheme
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Random
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    fun randomImageFile(): File{
        val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val rnd = Random()
        val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

        canvas.drawColor(color)

        val imageFile = File(filesDir,"image${color.absoluteValue}.png")
        imageFile.createNewFile()
        val bos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 0 , bos)
        val bitmapdata = bos.toByteArray()
        val fos = FileOutputStream(imageFile)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
        return imageFile
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*CoroutineScope(Dispatchers.IO).launch {
            try {
                val r = postsWithResponse()
                if(r.isSuccessful){
                    Log.d("lfjsflslfs", r.body()?.get(0)?.body.toString())
                }
            }
            catch (e: Exception){
                Log.d("lfjsflslfs", e.message.toString())
            }
        }*/
        /*CoroutineScope(Dispatchers.IO).launch {
            postsWithExecute()
        }*/
        /*CoroutineScope(Dispatchers.IO).launch {
            val imageFiles = List(3){
                randomImageFile()
            }
            ////////////////////////////
            postMethodWithImage(
                imageFiles
            ){postResponse1, throwable ->
                Log.d("lfjsflslfs",postResponse1.toString()+","+throwable?.message.toString())
            }
        }*/

        callMyApi1WithRxJava{ myData, throwable ->
            Log.d("lfjsflslfs",myData.toString()+","+throwable?.message.toString())
        }

        //posts()

        setContent {
            RetrofitMasterClassTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                }
            }
        }
    }

    fun callMyApi1WithRxJava(
        resultCallback: (MyData?,Throwable?)->Unit
    ){
        val logging = HttpLoggingInterceptor{
            Log.d("lfjsflslfs",it)
        }
        logging.level= HttpLoggingInterceptor.Level.BASIC
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(
                OkHttpClient
                    .Builder()
                    .addInterceptor(logging)
                    .addInterceptor(ChuckerInterceptor(this))
                    .build()
            )
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        apiService.simpleGetWithObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                it.title
            }
            .subscribe({
                Log.d("lfjsflslfs",it)
            },{
                Log.d("lfjsflslfs",it.message.toString())
            })
    }
}

interface ApiService{
    @GET("todos/1")
    fun simpleGet(): Call<MyData>

    @GET("todos/1")
    fun simpleGetWithObservable(): io.reactivex.Observable<MyData>

    @Headers("My-Agent: Retrofit-Sample-App")
    @GET("todos/1")
    fun simpleGetWithHeader(): Call<MyData>

    @GET("todos/1")
    fun simpleGetWithHeader(@Header("Authorization") authorization: String): Call<MyData>

    @GET("todos/1")
    fun simpleGetWithHeader(@HeaderMap headers: Map<String,String>): Call<MyData>

    @Headers(
        value = [
            "My-Agent: Retrofit-Sample-App",
            "My-Agent1: Retrofit-Sample-App1",
        ]
    )
    @GET("todos/1")
    fun simpleGetWithHeaders(): Call<MyData>

    @GET("todos/1")
    suspend fun simpleGetResponse(): Response<MyData>

    @GET("todos/1")
    fun simpleGetString(): Call<String>

    @GET("posts")
    fun posts(): Call<List<PostData>>

    @GET("posts/1")
    fun singlePost(): Call<PostData>

    @GET
    fun singlePostWithUrl(@Url url: String): Call<PostData>

    @GET("posts/{id}")
    fun singlePostWithPath(@Path("id") id: String): Call<PostData>

    @GET("posts")
    suspend fun postsWithResponse(): Response<List<PostData>>

    @GET("comments")
    fun comments(@Query("postId") id: String): Call<List<Comment>>

    @GET("comments")
    fun comments(
        @Query("postId") id: String,
        @Query("sort") sort: String,
    ): Call<List<Comment>>

    @GET("comments")
    fun comments(
        @QueryMap queries:Map<String,String>
    ): Call<List<Comment>>

    @POST("posts")
    fun postMethod(): Call<PostResponse>

    @FormUrlEncoded
    @POST("posts")
    fun postMethodFormUrlEncoded(
        @Field("name") name: String,
        @Field("age") age: Int
    ): Call<PostResponse1>

    @FormUrlEncoded
    @POST("posts")
    fun postMethodFormUrlEncoded(
        @FieldMap fields: Map<String,String>
    ): Call<PostResponse1>


    @POST("posts")
    fun postMethodWithBody(
        @Body data: MyOwnData
    ): Call<PostResponse1>


    @Multipart
    @POST("posts")
    fun postMethodWithMultipart(
        @Part("name") name: RequestBody
    ): Call<PostResponse1>


    @Multipart
    @POST("posts")
    fun postMethodWithImage(
        @Part name: MultipartBody.Part
    ): Call<PostResponse1>

    @Multipart
    @POST("posts")
    fun postMethodWithImages(
        @Part name: List<MultipartBody.Part>
    ): Call<PostResponse1>


    @Multipart
    @POST("posts")
    fun postMethodWithMultipart(
        @PartMap fields: Map<String,  @JvmSuppressWildcards RequestBody>
    ): Call<PostResponse1>
}

data class MyOwnData(
    val name: String,
    val age: String
)

fun callMyApi(
    resultCallback: (MyData?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.simpleGet().enqueue(object: Callback<MyData> {
        override fun onResponse(call: Call<MyData>, response: Response<MyData>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<MyData>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun callMyApiWthHeader(
    resultCallback: (MyData?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.simpleGetWithHeader().enqueue(object: Callback<MyData> {
        override fun onResponse(call: Call<MyData>, response: Response<MyData>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<MyData>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun callMyApiWthHeaders(
    resultCallback: (MyData?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.simpleGetWithHeaders().enqueue(object: Callback<MyData> {
        override fun onResponse(call: Call<MyData>, response: Response<MyData>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<MyData>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun callMyApiWthHeader(
    authorization: String,
    resultCallback: (MyData?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.simpleGetWithHeader(authorization).enqueue(object: Callback<MyData> {
        override fun onResponse(call: Call<MyData>, response: Response<MyData>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<MyData>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun callMyApiWthHeaders(
    headers: Map<String, String>,
    resultCallback: (MyData?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.simpleGetWithHeader(headers).enqueue(object: Callback<MyData> {
        override fun onResponse(call: Call<MyData>, response: Response<MyData>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<MyData>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun singlePost(
    resultCallback: (PostData?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.singlePost().enqueue(object: Callback<PostData> {
        override fun onResponse(call: Call<PostData>, response: Response<PostData>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostData>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun postMethod(
    resultCallback: (PostResponse?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.postMethod().enqueue(object: Callback<PostResponse> {
        override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostResponse>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun postMethod(
    name: String,
    age: Int,
    resultCallback: (PostResponse1?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.postMethodFormUrlEncoded(name,age).enqueue(object: Callback<PostResponse1> {
        override fun onResponse(call: Call<PostResponse1>, response: Response<PostResponse1>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostResponse1>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun postMethod(
    fields: Map<String, String>,
    resultCallback: (PostResponse1?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.postMethodFormUrlEncoded(fields).enqueue(object: Callback<PostResponse1> {
        override fun onResponse(call: Call<PostResponse1>, response: Response<PostResponse1>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostResponse1>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun postMethod(
    data: MyOwnData,
    resultCallback: (PostResponse1?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.postMethodWithBody(data).enqueue(object: Callback<PostResponse1> {
        override fun onResponse(call: Call<PostResponse1>, response: Response<PostResponse1>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostResponse1>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun postMethodWithMultipart(
    name: String,
    resultCallback: (PostResponse1?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    val rb = RequestBody.create("text/plain".toMediaTypeOrNull(),name)
    apiService.postMethodWithMultipart(rb).enqueue(object: Callback<PostResponse1> {
        override fun onResponse(call: Call<PostResponse1>, response: Response<PostResponse1>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostResponse1>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun postMethodWithMultipart(
    name: String,
    age: String,
    resultCallback: (PostResponse1?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    val rb = RequestBody.create("text/plain".toMediaTypeOrNull(),name)
    val rb1 = RequestBody.create("text/plain".toMediaTypeOrNull(),age)
    val map = mapOf(
        "name" to rb,
        "age" to rb1
    )
    apiService.postMethodWithMultipart(map).enqueue(object: Callback<PostResponse1> {
        override fun onResponse(call: Call<PostResponse1>, response: Response<PostResponse1>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostResponse1>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun postMethodWithImage(
    file: File,
    resultCallback: (PostResponse1?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    val rb = RequestBody.create("image/*".toMediaTypeOrNull(),file)
    val mb = MultipartBody.Part.createFormData("image",file.name,rb)
    apiService.postMethodWithImage(mb).enqueue(object: Callback<PostResponse1> {
        override fun onResponse(call: Call<PostResponse1>, response: Response<PostResponse1>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostResponse1>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun postMethodWithImage(
    files: List<File>,
    resultCallback: (PostResponse1?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)

    val imageParts = files.map {file->
        val rb = RequestBody.create("image/*".toMediaTypeOrNull(),file)
        MultipartBody.Part.createFormData("image[]",file.name,rb)
    }

    apiService.postMethodWithImages(imageParts).enqueue(object: Callback<PostResponse1> {
        override fun onResponse(call: Call<PostResponse1>, response: Response<PostResponse1>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostResponse1>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}

fun singlePostWithUrl(
    id: String,
    resultCallback: (PostData?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.singlePostWithUrl("posts/$id").enqueue(object: Callback<PostData> {
        override fun onResponse(call: Call<PostData>, response: Response<PostData>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostData>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun singlePostWithPath(
    id: String,
    resultCallback: (PostData?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.singlePostWithPath(id).enqueue(object: Callback<PostData> {
        override fun onResponse(call: Call<PostData>, response: Response<PostData>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<PostData>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun comments(
    id: String,
    resultCallback: (List<Comment>?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.comments(id).enqueue(object: Callback<List<Comment>> {
        override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun comments(
    id: String,
    sort: String,
    resultCallback: (List<Comment>?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.comments(id,sort).enqueue(object: Callback<List<Comment>> {
        override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun comments(
    queries: Map<String, String>,
    resultCallback: (List<Comment>?,Throwable?)->Unit
){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://webhook.site/c555763d-c9f3-485d-b4c9-28d478f3a061/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.comments(queries).enqueue(object: Callback<List<Comment>> {
        override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
            resultCallback(response.body(),null)
        }

        override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
            resultCallback(null,t)
        }
    })
}
fun posts(){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.posts().enqueue(object: Callback<List<PostData>> {
        override fun onResponse(call: Call<List<PostData>>, response: Response<List<PostData>>) {
            Log.d("lfjsflslfs", response.body()?.get(0)?.body.toString())
        }

        override fun onFailure(call: Call<List<PostData>>, t: Throwable) {
            Log.d("lfjsflslfs", "Failed: ${t.message}")
        }
    })
}
fun postsWithExecute(): Response<List<PostData>> {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    return apiService.posts().execute()
}
suspend fun postsWithResponse(): Response<List<PostData>> {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    return apiService.postsWithResponse()
}

suspend fun callMyApiWithResponse(): String? {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    val r = apiService.simpleGetResponse()
    return r.body()?.title
}

fun callMyApiWithExecute(){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    val r = apiService.simpleGet().execute()
    val myData = r.body()
    val title = myData?.title?:"null"
    Log.d("lfjsflslfs",title)
}

fun callMyApiForString(){
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    apiService.simpleGetString().enqueue(object: Callback<String> {
        override fun onResponse(call: Call<String>, response: Response<String>) {
            Log.d("lfjsflslfs",response.body()?:"none")
        }

        override fun onFailure(call: Call<String>, t: Throwable) {
            Log.d("lfjsflslfs","failed")
        }
    })
}

data class MyData (
    val userID: Long,
    val id: Long,
    val title: String,
    val completed: Boolean
)
/////////////////////////////////////////////
data class Posts(
    val list: List<PostData>
)// this is not required, List<PostData>

data class PostData (
    val userID: Long,
    val id: Long,
    val title: String,
    val body: String
)

data class Comment (
    val postId: Long,
    val id: Long,
    val name: String,
    val email: String,
    val body: String
)

data class PostResponse (
    val id: Long
)

data class PostResponse1 (
    val name: String,
    val age: String,
    val id: Long
)