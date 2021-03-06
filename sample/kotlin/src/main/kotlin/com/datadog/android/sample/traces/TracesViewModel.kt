/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sample.traces

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.ViewModel
import com.datadog.android.ktx.tracing.withinSpan
import com.datadog.android.log.Logger
import com.datadog.android.sample.BuildConfig
import com.datadog.android.sample.data.Result
import com.datadog.android.sample.server.LocalServer
import io.opentracing.Span
import io.opentracing.util.GlobalTracer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class TracesViewModel(private val okHttpClient: OkHttpClient) : ViewModel() {

    private var asyncOperationTask: AsyncTask<Unit, Unit, Unit>? = null
    private var networkRequestTask: AsyncTask<Unit, Unit, Result>? = null
    private var localServer: LocalServer = LocalServer()

    fun onResume() {
        localServer.start("https://www.datadoghq.com/")
    }

    fun onPause() {
        localServer.stop()
    }

    fun startAsyncOperation(
        onProgress: (Int) -> Unit = {},
        onDone: () -> Unit = {}
    ) {
        asyncOperationTask = AsyncOperationTask(onProgress, onDone)
        asyncOperationTask?.execute()
    }

    fun startRequest(
        onResponse: (Response) -> Unit,
        onException: (Throwable) -> Unit,
        onCancel: () -> Unit
    ) {
        networkRequestTask = RequestTask(
            localServer.getUrl(),
            okHttpClient,
            onResponse,
            onException,
            onCancel
        )
        networkRequestTask?.execute()
    }

    fun start404Request(
        onResponse: (Response) -> Unit,
        onException: (Throwable) -> Unit,
        onCancel: () -> Unit
    ) {
        networkRequestTask = RequestTask(
            "https://www.datadoghq.com/notfound",
            okHttpClient,
            onResponse,
            onException,
            onCancel
        )
        networkRequestTask?.execute()
    }

    fun stopAsyncOperations() {
        asyncOperationTask?.cancel(true)
        networkRequestTask?.cancel(true)
        asyncOperationTask = null
        networkRequestTask = null
        localServer.stop()
    }

    private class RequestTask(
        private val url: String,
        private val okHttpClient: OkHttpClient,
        private val onResponse: (Response) -> Unit,
        private val onException: (Throwable) -> Unit,
        private val onCancel: () -> Unit
    ) : AsyncTask<Unit, Unit, Result>() {
        private var currentActiveMainSpan: Span? = null

        override fun onPreExecute() {
            super.onPreExecute()
            currentActiveMainSpan = GlobalTracer.get().activeSpan()
        }

        @SuppressLint("LogNotTimber")
        override fun doInBackground(vararg params: Unit?): Result {
            val builder = Request.Builder()
                .get()
                .url(url)

            if (currentActiveMainSpan != null) {
                builder.tag(
                    Span::class.java,
                    currentActiveMainSpan
                )
            }
            val request = builder.build()
            return try {
                val response = okHttpClient.newCall(request).execute()
                val body = response.body()
                if (body != null) {
                    val content: String = body.string()
                    // Necessary to consume the response
                    Log.d("Response", content)
                }
                Result.Success(response)
            } catch (e: Exception) {
                Log.e("Response", "Error", e)
                Result.Failure(throwable = e)
            }
        }

        override fun onPostExecute(result: Result) {
            super.onPostExecute(result)
            if (!isCancelled) {
                handleResult(result)
            }
        }

        private fun handleResult(
            result: Result
        ) {
            when (result) {
                is Result.Success<*> -> {
                    onResponse(result.data as Response)
                }
                is Result.Failure -> {
                    if (result.throwable != null) {
                        onException(result.throwable)
                    } else {
                        onCancel()
                    }
                }
            }
        }
    }

    private class AsyncOperationTask(
        val onProgress: (Int) -> Unit,
        val onDone: () -> Unit
    ) : AsyncTask<Unit, Unit, Unit>() {

        var activeSpanInMainThread: Span? = null

        private val logger: Logger by lazy {
            Logger.Builder()
                .setLoggerName("async_task")
                .setLogcatLogsEnabled(true)
                .build()
                .apply {
                    addTag("flavor", BuildConfig.FLAVOR)
                    addTag("build_type", BuildConfig.BUILD_TYPE)
                }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            activeSpanInMainThread = GlobalTracer.get().activeSpan()
        }

        override fun doInBackground(vararg params: Unit?) {
            withinSpan("AsyncOperation", activeSpanInMainThread) {
                logger.v("Starting Async Operation...")

                for (i in 0..100) {
                    if (isCancelled) {
                        break
                    }
                    onProgress(i)
                    Thread.sleep(((i * i).toDouble() / 100.0).toLong())
                }

                logger.v("Finishing Async Operation...")
            }
        }

        override fun onPostExecute(result: Unit?) {
            if (!isCancelled) {
                onDone()
            }
        }
    }
}
