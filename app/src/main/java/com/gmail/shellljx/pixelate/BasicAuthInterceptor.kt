package com.gmail.shellljx.pixelate

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/15
 * @Description:
 */
class BasicAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val authenticatedRequest: Request = request.newBuilder()
            .header("Authorization", "Basic cHg2ZnFnZG12YmQyYzdpOmNuN3IyYnQ4MTUzYnQzZHFkYWNkMDNlMWg4MWNtMTZoYmVvcGtwZW1pNTJjOWFtamlqZXE=").build()
        return chain.proceed(authenticatedRequest)
    }
}