import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wlvpn.consumervpn.presentation.ui.theme.Dimens

data class ExtendedDimens(
    val none: Dp = 0.dp
)

val Dimens.extended: ExtendedDimens
    get() = ExtendedDimens()
