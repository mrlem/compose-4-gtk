package io.github.mmarco94.compose.adw.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import io.github.mmarco94.compose.GtkApplier
import io.github.mmarco94.compose.GtkComposeWidget
import io.github.mmarco94.compose.SingleChildComposeNode
import io.github.mmarco94.compose.modifier.Modifier
import org.gnome.adw.Clamp
import org.gnome.gtk.Orientation


@Composable
fun VerticalClamp(
    modifier: Modifier = Modifier,
    maximumSize: Int = 600,
    tighteningThreshold: Int = 400,
    content: @Composable () -> Unit,
) {
    Clamp(modifier, Orientation.VERTICAL, maximumSize, tighteningThreshold, content)
}

@Composable
fun HorizontalClamp(
    modifier: Modifier = Modifier,
    maximumSize: Int = 600,
    tighteningThreshold: Int = 400,
    content: @Composable () -> Unit,
) {
    Clamp(modifier, Orientation.HORIZONTAL, maximumSize, tighteningThreshold, content)
}

@Composable
fun Clamp(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.HORIZONTAL,
    maximumSize: Int = 600,
    tighteningThreshold: Int = 400,
    content: @Composable () -> Unit,
) {
    ComposeNode<GtkComposeWidget<Clamp>, GtkApplier>(
        factory = {
            SingleChildComposeNode(
                Clamp.builder().build(),
                set = { child = it },
            )
        },
        update = {
            set(modifier) { applyModifier(it) }
            set(orientation) { this.widget.orientation = it }
            set(maximumSize) { this.widget.maximumSize = it }
            set(tighteningThreshold) { this.widget.tighteningThreshold = it }
        },
        content = content,
    )
}