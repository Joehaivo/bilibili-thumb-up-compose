package com.github.joehaivo.bilibili_thumb_up_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.joehaivo.bilibili_thumb_up_compose.ui.theme.BilibilithumbupcomposeTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BilibilithumbupcomposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    TripleHit(66, 77, 88)
                }
            }
        }
    }

    @Composable
    fun TripleHit(thumbInitial: Long = 0, coinInitial: Long = 0, starInitial: Long = 0) {
        // 三个图标的初始值
        var thumbCount by remember { mutableStateOf(thumbInitial) }
        var coinCount by remember { mutableStateOf(coinInitial) }
        var starCount by remember { mutableStateOf(starInitial) }
        // 三个图标的高亮状态
        var thumbed by remember { mutableStateOf(false) }
        var coined by remember { mutableStateOf(false) }
        var started by remember { mutableStateOf(false) }
        // 动画（副作用）所在的协程作用域
        val scope = rememberCoroutineScope()
        // 三连进度 从0到-360度，逆时针
        var hitProgress by remember { mutableStateOf(0) }
        var hitJob by remember { mutableStateOf<Job?>(null) }
        Row(modifier = Modifier.padding(20.dp)) {
            ConstraintLayout(modifier = Modifier
                .pointerInput(Unit) {
                    forEachGesture {
                        awaitPointerEventScope {
                            awaitFirstDown()
                            // ACTION_DOWN here
                            hitJob?.cancel()
                            hitJob = scope.async {
                                // 手指按下后，逐步减少hitProgress，使其角度逆时针增加
                                while (hitProgress > -360) {
                                    delay(15)
                                    hitProgress -= 4
                                }
                                thumbCount += 1
                                coinCount += 1
                                starCount += 1
                                thumbed = true
                                coined = true
                                started = true
                                hitProgress = 0
                            }
                            do {

                                val event: PointerEvent = awaitPointerEvent()
                                // ACTION_MOVE loop

                                // Consuming event prevents other gestures or scroll to intercept
                                event.changes.forEach { pointerInputChange: PointerInputChange ->
                                    if (pointerInputChange.positionChange() != Offset.Zero) pointerInputChange.consume()
                                }
                            } while (event.changes.any { it.pressed })

                            // ACTION_UP is here
                            hitJob?.cancel()
                            hitJob = scope.async {
                                while (hitProgress < 0) {
                                    delay(8)
                                    hitProgress += 4
                                }
                            }
                        }
                    }
                }
            ) {
                // 点赞+数量
                val (refThumb, refCounter) = createRefs()
                Icon(Icons.Default.ThumbUp, "", tint = if (thumbed) Color(0xfffe669b) else Color(0xff60676a),
                    modifier = Modifier
                        .constrainAs(refThumb) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                        }
                        .size(40.dp)
                )
                Text(thumbCount.toString(),
                    modifier = Modifier.constrainAs(refCounter) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(refThumb.bottom)
                    })
            }
            // 投币+数量
            ConstraintLayout(modifier = Modifier.padding(start = 10.dp)) {
                val (refCoin, refCounter) = createRefs()
                Box(modifier = Modifier
                    .constrainAs(refCoin) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
                    .size(40.dp)
                    .drawBehind {
                        // 画一段圆弧，从-90度开始，扫过hitProgress的角度
                        drawArc(
                            color = Color(0xfffe669b),
                            startAngle = -90f,
                            sweepAngle = hitProgress.toFloat(),
                            size = Size(size.width, size.width),
                            style = Stroke(width = 7f),
                            useCenter = false
                        )
                    }, contentAlignment = Alignment.Center
                )
                {
                    Icon(Icons.Default.AccountCircle, "", tint = if (thumbed) Color(0xfffe669b) else Color(0xff60676a), modifier = Modifier.size(38.dp))
                }
                Text(coinCount.toString(),
                    modifier = Modifier.constrainAs(refCounter) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(refCoin.bottom)
                    })
            }
            // 收藏+数量
            ConstraintLayout(modifier = Modifier.padding(start = 10.dp)) {
                val (refStar, refCounter) = createRefs()
                Box(modifier = Modifier
                    .constrainAs(refStar) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
                    .size(40.dp)
                    .drawBehind {
                        drawArc(
                            color = Color(0xfffe669b),
                            startAngle = -90f,
                            sweepAngle = hitProgress.toFloat(),
                            size = Size(size.width, size.width),
                            style = Stroke(width = 7f),
                            useCenter = false
                        )
                    }, contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, "", tint = if (thumbed) Color(0xfffe669b) else Color(0xff60676a), modifier = Modifier.size(38.dp))
                }
                Text(starCount.toString(),
                    modifier = Modifier.constrainAs(refCounter) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(refStar.bottom)
                    })
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ThumbUpPreview() {
        TripleHit(77, 88, 99)
    }
}

