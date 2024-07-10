package io.github.mmarco94.compose.modifier

import org.gnome.gtk.*

fun Modifier.hover(
    exit: () -> Unit = {},
    enter: (x: Double, y: Double) -> Unit,
): Modifier {
    val controller = EventControllerMotion.builder()
        .build()
    controller.onEnter(enter)
    controller.onLeave(exit)
    return combine(
        apply = {
            it.addController(controller)
        },
        undo = {
            it.removeController(controller)
        }
    )
}
