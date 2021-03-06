package sidev.lib.collection.sequence

import sidev.lib.`val`.SuppressLiteral
import sidev.lib.collection.iterator.emptyNestedIteratorSimple
import sidev.lib.collection.iterator.NestedIterator

object EmptyNestedSequence: NestedSequence<Any?> {
    override fun iterator(): NestedIterator<*, Any?> = emptyNestedIteratorSimple()
}

@Suppress(SuppressLiteral.UNCHECKED_CAST)
fun <O> emptyNestedSequence(): NestedSequence<O> = EmptyNestedSequence as NestedSequence<O>