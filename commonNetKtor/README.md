# CommonNetKtor 网络请求库 (Ktor)

## 📖 模块概述

CommonNetKtor 是基于 Ktor + Ktorfit 的现代化网络请求库，提供了类型安全、协程原生的网络请求解决方案，支持 JSON 序列化、日志记录、缓存等功能。

## 🚀 核心特性

- 🌐 **Ktor 架构** - 基于 Ktor 的跨平台网络引擎
- 📝 **Ktorfit** - 声明式 API 定义，类似 Retrofit
- ⚡ **协程原生** - 完全基于 Kotlin 协程的异步处理
- 🔄 **Flow 支持** - 原生支持 Kotlin Flow 数据流
- 📊 **网络结果封装** - 统一的网络请求结果处理
- 🎯 **类型安全** - 使用 Kotlinx.serialization 进行类型安全的 JSON 处理

## 📦 依赖说明

```kotlin
// 在 app/build.gradle.kts 中添加依赖
implementation(project(":commonNetKtor"))
```

## 🏗️ 使用方式

### 1. 初始化网络配置

#### 创建配置类
```kotlin
class MyNetworkConfig : BaseNetworkConfig(), INetworkConfig {
    override fun getBaseUrl(): String = "https://api.example.com/"
    
    override fun getConnectTimeout(): Long = 15000L // 15秒
    
    override fun getReadTimeout(): Long = 30000L // 30秒
    
    override fun getInterceptors(): List<Interceptor> {
        return listOf(
            // 通用请求头拦截器
            Interceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "MyApp/1.0")
                    .build()
                chain.proceed(request)
            }
        )
    }
}
```

#### 在 Application 中初始化
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val networkConfig = MyNetworkConfig()
        NetworkManager.init(networkConfig)
    }
}
```

### 2. 创建 API 接口

```kotlin
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String
)

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

interface UserApi {
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): NetworkResult<User>
    
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): NetworkResult<List<User>>
    
    @POST("users")
    suspend fun createUser(@Body user: CreateUserRequest): NetworkResult<User>
    
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: String,
        @Body user: CreateUserRequest
    ): NetworkResult<User>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: String): NetworkResult<Unit>
    
    @Multipart
    @POST("upload")
    suspend fun uploadFile(@Part file: PartData<FileChannel>): NetworkResult<UploadResult>
}
```

### 3. 创建 Repository

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val ktorfit: Ktorfit
) : BaseRepository() {
    
    private val userApi = ktorfit.create<UserApi>()
    
    fun getUser(userId: String): Flow<NetworkResult<User>> = request { emit(userApi.getUser(userId)) }
    
    fun getUsers(page: Int = 1, size: Int = 20): Flow<NetworkResult<List<User>>> = request { 
        emit(userApi.getUsers(page, size)) 
    }
    
    fun createUser(user: CreateUserRequest): Flow<NetworkResult<User>> = request { 
        emit(userApi.createUser(user)) 
    }
    
    fun updateUser(userId: String, user: CreateUserRequest): Flow<NetworkResult<User>> = request { 
        emit(userApi.updateUser(userId, user)) 
    }
    
    fun deleteUser(userId: String): Flow<NetworkResult<Unit>> = request { 
        emit(userApi.deleteUser(userId)) 
    }
    
    // 使用 apiCall 安全包装
    fun safeGetUser(userId: String): Flow<NetworkResult<User>> = request {
        emit(apiCall { userApi.getUser(userId) })
    }
}
```

### 4. 在 ViewModel 中使用

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {
    
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user
    
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            userRepository.getUser(userId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _user.value = result.data
                        _loading.value = false
                    }
                    is NetworkResult.Exception -> {
                        _error.value = result.e.message
                        _loading.value = false
                    }
                }
            }
        }
    }
    
    fun loadUsers(page: Int = 1, size: Int = 20) {
        viewModelScope.launch {
            userRepository.getUsers(page, size).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _users.value = result.data
                        _loading.value = false
                    }
                    is NetworkResult.Exception -> {
                        _error.value = result.e.message
                        _loading.value = false
                    }
                }
            }
        }
    }
}
```

### 5. 在 Activity/Fragment 中使用

```kotlin
@AndroidEntryPoint
class UserActivity : BaseActivity<ActivityUserBinding, UserViewModel>() {
    
    override val mViewModel: UserViewModel by viewModels()
    
    override fun createVB(): ActivityUserBinding {
        return ActivityUserBinding.inflate(layoutInflater)
    }
    
    override fun initObserve() {
        mViewModel.user.observe(this) { user ->
            binding.nameTextView.text = user.name
            binding.emailTextView.text = user.email
        }
        
        mViewModel.users.observe(this) { users ->
            // 更新列表 UI
        }
        
        mViewModel.loading.observe(this) { loading ->
            if (loading) {
                // 显示加载状态
            } else {
                // 隐藏加载状态
            }
        }
        
        mViewModel.error.observe(this) { error ->
            // 显示错误信息
        }
    }
    
    override fun initData() {
        mViewModel.loadUser("123")
        mViewModel.loadUsers()
    }
}
```

## 🔧 高级配置

### 1. 自定义 JSON 配置

```kotlin
object NetworkManager {
    
    fun init(config: INetworkConfig) {
        if (isInitialized) return
        
        val ktorClient = HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                    readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                    config.getInterceptors().forEach { addInterceptor(it) }
                }
            }
            
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    encodeDefaults = false
                })
            }
            
            // 其他配置...
        }
        
        ktorfit = Ktorfit.Builder()
            .baseUrl(config.getBaseUrl())
            .httpClient(ktorClient)
            .build()
        
        isInitialized = true
    }
}
```

### 2. 添加认证拦截器

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getAuthToken() // 获取认证令牌
        
        val original = chain.request()
        val request = if (token.isNotEmpty()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        
        return chain.proceed(request)
    }
    
    private fun getAuthToken(): String {
        // 从本地存储获取令牌
        return SpUtils.getString("auth_token", "")
    }
}
```

### 3. 缓存配置

```kotlin
fun initWithCache(config: INetworkConfig) {
    val ktorClient = HttpClient(OkHttp) {
        install(HttpCache) {
            publicCache(File(context.cacheDir, "ktor_cache"), 10 * 1024 * 1024) // 10MB
        }
        
        // 其他配置...
    }
}
```

### 4. 重试机制

```kotlin
suspend fun <T> apiCallWithRetry(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    apiCall: suspend () -> T
): NetworkResult<T> {
    var currentDelay = initialDelay
    repeat(maxRetries) { attempt ->
        try {
            return NetworkResult.Success(apiCall())
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) {
                return NetworkResult.Exception(e)
            }
            
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    return NetworkResult.Exception(Exception("Max retries exceeded"))
}
```

## 🔒 网络结果处理

### 1. 扩展 NetworkResult

```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val exception: Throwable,
        val message: String? = null,
        val code: Int? = null
    ) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

// 扩展函数
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (NetworkResult.Error) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(this)
    return this
}

inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) action()
    return this
}

val NetworkResult<*>.isSuccess: Boolean
    get() = this is NetworkResult.Success

val NetworkResult<*>.isError: Boolean
    get() = this is NetworkResult.Error

val NetworkResult<*>.isLoading: Boolean
    get() = this is NetworkResult.Loading
```

### 2. 错误处理

```kotlin
class NetworkErrorHandler {
    
    fun handleError(exception: Throwable): NetworkResult.Error {
        return when (exception) {
            is ClientRequestException -> {
                val statusCode = exception.response.status.value
                val message = when (statusCode) {
                    400 -> "请求参数错误"
                    401 -> "未授权访问"
                    403 -> "禁止访问"
                    404 -> "资源不存在"
                    else -> "客户端错误: $statusCode"
                }
                NetworkResult.Error(exception, message, statusCode)
            }
            is ServerResponseException -> {
                val statusCode = exception.response.status.value
                val message = when (statusCode) {
                    in 500..599 -> "服务器内部错误"
                    else -> "服务器错误: $statusCode"
                }
                NetworkResult.Error(exception, message, statusCode)
            }
            is ConnectTimeoutException -> NetworkResult.Error(exception, "连接超时")
            is SocketTimeoutException -> NetworkResult.Error(exception, "读取超时")
            is UnknownHostException -> NetworkResult.Error(exception, "网络连接失败")
            else -> NetworkResult.Error(exception, "未知错误: ${exception.message}")
        }
    }
}
```

## 🧪 测试

### 1. Mock API 测试

```kotlin
@ExperimentalCoroutinesApi
class UserRepositoryTest {
    
    @Mock
    private lateinit var ktorfit: Ktorfit
    
    @Mock
    private lateinit var userApi: UserApi
    
    private lateinit var userRepository: UserRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(ktorfit.create<UserApi>()).thenReturn(userApi)
        userRepository = UserRepository(ktorfit)
    }
    
    @Test
    fun testGetUserSuccess() = runTest {
        // Given
        val expectedUser = User("1", "Test User", "test@example.com", "2024-01-01")
        whenever(userApi.getUser("1")).thenReturn(NetworkResult.Success(expectedUser))
        
        // When
        val result = userRepository.getUser("1").first()
        
        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedUser, (result as NetworkResult.Success).data)
    }
    
    @Test
    fun testGetUserError() = runTest {
        // Given
        val exception = Exception("Network error")
        whenever(userApi.getUser("1")).thenReturn(NetworkResult.Exception(exception))
        
        // When
        val result = userRepository.getUser("1").first()
        
        // Then
        assertTrue(result is NetworkResult.Exception)
    }
}
```

### 2. 集成测试

```kotlin
class UserApiIntegrationTest {
    
    private val ktorfit = Ktorfit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .build()
    
    private val userApi = ktorfit.create<UserApi>()
    
    @Test
    fun testRealApiCall() = runTest {
        val result = apiCall { 
            userApi.getUser("1") 
        }
        
        assertTrue(result is NetworkResult.Success)
    }
}
```

## 🚨 注意事项

1. **初始化顺序**: 必须在使用 API 之前初始化 NetworkManager
2. **序列化注解**: 使用 `@Serializable` 注解标记数据类
3. **线程安全**: NetworkManager 使用双重检查锁定保证线程安全
4. **错误处理**: 始终检查 NetworkResult 的状态
5. **协程作用域**: 在合适的协程作用域中调用 API

## 📱 版本要求

- **最低 SDK**: 24 (Android 7.0)
- **编译 SDK**: 36 (Android 14)
- **Ktor**: 3.3.3
- **Ktorfit**: 2.6.5
- **Kotlin**: 1.9+
- **Kotlinx Serialization**: 1.6.0+

## 📄 许可证

本项目采用 MIT 许可证。