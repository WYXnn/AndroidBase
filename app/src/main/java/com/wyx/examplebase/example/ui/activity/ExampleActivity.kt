package com.wyx.examplebase.example.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.wyx.commonnet.network.INetworkConfig
import com.wyx.examplebase.example.viewmodel.ExampleViewModel
import com.wyx.examplebase.ui.BaseActivity
import com.wyx.unicombase.databinding.ActivityExampleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExampleActivity : BaseActivity<ActivityExampleBinding, ExampleViewModel>() {

    override val mViewModel by viewModels<ExampleViewModel>()

    companion object {
        fun start(context : Context) {
            val intent = Intent(context, ExampleActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.wyx.unicombase.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun createVB(): ActivityExampleBinding {
        return ActivityExampleBinding.inflate(layoutInflater)
    }

    override fun initView() {
        mViewBinding.exampleContentTv.text = mViewModel.count.toString()

        mViewBinding.examplePlusBtn.setOnClickListener {
            mViewModel.addCount()
        }

        mViewBinding.exampleToCmpBtn.setOnClickListener {
            CmpActivity.start(this)
        }

    }

    override fun initObserve() {
        lifecycleScope.launch {
            mViewModel.count.collect {
                mViewBinding.exampleContentTv.text = it.toString()
            }
        }
    }

    override fun initData() {

    }
}