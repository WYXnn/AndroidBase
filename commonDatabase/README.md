# commonDatabase

数据库管理模块，基于Room和SQLCipher提供安全的本地数据存储功能。

## 功能概述

本模块提供基于Room数据库的基础DAO类封装，支持SQLCipher加密，简化数据库操作。提供通用的增删改查方法，支持Flow流式查询。

## 依赖说明

在app模块的build.gradle.kts中添加：

```kotlin
implementation(project(":commonDatabase"))
```

## 使用方式

### 1. 创建实体类

```kotlin
@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "age")
    val age: Int
)
```

### 2. 创建DAO接口

```kotlin
@Dao
interface UserDao : BaseDao<User> {
    
    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getUserById(id: Long): User?
    
    @Query("SELECT * FROM user WHERE name = :name")
    fun getUserByNameFlow(name: String): Flow<User?>
    
    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("DELETE FROM user WHERE age < :age")
    suspend fun deleteUsersUnderAge(age: Int): Int
}
```

### 3. 创建数据库

```kotlin
@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### 4. 在Repository中使用

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    
    // 插入用户
    suspend fun insertUser(user: User): Long {
        return userDao.insert(user)
    }
    
    // 批量插入用户
    suspend fun insertUsers(users: List<User>): List<Long> {
        return userDao.insert(users)
    }
    
    // 更新用户
    suspend fun updateUser(user: User): Int {
        return userDao.update(user)
    }
    
    // 删除用户
    suspend fun deleteUser(user: User) {
        userDao.delete(user)
    }
    
    // 获取用户流
    fun getUserFlow(id: Long): Flow<User?> {
        return userDao.getUserByIdFlow(id)
    }
    
    // 获取所有用户
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
    
    // 清空所有数据
    suspend fun deleteAllUsers(): Int {
        return userDao.deleteAll()
    }
}
```

### 5. 在ViewModel中使用

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    init {
        loadUsers()
    }
    
    private fun loadUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { userList ->
                _users.value = userList
            }
        }
    }
    
    fun addUser(name: String, email: String, age: Int) {
        viewModelScope.launch {
            val user = User(name = name, email = email, age = age)
            userRepository.insertUser(user)
        }
    }
    
    fun updateUser(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
        }
    }
    
    fun deleteUser(user: User) {
        viewModelScope.launch {
            userRepository.deleteUser(user)
        }
    }
}
```

## BaseDao提供的功能

### 基础CRUD操作

```kotlin
// 插入单个对象
suspend fun insert(obj: T): Long

// 插入多个对象
suspend fun insert(vararg objs: T): LongArray?

// 插入对象列表
suspend fun insert(personList: List<T>): List<Long>

// 根据主键删除对象
suspend fun delete(obj: T)

// 根据主键更新对象
suspend fun update(vararg obj: T): Int

// 删除所有数据
suspend fun deleteAll(): Int

// 获取表名
val tableName: String
```

## 高级功能

### 1. 自定义查询

```kotlin
@Dao
interface CustomDao : BaseDao<YourEntity> {
    
    // 复杂查询
    @Query("SELECT * FROM user WHERE age BETWEEN :minAge AND :maxAge ORDER BY name")
    fun getUsersByAgeRange(minAge: Int, maxAge: Int): Flow<List<User>>
    
    // 原始查询
    @RawQuery(observedEntities = [User::class])
    fun getUsersByRawQuery(query: SupportSQLiteQuery): Flow<List<User>>
}
```

### 2. 事务操作

```kotlin
@Dao
interface TransactionDao : BaseDao<User> {
    
    @Transaction
    suspend fun updateUserAndLog(user: User, log: UserLog) {
        update(user)
        insert(log)
    }
}
```

## 数据库配置

### Room配置选项

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "database_name")
    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // WAL模式
    .enableMultiInstanceInvalidation() // 多实例失效
    .fallbackToDestructiveMigration() // 破坏性迁移
    .addMigrations(MIGRATION_1_2) // 数据库迁移
    .build()
```

## 注意事项

1. 所有数据库操作必须在协程中执行
2. BaseDao中的deleteAll()方法会删除表中的所有数据
3. 使用Flow查询时记得在ViewModel中正确管理生命周期
4. 主键使用@Entity中的@PrimaryKey注解标记
5. 数据库操作应该是线程安全的

## 版本要求

- Android API Level: 21+
- Room: 2.6.0+
- SQLCipher: 4.5.0+
- Kotlin Coroutines: 1.6.0+

## 依赖模块

- Room数据库框架
- SQLCipher加密库（可选）
- Kotlin协程