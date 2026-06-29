package github.boxiaolanya2008.kc_tool.ui.anim

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import github.boxiaolanya2008.kc_tool.R

enum class LottieKind {
    Spinner, Check, Empty, Rocket, Shield, Warn
}

@Composable
fun LottieView(
    kind: LottieKind,
    size: Dp = 96.dp,
    iterations: Int = LottieConstants.IterateForever,
    modifier: Modifier = Modifier
) {
    val raw = when (kind) {
        LottieKind.Spinner -> R.raw.lottie_spinner
        LottieKind.Check -> R.raw.lottie_check
        LottieKind.Empty -> R.raw.lottie_empty
        LottieKind.Rocket -> R.raw.lottie_rocket
        LottieKind.Shield -> R.raw.lottie_shield
        LottieKind.Warn -> R.raw.lottie_warn
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(raw))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}
