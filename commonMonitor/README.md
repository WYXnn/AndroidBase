项目的gradle文件需要添加 maven { setUrl("https://gitlab.bonree.com/BonreeSDK_TAPM/Android/raw/master") }
以及 classpath ("com.bonree.agent.android:bonree:$brsdk_version")
主module的plugin中添加 id("bonree")