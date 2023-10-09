package io.github.mmarco94.compose.gtk.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import io.github.mmarco94.compose.*
import io.github.mmarco94.compose.modifier.Modifier
import org.gnome.adw.CenteringPolicy
import org.gnome.adw.HeaderBar
import org.gnome.gtk.Widget


@Composable
fun HeaderBar(
    modifier: Modifier = Modifier,
    centeringPolicy: CenteringPolicy = CenteringPolicy.LOOSE,
    title: (@Composable () -> Unit)? = null,
    startWidgets: @Composable () -> Unit = {},
    endWidgets: @Composable () -> Unit = {},
) {
    ComposeNode<GtkComposeNode<HeaderBar>, GtkApplier>(
        {
            VirtualComposeNodeContainer(HeaderBar.builder().build())
        },
        update = {
            set(modifier) { applyModifier(it) }
            set(centeringPolicy) { this.gObject.centeringPolicy = it }
        },
        content = {
            Pack({ packStart(it) }, startWidgets)
            if (title != null) {
                Title {
                    title()
                }
            }
            Pack({ packEnd(it) }, endWidgets)
        },
    )
}

@Composable
private fun Pack(
    packer: HeaderBar.(Widget) -> Unit,
    content: @Composable () -> Unit = {},
) {
    ComposeNode<GtkComposeNode<Nothing?>, GtkApplier>(
        {
            VirtualComposeNode<HeaderBar> { header ->
                GtkContainerComposeNode.appendOnly<HeaderBar, Widget>(
                    header,
                    add = { packer(it) },
                    remove = { remove(it) },
                )
            }
        },
        update = {},
        content = content,
    )
}

@Composable
private fun Title(
    title: @Composable () -> Unit = {},
) {
    ComposeNode<GtkComposeNode<Nothing?>, GtkApplier>(
        {
            VirtualComposeNode<HeaderBar> { header ->
                SingleChildComposeNode(
                    header,
                    add = { titleWidget = it.gObject as Widget },
                    remove = { titleWidget = null },
                )
            }
        },
        update = {},
        content = title,
    )
}