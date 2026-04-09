package com.novamindlabs.learngo.core

/**
 * A generic sealed class that represents the state of a resource or data request.
 *
 * This class is commonly used in MVVM architecture with LiveData/StateFlow to handle:
 * - Loading states
 * - Success responses
 * - Error responses
 * - Idle/default states
 *
 * @param T The type of data being handled
 * @property data The actual data, if available
 * @property message An optional message (e.g., error message)
 */
sealed class Resource<out T>(
    val data: T? = null,
    val message: String? = null
) {

    /**
     * Represents a successful state with non-null [data].
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Represents an error state with optional [data] and a mandatory [message].
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    /**
     * Represents a loading state. Optional [data] can be provided to show cached/previous data.
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)

    /**
     * Represents an idle or default state, typically before any request has been made.
     */
    object Idle : Resource<Nothing>()
}
