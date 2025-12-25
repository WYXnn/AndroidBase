package com.wyx.examplebase.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.wyx.examplebase.eventbus.EventBusUtil
import com.wyx.examplebase.eventbus.RegisterEventBus
import com.wyx.commonnet.viewmodel.BaseViewModel

abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {

    lateinit var mViewBinding: VB
    private var mHaveRegisterEventBus = false
    protected abstract val mViewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = createVB()
        setContentView(mViewBinding.root)

        if (javaClass.isAnnotationPresent(RegisterEventBus::class.java) && !mHaveRegisterEventBus) {
            mHaveRegisterEventBus = true
            EventBusUtil.register(this)
        }

        initView()
        initObserve()
        initData()

    }

    /**
     * 创建 [ViewBinding] 实例
     * 布局由各自BaseActivity的实现类来提供
     */
    abstract fun createVB(): VB

    abstract fun initView()

    /**
     * 订阅 [LiveData]
     */
    abstract fun initObserve()

    abstract fun initData()

    override fun onDestroy() {
        // 根据子类是否有 RegisterEventBus 注解决定是否进行注册 EventBus
        if (mHaveRegisterEventBus) {
            EventBusUtil.unregister(this)
        }
        super.onDestroy()
    }

}