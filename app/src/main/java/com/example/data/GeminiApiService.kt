package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.Streaming

// --- Common Data Classes ---

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val tools: List<JsonObject>? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class ResponseFormat(
    val text: ResponseFormatText? = null
)

@Serializable
data class ResponseFormatText(
    val mimeType: String,
    val schema: JsonObject? = null
)

@Serializable
data class GenerationConfig(
    val responseFormat: ResponseFormat? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseModalities: List<String>? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

// --- Retrofit Setup ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:streamGenerateContent")
    @Streaming
    suspend fun generateContentStream(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): ResponseBody
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Usage Example ---

suspend fun generateAIContent(prompt: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "عذراً، يجب إضافة مفتاح Gemini API في إعدادات التطبيق لتعمل ميزات الذكاء الاصطناعي."
    }
    
    val request = GenerateContentRequest(
        contents = listOf(Content(
            parts = listOf(Part(text = prompt))
        )),
        systemInstruction = Content(
            parts = listOf(Part(text = "إنت خبير في إدارة محلات الدهب والمجوهرات في مصر. وظيفتك تحليل بيانات المحل وتقديم نصايح ذكية ومفيدة للتاجر بناءً على بياناته. اتكلم بلهجة مصرية عامية بس تكون احترافية ومحترمة (زي يا باشا، يا معلم، إيه الأخبار، إلخ). خليك مختصر ومباشر. نظّم كلامك بنقاط واضحة باستخدام Markdown."))
        )
    )
    try {
        val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
        response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "لم يتم العثور على رد."
    } catch (e: Exception) {
        if (e is retrofit2.HttpException && (e.code() == 503 || e.code() == 429 || e.code() == 404)) {
            try {
                // Fallback to pro preview
                val response2 = RetrofitClient.service.generateContent("gemini-3.1-pro-preview", apiKey, request)
                return@withContext response2.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "لم يتم العثور على رد."
            } catch (ex2: Exception) {
                try {
                    // Fallback to flash lite
                    val response3 = RetrofitClient.service.generateContent("gemini-3.1-flash-lite-preview", apiKey, request)
                    return@withContext response3.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "لم يتم العثور على رد."
                } catch (ex3: Exception) {
                    return@withContext "خطأ في الاتصال بالذكاء الاصطناعي (بعد عدة محاولات): ${ex3.message}"
                }
            }
        }
        "خطأ في الاتصال بالذكاء الاصطناعي: ${e.message}"
    }
}
