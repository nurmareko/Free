package com.dresta0056.free.data.remote

import com.dresta0056.free.data.remote.dto.DeleteResponse
import com.dresta0056.free.data.remote.dto.ItemDto
import com.dresta0056.free.data.remote.dto.MeDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("items")
    suspend fun getItems(@Query("mine") mine: Boolean? = null): List<ItemDto>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: String): ItemDto

    @Multipart
    @POST("items")
    suspend fun createItem(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("location") location: RequestBody,
        @Part("contactInfo") contactInfo: RequestBody,
        @Part image: MultipartBody.Part
    ): ItemDto

    @Multipart
    @PUT("items/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("location") location: RequestBody,
        @Part("contactInfo") contactInfo: RequestBody,
        @Part image: MultipartBody.Part?
    ): ItemDto

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String): DeleteResponse

    @GET("me")
    suspend fun me(): MeDto
}
