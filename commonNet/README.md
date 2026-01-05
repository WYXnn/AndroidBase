# CommonNet 网络请求库 (Retrofit)

## 📖 模块概述

CommonNet 是基于 Retrofit + OkHttp 的网络请求库，提供了统一的网络配置、依赖注入支持和基础的网络请求架构。

## 🚀 核心特性

- 🌐 **Retrofit 架构** - 基于 Retrofit 2.0 的声明式网络请求
- 🔧 **依赖注入** - 集成 Hilt/Dagger 支持
- ⚡ **协程支持** - 原生支持 Kotlin 协程
- 📊 **统一配置** - 提供网络请求的统一配置管理
- 🔄 **Flow 封装** - 使用 Flow 进行数据流处理

## 📦 依赖说明

```kotlin
// 在 app/build.gradle.kts 中添加依赖
implementation(project(":commonNet"))
```

## 🏗️ 使用方式

### 1. 网络配置

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
                    .build()
                chain.proceed(request)
            },
            
            // 日志拦截器（自动添加）
            // HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            
            // 其他拦截器...
        )
    }
}
```

#### 配置依赖注入模块
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideNetworkConfig(): INetworkConfig {
        return MyNetworkConfig()
    }
}
```

### 2. 创建 API 接口

```kotlin
interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<User>
    
    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>
    
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: String, @Body user: User): Response<User>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: String): Response<Unit>
    
    @GET("users")
    suspend fun getUsers(@Query("page") page: Int, @Query("size") size: Int): Response<List<User>>
    
    @Multipart
    @POST("upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<UploadResult>
}
```

### 3. 创建 Repository

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi
) : BaseRepository() {
    
    fun getUser(userId: String): Flow<User> = request { emit(userApi.getUser(userId).body()!!) }
    
    fun getUsers(page: Int, size: Int): Flow<List<User>> = request { 
        emit(userApi.getUsers(page, size).body() ?: emptyList()) 
    }
    
    fun createUser(user: User): Flow<User> = request { emit(userApi.createUser(user).body()!!) }
    
    fun updateUser(userId: String, user: User): Flow<User> = request { 
        emit(userApi.updateUser(userId, user).body()!!) 
    }
    
    fun deleteUser(userId: String): Flow<Unit> = request { emit(userApi.deleteUser(userId).body()!!) }
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
            try {
                _loading.value = true
                userRepository.getUser(userId).collect { user ->
                    _user.value = user
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun loadUsers(page: Int = 1, size: Int = 20) {
        viewModelScope.launch {
            try {
                _loading.value = true
                userRepository.getUsers(page, size).collect { users ->
                    _users.value = users
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
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
    
    override fun initView() {
        // 设置监听器等
    }
    
    override fun initObserve() {
        // 观察 LiveData
        mViewModel.user.observe(this) { user ->
            // 更新 UI
        }
        
        mViewModel.users.observe(this) { users ->
            // 更新列表
        }
        
        mViewModel.loading.observe(this) { loading ->
            // 显示/隐藏加载状态
        }
        
        mViewModel.error.observe(this) { error ->
            // 显示错误信息
        }
    }
    
    override fun initData() {
        // 加载数据
        mViewModel.loadUser("123")
        mViewModel.loadUsers()
    }
}
```

## 🔧 高级配置

### 1. 自定义 OkHttpClient

```kotlin
@Module
@InstallIn(SingletonComponent::class)
class CustomNetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(config: INetworkConfig): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(30000, TimeUnit.MILLISECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .addInterceptor(config.getInterceptors())
            .retryOnConnectionFailure(true)
            .cache(Cache(File(context.cacheDir, "http_cache"), 10 * 1024 * 1024)) // 10MB缓存
            .build()
    }
}
```

### 2. 自定义 Gson 配置

```kotlin
@Module
@InstallIn(SingletonComponent::class)
class GsonModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Date::class.java, DateTypeAdapter())
            .create()
    }
    
    @Provides
    @Singleton
    fun provideConverterFactory(gson: Gson): Converter.Factory {
        return GsonConverterFactory.create(gson)
    }
}
```

### 3. 多环境配置

```kotlin
enum class Environment {
    DEV, STAGING, PROD
}

object NetworkConfigManager {
    private val currentEnvironment = Environment.DEV
    
    fun getConfig(): INetworkConfig {
        return when (currentEnvironment) {
            Environment.DEV -> DevNetworkConfig()
            Environment.STAGING -> StagingNetworkConfig()
            Environment.PROD -> ProdNetworkConfig()
        }
    }
}

class DevNetworkConfig : BaseNetworkConfig() {
    override fun getBaseUrl(): String = "https://dev-api.example.com/"
}

class StagingNetworkConfig : BaseNetworkConfig() {
    override fun getBaseUrl(): String = "https://staging-api.example.com/"
}

class ProdNetworkConfig : BaseNetworkConfig() {
    override fun getBaseUrl(): String = "https://api.example.com/"
}
```

## 🔒 安全配置

### 1. HTTPS 配置

```kotlin
fun getSecureOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .sslSocketFactory(sslSocketFactory, trustAllCerts)
        .hostnameVerifier { _, _ -> true } // 生产环境不要使用
        .build()
}
```

### 2. 证书固定

```kotlin
fun getPinningOkHttpClient(context: Context): OkHttpClient {
    val certificatePinner = CertificatePinner.Builder()
        .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build()
    
    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .build()
}
```

## 📝 API 响应处理

### 1. 统一响应格式

```kotlin
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class ApiError(
    val code: Int,
    val message: String
)
```

### 2. 错误处理

```kotlin
class ApiErrorHandler {
    fun handleError(throwable: Throwable): ApiError {
        return when (throwable) {
            is HttpException -> {
                val errorBody = throwable.response()?.errorBody()?.string()
                // 解析错误信息
                ApiError(throwable.code(), errorBody ?: "Unknown error")
            }
            is SocketTimeoutException -> ApiError(-1, "请求超时")
            is UnknownHostException -> ApiError(-2, "网络连接失败")
            else -> ApiError(-999, "未知错误: ${throwable.message}")
        }
    }
}
```

## 🧪 测试

### 1. Mock API

```kotlin
@ExperimentalCoroutinesApi
class UserRepositoryTest {
    
    @Mock
    private lateinit var userApi: UserApi
    
    private lateinit var userRepository: UserRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        userRepository = UserRepository(userApi)
    }
    
    @Test
    fun testGetUser() = runTest {
        // Given
        val expectedUser = User("1", "Test User")
        whenever(userApi.getUser("1")).thenReturn(Response.success(expectedUser))
        
        // When
        val result = userRepository.getUser("1").first()
        
        // Then
        assertEquals(expectedUser, result)
    }
}
```

## 🚨 注意事项

1. **网络权限**: 确保在 AndroidManifest.xml 中添加网络权限
2. **主线程网络请求**: Android 9+ 不允许在主线程进行网络请求
3. **HTTPS**: 生产环境建议使用 HTTPS
4. **错误处理**: 始终处理网络请求的异常情况
5. **超时设置**: 根据实际情况设置合适的超时时间

## 📱 版本要求

- **最低 SDK**: 24 (Android 7.0)
- **编译 SDK**: 36 (Android 14)
- **Retrofit**: 3.0.0
- **OkHttp**: 5.1.0
- **Kotlin**: 1.9+

## 📄 许可证

本项目采用 MIT 许可证。