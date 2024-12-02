package com.puntogris.telescope.utils

import org.w3c.dom.Node
import org.w3c.dom.NodeList

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 * https://github.com/neworld/vd2svg/blob/master/src/main/kotlin/lt/neworld/vd2svg/resources/AndroidResourceParser.kt
 * not working in most cases
 */

private class NodeListIterator(private val nodeList: NodeList) : Iterator<Node> {
    private var position = 0

    override fun hasNext(): Boolean {
        return position < nodeList.length
    }

    override fun next() = nodeList.item(position++)!!
}

val NodeList.iterable: Iterable<Node>
    get() {
        return object : Iterable<Node> {
            override fun iterator(): Iterator<Node> {
                return iterator
            }
        }
    }

val NodeList.iterator: Iterator<Node>
    get() = NodeListIterator(this)