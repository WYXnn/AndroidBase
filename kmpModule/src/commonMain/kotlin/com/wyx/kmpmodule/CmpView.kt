package com.wyx.kmpmodule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CmpView() {

    val mViewModel : SharedPlatformViewModel = SharedPlatformViewModel

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Hello from Compose Multiplatform!")
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                mViewModel.onToastButtonClicked(PlatformAction.needCallback("message from cmp") {
                    print("wyx111 from native callback:$it")
                })


            }) {
                Text("Go to Native Screen")
            }
        }
    }

}