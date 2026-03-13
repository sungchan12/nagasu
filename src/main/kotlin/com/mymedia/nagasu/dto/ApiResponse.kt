// package com.mymedia.nagasu.dto
//
//
// sealed class ApiResponse<out T> {
//     data class Success<T>(val data: T) : ApiResponse<T>()
//     data class Failure(val error: String) : ApiResponse<Nothing>()
// }