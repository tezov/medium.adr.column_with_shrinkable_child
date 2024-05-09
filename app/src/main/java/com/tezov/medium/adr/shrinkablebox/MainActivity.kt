package com.tezov.medium.adr.shrinkablebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tezov.medium.adr.shrinkablebox.ui.theme.ShrinkableboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShrinkableboxTheme {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    val reverseAnimation = remember { mutableStateOf(false) }
                    val animatedValue = animateFloatAsState(
                        label = "animation",
                        targetValue = if (reverseAnimation.value) 1.0f else 0.0f,
                        animationSpec = tween(
                            durationMillis = if (reverseAnimation.value) 1500 else 2000,
                            easing = if (reverseAnimation.value) FastOutLinearInEasing else LinearOutSlowInEasing,
                        ),
                        finishedListener = {
                            reverseAnimation.value = !reverseAnimation.value
                        }
                    )
                    LaunchedEffect(Unit) { reverseAnimation.value = true }


                    ColumnWithShrinkable(
                        modifier = Modifier
                            .background(Color.Green)
                            .align(Alignment.Center)
                            .width(IntrinsicSize.Max)

                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Blue)
                                .fillMaxWidth()
                                .height(12.dp)
                        )

                        Text(
                            modifier = Modifier
                                .shrink(animatedValue.value),
                            text = "Hello World",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Box(
                            modifier = Modifier
                                .background(Color.Red)
                                .fillMaxWidth()
                                .height(12.dp)
                        )
                    }

                }
            }
        }
    }
}
