import org.gradle.api.publish.PublishingExtension

// 1. 确保应用了插件
apply(plugin = "maven-publish")

// 2. 使用 configure<PublishingExtension> 代替直接写 publishing
configure<PublishingExtension> {
    publications {
        create<MavenPublication>("release") {
            // 使用 afterEvaluate 确保在模块配置完成后再获取 components
            afterEvaluate {
                from(components["release"])
            }

            groupId = "com.github.WYXnn"
            // artifactId 会自动取模块名，除非你想手动指定：
            // artifactId = "custom-name"
            version = "0.1.0"
        }
    }
}