package io.github.mmarco94.compose

import io.github.mmarco94.compose.modifier.Modifier
import io.github.mmarco94.compose.utils.inPlaceMap
import org.gnome.adw.Dialog
import org.gnome.gobject.GObject
import org.gnome.gtk.Widget
import org.gnome.gtk.Window

/**
 * A base node in the Compose tree.
 *
 * TODO: can we achieve the same using composition instead of inheritance?
 */
interface GtkComposeNode {
    fun addNode(index: Int, child: GtkComposeNode)
    fun removeNode(index: Int)
    fun clearNodes()
}

/**
 * A node in the Compose tree that corresponds to a GTK [Widget].
 */
abstract class GtkComposeWidget<out W : Widget>(val widget: W) : GtkComposeNode {
    var modifier: Modifier = Modifier

    fun applyModifier(modifier: Modifier) {
        this.modifier.undo(widget)
        this.modifier = modifier
        this.modifier.apply(widget)
    }
}

/**
 * A node in the Compose tree that corresponds to a GTK [Widget] that can contain other widgets.
 * By default, this class handles the addition of "phantom children", for example Dialogs and Windows.
 * TODO: should phantom children be "propagated" to the parent, until there's a parent that can handle them?
 */
abstract class GtkComposeContainer<out W : Widget>(widget: W) : GtkComposeWidget<W>(widget) {
    private val phantomChildren = mutableListOf<Int>()

    protected open fun canBeAttached(child: Widget): Boolean {
        return child !is Dialog && child !is Window
    }

    private fun toAttachableIndex(index: Int): Int {
        return index - phantomChildren.count { it < index }
    }

    override fun addNode(index: Int, child: GtkComposeNode) {
        if (child is GtkComposeWidget<*> && canBeAttached(child.widget)) {
            add(toAttachableIndex(index), child)
        } else {
            phantomChildren.inPlaceMap {
                when {
                    it >= index -> it + 1
                    else -> it
                }
            }
            phantomChildren.add(index)
        }
    }

    override fun removeNode(index: Int) {
        val iof = phantomChildren.indexOf(index)
        if (iof >= 0) {
            phantomChildren.removeAt(iof)
        } else {
            remove(toAttachableIndex(index))
        }
    }

    override fun clearNodes() {
        phantomChildren.clear()
        clear()
    }

    protected abstract fun add(index: Int, child: GtkComposeWidget<Widget>)
    protected abstract fun remove(index: Int)
    protected abstract fun clear()
}

internal open class LeafComposeNode<G : Widget>(gObject: G) : GtkComposeWidget<G>(gObject) {
    override fun addNode(index: Int, child: GtkComposeNode) = throw UnsupportedOperationException()
    override fun removeNode(index: Int) = throw UnsupportedOperationException()
    override fun clearNodes() = throw UnsupportedOperationException()
}

internal open class SingleChildComposeNode<W : Widget>(
    widget: W,
    val set: W.(Widget?) -> Unit,
) : GtkComposeContainer<W>(widget) {
    private val stack = mutableListOf<Widget>()

    private fun recompute() {
        val widget = stack.lastOrNull()
        this.widget.set(widget)
    }

    override fun add(index: Int, child: GtkComposeWidget<Widget>) {
        stack.add(child.widget)
        recompute()
    }

    override fun remove(index: Int) {
        stack.removeFirst()
        recompute()
    }

    override fun clear() {
        stack.clear()
        recompute()
    }
}

internal class VirtualComposeNode<G : GObject>(
    val nodeCreator: (G) -> GtkComposeNode,
) : GtkComposeNode {
    private var parentCreator: GtkComposeNode? = null
    private val children = mutableListOf<GtkComposeNode>()

    override fun addNode(index: Int, child: GtkComposeNode) {
        children.add(index, child)
        parentCreator?.addNode(index, child)
    }

    override fun removeNode(index: Int) {
        children.removeAt(index)
        parentCreator?.removeNode(index)
    }

    override fun clearNodes() {
        children.clear()
        parentCreator?.clearNodes()
    }

    fun setParent(parent: G?) {
        parentCreator?.clearNodes()
        parentCreator = if (parent != null) {
            nodeCreator(parent).also {
                children.forEachIndexed { index, child -> it.addNode(index, child) }
            }
        } else {
            null
        }
    }
}

internal class VirtualComposeNodeContainer<W : Widget>(widget: W) : GtkComposeWidget<W>(widget) {
    private val children = mutableListOf<VirtualComposeNode<W>>()
    override fun addNode(index: Int, child: GtkComposeNode) {
        child as VirtualComposeNode<W>
        children.add(index, child)
        child.setParent(widget)
    }

    override fun removeNode(index: Int) {
        children.removeAt(index).setParent(null)
    }

    override fun clearNodes() {
        children.forEach { it.setParent(null) }
        children.clear()
    }
}

internal abstract class GtkContainerComposeNode<W : Widget, C : Widget>(widget: W) : GtkComposeContainer<W>(widget) {
    private val _children = mutableListOf<C>()
    protected val children: List<C> = _children
    override fun add(index: Int, child: GtkComposeWidget<Widget>) {
        val childWidget = child.widget as C
        _children.add(index, childWidget)
    }

    override fun remove(index: Int) {
        _children.removeAt(index)
    }

    override fun clear() {
        _children.clear()
    }

    companion object {
        fun <W : Widget, C : Widget> appendOnly(
            widget: W,
            add: W.(C) -> Unit,
            remove: W.(C) -> Unit,
        ) = object : GtkContainerComposeNode<W, C>(widget) {
            override fun add(index: Int, child: GtkComposeWidget<Widget>) {
                val toReinsert = children.drop(index)
                super.add(index, child)
                toReinsert.forEach { widget.remove(it) }
                widget.add(child.widget as C)
                toReinsert.forEach { widget.add(it) }
            }

            override fun remove(index: Int) {
                widget.remove(children[index])
                super.remove(index)
            }

            override fun clear() {
                children.forEach { widget.remove(it) }
                super.clear()
            }
        }
    }
}
