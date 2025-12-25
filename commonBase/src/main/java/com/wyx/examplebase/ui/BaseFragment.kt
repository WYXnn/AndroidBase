package com.wyx.examplebase.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.wyx.examplebase.eventbus.EventBusUtil
import com.wyx.examplebase.eventbus.RegisterEventBus
import com.wyx.commonnet.viewmodel.BaseViewModel

abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {

    lateinit var mViewBinding: VB

    private var mHaveRegisterEventBus = false

    protected abstract val mViewModel: VM

    protected var mIsShowed: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewBinding = createVB()
        return mViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 根据子类是否有 RegisterEventBus 注解決定是否进行注册 EventBus
        if (javaClass.isAnnotationPresent(RegisterEventBus::class.java)) {
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

    override fun onResume() {
        super.onResume()
        setFragmentShow(true)
    }

    override fun onPause() {
        setFragmentShow(false)
        super.onPause()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        setFragmentShow(isVisibleToUser)
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setFragmentShow(!hidden)
    }

    protected fun onFragHidden() {
        if (isAdded() && getChildFragmentManager() != null && getChildFragmentManager().getFragments() != null) {
            for (fragment in getChildFragmentManager().getFragments()) {
                if (fragment != null && fragment is BaseFragment<*, *> && fragment.isVisible()) {
                    fragment.setFragmentShow(false)
                }
            }
        }
    }

    protected fun onFragmentReenter(resultCode: Int, data: Intent?) {
        if (isAdded() && getChildFragmentManager() != null && getChildFragmentManager().getFragments() != null) {
            for (fragment in getChildFragmentManager().getFragments()) {
                if (fragment != null && fragment is BaseFragment<*, *> && fragment.isVisible()) {
                    fragment.onFragmentReenter(resultCode, data)
                }
            }
        }
    }

    protected fun onFragShow() {
        if (isAdded() && getChildFragmentManager() != null && getChildFragmentManager().getFragments() != null) {
            for (fragment in getChildFragmentManager().getFragments()) {
                if (fragment != null && fragment is BaseFragment<*, *> && fragment.isVisible()) {
                    fragment.setFragmentShow(true)
                }
            }
        }
    }

    fun setFragmentShow(isShowed: Boolean) {
        if (isShowed && !mIsShowed) {
            onFragShow()
        } else if (!isShowed && mIsShowed) {
            onFragHidden()
        }
        mIsShowed = isShowed
    }

    fun isShowed(): Boolean {
        return mIsShowed
    }

}