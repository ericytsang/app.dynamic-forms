package com.github.ericytsang.app.dynamicforms.newformdatafactory

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.ericytsang.app.dynamicforms.domainobjects.FormFieldReadData
import com.github.ericytsang.app.dynamicforms.domainobjects.Url
import com.github.ericytsang.app.dynamicforms.utils.Result
import com.github.ericytsang.app.dynamicforms.utils.SingletonFactory
import com.github.ericytsang.app.dynamicforms.utils.indices
import org.json.JSONArray
import java.util.concurrent.ArrayBlockingQueue

class RoundRobinUrlDownloadingNewFormDataFactory
private constructor(

    /**
     * some [Url]s that when HTTP GET, should return a JSON array that can be parsed into
     * [FormFieldReadData] objects.
     */
    val getUrls:List<Url>,

    _context:Context
):
    NewFormDataFactory
{
    companion object
    {
        data class Params(
            val getUrls:List<Url>,
            val context:Context
        )

        val factory = SingletonFactory()
        {params:Params ->
            RoundRobinUrlDownloadingNewFormDataFactory(
                params.getUrls,
                params.context
            )
        }
    }

    private val context = _context.applicationContext
    private val imageUrlFactory:ImageUrlFactory = LoremPicsum()
    private val volleyRequestQueue = Volley.newRequestQueue(context)
    private val formFieldJsonUrlFactory = generateSequence {getUrls}
        .flatMap {it.asSequence()}
        .iterator()

    override fun make():Result<NewFormData,String>
    {
        val imageUrl = imageUrlFactory.make()

        val formFields = ArrayBlockingQueue<()->List<FormFieldReadData>>(1)

        // Request a string response from the provided URL.
        volleyRequestQueue.add(StringRequest(
            Request.Method.GET,
            formFieldJsonUrlFactory.next().url,
            Response.Listener<String>()
            {response ->
                // todo: handle parsing errors
                val jsonArray = JSONArray(response)
                formFields += {
                    jsonArray.indices
                        .map {jsonArray.getJSONObject(it)}
                        .map {FormFieldReadData.fromJson(it)}
                }
            },
            Response.ErrorListener()
            {
                formFields += {throw Throwable(it.message)}
            }
        ))

        // start fetching the image so that it's cached so we can use it later
        volleyRequestQueue.add(ImageRequest(
            imageUrl.url,
            Response.Listener<Bitmap> {/* ignore response */},
            200,200,ImageView.ScaleType.CENTER_CROP,
            Bitmap.Config.ARGB_8888,
            Response.ErrorListener {}
        ))

        return runCatching()
        {
            Result.Success<NewFormData,String>(
                NewFormData(
                    imageUrl,
                    formFields.take().invoke()
                )
            )
        }.getOrElse()
        {
            Result.Failure(it.message!!)
        }
    }
}