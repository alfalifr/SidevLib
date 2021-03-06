package sidev.lib.collection

import sidev.lib.`val`.SuppressLiteral
import sidev.lib.collection.array.arrayCopy
import sidev.lib.collection.array.trimNulls
import sidev.lib.collection.array.trimToSize

abstract class VectorImpl<T>(initCapacity: Int): Vector<T> {
    constructor(): this(10)

    var capacityIncrement= 10
    protected var array: Array<Any?> = arrayOfNulls(initCapacity)

    /**
     * Jumlah elemen yg sesungguhnya yg merupakan hasil penambahan dari [add] yg ada di [array].
     * Property ini sama dg [elementCount].
     */
    final override var size: Int
        get()= elementCount
        private set(v){
            elementCount= v
        }

    /**
     * Index ujung yg digunakan oleh fungsi [peek], [pop], dan [push].
     */
    var cursorInd: Int= -1
        private set
    /**
     * Jumlah elemen yg sesungguhnya yg merupakan hasil penambahan dari [add] yg ada di [array].
     */
    var elementCount: Int= 0
        private set


    override fun remove(element: T): Boolean {
        for(i in 0 until elementCount)
            if(array[i] == element){
                removeAt(i)
                return true
            }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var bool= false
        for(removedE in elements){
            bool= bool || remove(removedE)
        }
        return bool
    }

    override fun clear() {
        for(i in 0 until elementCount)
            array[i]= null
        elementCount= 0
        cursorInd= 0
    }

    override fun removeAt(index: Int): T {
        val item= array[index]
        val len= size - index - 1

        if(len > 0){
            arrayCopy(array, index +1, array, index, len)
        }

        cursorInd= popIndex(cursorInd, elementCount, index)
        array[--elementCount]= null

        @Suppress(SuppressLiteral.UNCHECKED_CAST)
        return item as T
    }

    override fun add(index: Int, element: T) {
        if(index > elementCount)
            throw IndexOutOfBoundsException("size= $elementCount < index= $index")

        if(elementCount == array.size)
            grow()

        if(index < elementCount)
            arrayCopy(array, index, array, index +1, elementCount -index)

        cursorInd= pushIndex(cursorInd, elementCount, index)
        array[index]= element
        elementCount++
    }

    override fun add(element: T): Boolean {
        if(elementCount == array.size)
            grow()

        val currInd= cursorInd
        cursorInd= pushIndex(currInd, elementCount, currInd)
        array[currInd]= element
        elementCount++
        return true
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        if(index > elementCount)
            throw IndexOutOfBoundsException("size= $elementCount < index= $index")

        val totalLen= elementCount +elements.size
        if(totalLen > array.size)
            grow(totalLen)

        val addedCount= elements.size
        cursorInd= pushIndex(cursorInd, elementCount, index, addedCount)

        val len= elementCount -index
        arrayCopy(array, index, array, len, len)
        elementCount += addedCount
        return true
    }
    override fun addAll(elements: Collection<T>): Boolean = addAll(elementCount, elements)


    override fun trimNulls() { array.trimNulls(end = elementCount) }
    override fun trimToSize() {
        if(array.size > elementCount)
            array= array.trimToSize(elementCount)
    }

    override fun contains(element: T): Boolean = array.contains(element)
    override fun containsAll(elements: Collection<T>): Boolean =
        @Suppress(SuppressLiteral.UNCHECKED_CAST) (array as Array<T>).all { contains(it) }

    override fun get(index: Int): T =
        @Suppress(SuppressLiteral.UNCHECKED_CAST) (array[index] as T)
    override fun set(index: Int, element: T): T {
        val prev= array[index]
        array[index]= element

        @Suppress(SuppressLiteral.UNCHECKED_CAST)
        return prev as T
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val retainedSet= mutableSetOf<T>()

        for(retained in elements)
            if(retained in this)
                retainedSet += retained

        array= arrayOfNulls(retainedSet.size)

        for((i, retained) in retainedSet.withIndex())
            array[i]= retained

        cursorInd= pushIndex(0, 0, retainedSet.size)
        elementCount= retainedSet.size
        return elementCount != 0
    }

    override fun indexOf(element: T): Int = array.indexOf(element)
    override fun lastIndexOf(element: T): Int = array.lastIndexOf(element)

    override fun isEmpty(): Boolean = elementCount == 0

    override fun peek(): T =
        @Suppress(SuppressLiteral.UNCHECKED_CAST) (array[cursorInd] as T)
    override fun pop(): T = removeAt(cursorInd)
    override fun push(item: T): T{
        cursorInd= pushIndex(cursorInd, size, cursorInd)
        add(cursorInd, item)
        return item
    }

    /**
     * Iterasi dilakukan sesuai arah dari [queueMode].
     */
    override fun iterator(): MutableIterator<T> = object : MutableIterator<T>{
        var i= 0
        var poppedCount= 0
        override fun hasNext(): Boolean = poppedCount < elementCount
        override fun next(): T {
            @Suppress(SuppressLiteral.UNCHECKED_CAST)
            val item= array[popIndex(i, elementCount -poppedCount, i).also { i= it }] as T
            poppedCount++
            return item
        }
        override fun remove() {
            removeAt(i)
        }
    }
    override fun listIterator(): MutableListIterator<T> = listIterator(0)
    override fun listIterator(index: Int): MutableListIterator<T> = object : MutableListIterator<T>{
        var i= index
        var popInd= index
        var pushInd= index
        var poppedCount= 0
        override fun hasNext(): Boolean = poppedCount < elementCount
        override fun hasPrevious(): Boolean = poppedCount > 0

        override fun nextIndex(): Int = popIndex(i, elementCount -poppedCount, i).also { popInd= it }
        override fun previousIndex(): Int = pushIndex(i, elementCount -poppedCount, i).also { pushInd= it }

        override fun next(): T {
            i= nextIndex()
            poppedCount++

            @Suppress(SuppressLiteral.UNCHECKED_CAST)
            return array[popInd] as T
        }
        override fun previous(): T {
            i= previousIndex()
            poppedCount--

            @Suppress(SuppressLiteral.UNCHECKED_CAST)
            return array[pushInd] as T
        }
        override fun add(element: T) {
            this@VectorImpl.add(i++, element)
        }
        override fun remove() {
            removeAt(--i)
        }
        override fun set(element: T) {
            this@VectorImpl[i]= element
        }
    }

    private fun grow(minCapacity: Int= 1){
        val increment=
            if(capacityIncrement > minCapacity) capacityIncrement
            else minCapacity + capacityIncrement / 2 //ditambah 50% dari `capacityIncrement` agar proses add selanjutnya gak grow() lagi.

        val newLen= array.size +increment
        val newArray= arrayOfNulls<Any>(newLen) //as Array<T?>

        arrayCopy(array, 0, newArray, 0, newLen)
        array= newArray
    }
}