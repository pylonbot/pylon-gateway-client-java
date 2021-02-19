package lol.up.pylon.gateway.client.extension

import kotlinx.coroutines.future.await
import lol.up.pylon.gateway.client.service.request.GrpcApiRequest
import lol.up.pylon.gateway.client.service.request.GrpcRequest

suspend fun <T> GrpcRequest<T>.await(): T = submit().await()

suspend fun <T> GrpcApiRequest<T>.await(): T = submit().await()