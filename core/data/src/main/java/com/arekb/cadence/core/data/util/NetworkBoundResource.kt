package com.arekb.cadence.core.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

/**
 * A generic function that provides a resource backed by both the local database and the network.
 *
 * @param ResultType The type of the data returned by the resource.
 * @param RequestType The type of the data fetched from the network.
 * @param query A function that returns a [Flow] of data from the local database.
 * @param fetch A suspend function that makes a network request and returns a [retrofit2.Response].
 * @param saveFetchResult A suspend function that saves the result of the network request to the local database.
 * @param shouldFetch A function that decides whether to fetch new data from the network.
 * @return A [Flow] of [Result] that emits data from the local database and/or the network.
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType?>,
    crossinline fetch: suspend () -> Response<RequestType>,
    crossinline saveFetchResult: suspend (Response<RequestType>) -> Unit,
    crossinline shouldFetch: (ResultType?) -> Boolean = { true } // Default to always fetching
) = flow {
    // Get initial data from the local database. It will be null if the DB is empty.
    val initialData = query().firstOrNull()

    /// Only proceed with the network call if the shouldFetch rule returns true.
    if (shouldFetch(initialData)) {
        // First, emit the cached data so the UI loads loading state
        if (initialData != null) {
            emit(Result.success(initialData))
        }

        try {
            // Make the network call and save the result.
            saveFetchResult(fetch())
            // After saving, query the database again to get the fresh data.
            emitAll(query().map { Result.success(it) })
        } catch (throwable: Throwable) {
            // If the network fails, continue emitting the cached data but with an error.
            emitAll(query().map { Result.failure(throwable) })
        }
    } else {
        // If we don't need to fetch, just continue emitting data from the database.
        emitAll(query().map { Result.success(it) })
    }
}