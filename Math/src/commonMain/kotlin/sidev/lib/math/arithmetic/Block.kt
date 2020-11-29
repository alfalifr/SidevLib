package sidev.lib.math.arithmetic

import sidev.lib.collection.forEachIndexed
import sidev.lib.annotation.TooExpensiveForBackingField
import sidev.lib.collection.addIfAbsent
import sidev.lib.collection.copy
import sidev.lib.collection.indexOfWhere
import sidev.lib.console.prine
import sidev.lib.console.prinw
import sidev.lib.exception.IllegalArgExc
import sidev.lib.exception.IllegalStateExc
import sidev.lib.exception.UnavailableOperationExc
import sidev.lib.number.*
import sidev.lib.reflex.getHashCode
import sidev.lib.structure.data.Copyable
import sidev.lib.text.*
import kotlin.jvm.JvmStatic
import kotlin.random.Random


/**
 * Kumpulan operasi matematika yang berada pada tempat yg sama yg dipisahkan oleh tanda ekuasi.
 * Satu blok dapat terdiri dari bbrp nested blok (yg dipisahkan dg tanda kurung).
 */
interface Block: Calculable, Copyable<Block> {
    companion object {
        /**
         * Mengubah [blockStr] menjadi [Block].
         * [blockStr] tidak boleh mengandung tanda persamaan / pertidak-samaan karena
         * [blockStr] dikhususkan untuk string yg merepresentasikan block pada salah satu sisi yang dipisahkan
         * oleh tanda persamaan/ pertidak-samaan.
         */
        @JvmStatic
        fun parse(blockStr: String): Block {
            val resBlock= blockOf()
            var block= resBlock
            var openingBracket= 0
//            var closingBracket= 0

            var currChildBlock: Block?= null
            var currOpParent: Operation?= null
            var currOp: Operation?= null
            var currSignum: Int= 1 //untuk tanda - +
            var currNum: Int?= null
            var currName: String?= null
            var currElement: Calculable?= null
            var isCurrentlyChangeBlock= false

            var i= 0
            fun Block.addCurrOperation(){
/*
                prine("addCurrOperation() i= $i ch= ${ try{blockStr[i]} catch (e: Exception){ "<IndexOutOfBound>" } }")
                prine("this= $this")
                prine("currOp= $currOp")
                prine("currNum= $currNum")
                prine("currName= $currName")
                prine("currElement awal= $currElement")
// */
                currNum = currNum?.times(currSignum)

                if(currElement == null)
                    currElement= when {
                        currName != null -> variableOf(currName!!, currNum ?: 1)
                        else -> constantOf(currNum!!)
//                        else -> currChildBlock
                    }
//                prine("currElement akhir= $currElement")

                if(currOp != null)
                    addOperation(currElement!!, currOp!!, prioritizePrecedence = false)
                else
                    setElementAt(0, currElement!!) //setFirstElement(currElement!!)

                currChildBlock= null
                currElement= null
                currName= null
                currNum= null
                currSignum= 1
            }

            val lastIndex= blockStr.length -1
            val len= blockStr.length
            while(i < len){
                val ch= blockStr[i]
//                prine("ch itr ch= $ch i= $i")
                when {
                    ch == '(' -> {
                        openingBracket++
                        currOpParent= currOp
                        currOp= null
                        block= blockOf(/*operationLevel = 10, */parentBlock = block)
                        isCurrentlyChangeBlock= true
                    }
                    ch == ')' -> {
/*
                        if(isCurrentlyChangeBlock) {
                            isCurrentlyChangeBlock= false
                            continue
                        }
 */
//                        prine("ch == ')' i= $i == Awal 1")

                        if(--openingBracket < 0)
                            throw IllegalStateExc(stateOwner = this::class, currentState = "\")\" terlalu banyak")
                        if(!isCurrentlyChangeBlock && blockStr.prevNonWhitespaceChar(i-1) != ')'){ // ini untuk kondisi saat tanda kurung yg digunakan untuk mengapit angka minus.
                            try {
                                block.addCurrOperation()
                                currElement= block
//                                prine("ch == ')' i= $i isCurrentlyChangeBlock= $isCurrentlyChangeBlock")
                            } catch (e: NullPointerException) {
//                                prine("Block.parse() e= ")
//                                e.printStackTrace()
                                throw IllegalStateExc(detMsg = "Terjadi kesalahan dalam penulisan block.")
                            }
                        }

//                        prine("ch == ')' i= $i == Awal 2")

                        currOp= currOpParent
                        currOpParent= null

                        if(currOp == null)
                            currElement= block

                        block= block.parentBlock!!
                        block.addCurrOperation()

                        isCurrentlyChangeBlock= false
                    }

                    ch.isMathOperator() -> {
//                        prine("ch == '-' && ,, currOp == null => ${currOp == null}")
                        if(ch == '-' && currOp == null/* && isCurrentlyChangeBlock*/){
//                            block= block.parentBlock!!
                            blockStr.indexOfWhere(i+1) { !it.isWhitespace() }.let {
                                if(it.isNotNegative()){
                                    val ch2= blockStr[it]
                                    when {
                                        ch2.isDigit() -> {
                                            currSignum= -1
                                            i= it-1
                                        }
                                        ch2.isLetter() -> {
                                            currNum = -1
                                            i= it-1
                                        }
                                        else -> throw IllegalStateExc(currentState = "`blockStr` tidak valid", detMsg = "`blockStr` memiliki char '$ch2' di belakang blok.")
                                    }
                                } else {
                                    throw IllegalStateExc(currentState = "`blockStr` tidak lengkap", detMsg = "`blockStr` memiliki tanda '-' di belakang blok.")
                                }
                            }
                        } else {
                            try { block.addCurrOperation() }
                            catch (e: NullPointerException) { /*abaikan karena kesalahan terjadi karena fungsi addCurrOperation() udah dipanggil duluan pas ')'*/ }

                            currOp= Operation.from(ch)
                            if(block.operationLevel > currOp.level)
                                (block as BlockImpl).operationLevel= currOp.level
                            isCurrentlyChangeBlock= false
                        }
                    }
                    ch.isDigit() -> {
                        val u = if(i < lastIndex) blockStr.indexOfWhere(i+1) { !it.isDigit() }
                        else len

                        if(u.isNotNegative()){
                            currNum= blockStr.substring(i, u).toInt()
                            i= u-1
                        }
//                        isCurrentlyChangeBlock= false
                    }
                    ch.isLetter() -> {
                        val u = if(i < lastIndex) blockStr.indexOfWhere(i+1) { !it.isLetter() }
                        else len

                        if(u.isNotNegative()){
                            currName= blockStr.substring(i, u)
                            i= u-1
                        }
//                        isCurrentlyChangeBlock= false
                    }
                }
                i++
            }
            if(openingBracket > 0)
                throw IllegalStateExc(stateOwner = this::class, currentState = "\"(\" terlalu banyak")

//            prine(" ===== akhir ===== currElement= $currElement currNum= $currNum currName= $currName")
            if(currNum != null)
                block.addCurrOperation()

            if(resBlock.elements.size == 1){
                val f= resBlock.elements.firstOrNull()
                if(f is Block) return f
            }

            return resBlock
        }
    }

    /**
     * Satu `Block` memiliki [operations] dg level yg sama dg [operationLevel].
     */
    val operationLevel: Int

    override val nInput: Int
        get() = elements.fold(0) { acc, calculable -> acc + calculable.nInput }

    /**
     * [Block] tempat `this` menempel.
     */
    val parentBlock: Block?

    /**
     * Kumpulan element [Calculable] yang terhubung dengan [operations] dg `level` yang sama.
     */
    val elements: List<Calculable>

    /**
     * Berisi nama unik variabel yang ada di dalam `this` [Block].
     */
    @TooExpensiveForBackingField(
        "Untuk ukuran aritmatika, di mana tiap elemen digunakan untuk perhitungan kecil, list ini terlalu mahal untuk disimpan di field."
    )
    val varNames: List<String>
        get()= elements.fold(mutableListOf()) { acc, calculable ->
            when(calculable){
                is Variable<*> -> {
                    acc.addIfAbsent(calculable.name)
                    acc
                }
                is Block -> (acc union calculable.varNames).toMutableList()
                else -> acc
            }
        }

    /**
     * Jumlah [elements] yang berupa [Variable].
     */
    val varCount: Int
        get() {
            var i= 0
            for(e in elements){
                if(e is Variable<*> || e.nInput == 1)
                    i++
            }
            return i
        }

    /**
     * Jumlah [elements] yang berupa [Constant].
     */
    val constCount: Int
        get() {
            var i= 0
            for(e in elements){
                if(e is Constant<*> || e.nInput == 0)
                    i++
            }
            return i
        }

    /**
     * Jumlah [elements] selain [Variable] dan [Constant].
     */
    val otherElementCount: Int
        get() {
            var i= 0
            for(e in elements){
                if(e.nInput > 2)
                    i++
            }
            return i
        }

    /**
     * Kumpulan [Operation] yang digunakan untuk mengolah [elements].
     * Jml [operations] harus n-1 jml [elements].
     * [operations] memiliki [Operation.level] yang sama untuk 1 `SubBlock`, yaitu [operationLevel].
     */
    val operations: List<Operation>

//    fun setFirstElement(element: Calculable)
    /**
     * Untuk mengganti tanda positif-negatif (+-) dari [elements] pertama.
     */
    fun setFirstElementSignum(positive: Boolean)

    /**
     * Return `Calculable` yang sebelumnya tergantikan oleh [element].
     */
    fun setElementAt(index: Int, element: Calculable): Calculable

    /**
     * Return `Operation` yang sebelumnya tergantikan oleh [operation].
     */
    fun setOperationAt(index: Int, operation: Operation, prioritizePrecedence: Boolean = true): Operation

    /**
     * Mengonfigurasi [parentBlock] milik child pada [elements] menjadi `this`.
     * Fungsi ini berguna saat terjadi pemecahan `Block` sehingga [elements] pada `Block` baru
     * memiliki [parentBlock] selain `this`.
     *
     * [parent] merupakan `Block` yang akan dipasangkan pada [parentBlock]
     */
    fun reconfigureChildParent(parent: Block? = null)

    /**
     * Return `Block` yang merupakan akar dari `this`.
     *  -Mengembalikan `this` jika `this` merupakan akar dari `Block` lainnya.
     *  -Mengembalikan [parentBlock] jika selama penambahan operasi terjadi pemecahan `this`
     *   menjadi 2 sehingga 2 pecahan tersebut haru
     */
    fun addOperation(
        element: Calculable, operation: Operation,
        elementIndex: Int = elements.size, prioritizePrecedence: Boolean= true
    ): Block

    /**
     * Menghapus [elements] pada [elementIndex] dan [operations] pada
     * [elementIndex]-1 jika [elementIndex] > 0 atau [elementIndex] jika [elementIndex] == 0.
     * Return [Pair] dari [Calculable] dan [Operation] sebelumnya pada index [elementIndex].
     * [Pair] yang direturn dapat berisi [Operation] yg `null` jika [elements.size] sebelumnya berukuran 1.
     */
    fun removeOperationAt(elementIndex: Int): Pair<Calculable, Operation?>

    /**
     * Mengahpus [elements] dan [operations] yang memiliki pasangan nilai [element] dan [operation].
     * Jika i adalah index [operation], maka i-1 adalah index [operation] jika i > 0 atau i jika i == 0.
     */
    fun removeOperation(element: Calculable, operation: Operation?): Boolean

    /**
     * Menyederhanakan `this` [Block] dengan cara mengoperasikan [elements] yang serupa
     * sehingga jml [elements] dapat berkurang dan lebih simpel.
     * Return hasil akhir penyederhanaan, dapat berupa `this` atau bentuk [Variable] atau [Constant] sederhana.
     * [n] adalah jml banyaknya iterasi yg dilakukan oleh `this` untuk melakukan proses penyederhanaan.
     * [n] berguna untuk `Block` yang mengandung banyak nested block.
     */
    fun simply(n: Int = 1): Calculable
    fun simply(): Calculable = simply(1)


    override fun plus(element: Calculable): Calculable {
        addOperation(element, Operation.PLUS)
        return this
    }

    override fun minus(element: Calculable): Calculable {
        addOperation(element, Operation.MINUS)
        return this
    }

    override fun times(element: Calculable): Calculable {
        addOperation(element, Operation.TIMES)
        return this
    }

    override fun div(element: Calculable): Calculable {
        addOperation(element, Operation.DIVIDES)
        return this
    }

    override fun rem(element: Calculable): Calculable {
        addOperation(element, Operation.MODULO)
        return this
    }

    private fun Map<String, Number>.getVarNum(varr: Variable<*>): Number = this[varr.name]
        ?: throw IllegalArgExc(paramExcepted = arrayOf("calculate() `namedNums`"), detailMsg = "Tidak terdapat nilai untuk variabel \"${varr.name}\" dg coeficient \"${varr.coeficient}\".")


    override operator fun invoke(vararg namedNums: Pair<String, Number>): Number = calculate(*namedNums)
    override fun calculate(vararg namedNums: Pair<String, Number>): Number {
        val numMap= mapOf(*namedNums)
        val elItr= elements.iterator()
        val opItr= operations.iterator()

        var res= elItr.next().let { calc ->
            val res= when(calc) {
                is Constant<*> -> calc()
                is Variable<*> -> calc(numMap.getVarNum(calc))
                else -> calc(*namedNums)
//                else -> calc(*namedNums.toArrayOf { it.second })
            }
            res
        }
//        prine("Block calculate first= $res owner= $this")

        while(elItr.hasNext()){
            val el= elItr.next()
            val op= opItr.next()
            res= op(res, when(el) {
                is Constant<*> -> el()
                is Variable<*> -> el(numMap.getVarNum(el))
                else -> el(*namedNums)
//                else -> el(*namedNums.toArrayOf { it.second })
            })
//            prine("Block calculate e= $el op= $op owner= $this")
//            prine("Block calculate res= $res owner= $this")
        }
        return res
    }

    fun resultEquals(other: Calculable): Boolean {
        val rand= Random.Default
        val limit= Random.Default.nextInt(5, 20)
        val names= varNames

        prinw("========== Tes persamaan berdasarkan hasil perhitungan dimulai n=$limit ==========")

        for(i in 0 until limit) {
            val args= mutableListOf<Pair<String, Number>>()
            for(name in names)
                args += name to rand.nextInt(100)

            prinw("========== Tes hasil #$i args= $args ==========")
            val res1= this(*args.toTypedArray())
            val res2= other(*args.toTypedArray())

            prinw("========== Hasil tes #$i res1= $res1 res2= $res2 ==========")
            if(res1 != res2){
                prinw("========== Tes perhitungan dihentikan karena hasil berbeda ==========")
                return false
            }
        }
        return true
    }

//    operator fun equals(other: Calculable): Boolean

    /*
    private fun Array<*>.getVarNum(varr: Variable<*>): Number = when(val fir= first()){
        is Pair<*, *> -> try {
            (this as Array<Pair<String, Number>>).find { it.first == varr.name }!!.second
        } catch (e: NullPointerException) {
            throw IllegalArgExc(paramExcepted = arrayOf("calculate() `namedNums`"), detailMsg = "Tidak terdapat nilai untuk variabel \"${varr.name}\" dg coeficient \"${varr.coeficient}\".")
        }
        is Number -> fir
        else -> throw IllegalStateExc(stateOwner = this::class, currentState = "Array<*>", expectedState = "Array<Pair<String, Number>> || Array<Number>")
    }
 */

/*
    @Deprecated("Gunakan calculate() dg Pair<String, Number>", level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("super.invoke(*nums)")
    )
    override fun invoke(vararg nums: Number): Number = super.invoke(*nums)

    @Deprecated("Gunakan calculate() dg Pair<String, Number>", level = DeprecationLevel.ERROR)
    override fun calculate(vararg nums: Number): Number {
        val varCount= varCount
        if(nums.size < varCount)
            throw IllegalArgExc(paramExcepted = arrayOf("numForVars"), detailMsg = "Param `numForVars`.size (${nums.size}) < varCount ($varCount)")

        val elItr= elements.iterator()
        val opItr= operations.iterator()
        val decreasingNumList= nums.toMutableList()

        var res= elItr.next().let { calc ->
            val res= when(calc) {
                is Constant<*> -> calc()
                is Variable<*> -> calc(decreasingNumList.removeFirst())
                else -> calc(*decreasingNumList.toTypedArray()).also {
                    for(i in 0 until calc.nInput)
                        decreasingNumList.removeFirst()
                }
            }
            res
        }

        prine("Block calculate first= $res owner= $this")

        while(elItr.hasNext()){
            val el= elItr.next()
            val op= opItr.next()
            res= op(res, when(el) {
                is Constant<*> -> el()
                is Variable<*> -> el(decreasingNumList.removeFirst())
                else -> el(*decreasingNumList.toTypedArray()).also {
                    for(i in 0 until el.nInput)
                        decreasingNumList.removeFirst()
                }
            })

            prine("Block calculate e= $el op= $op owner= $this")
            prine("Block calculate res= $res owner= $this")
        }
        return res
    }
 */
}

internal class BlockImpl(
    firstElement: Calculable = NullCalculable,
    override var parentBlock: Block? = null
): Block {
    override var elements: List<Calculable> = mutableListOf()
        private set
    override var operations: List<Operation> = mutableListOf()
        private set
//    override val varNames: List<String> = mutableListOf() //Ini akan mahal jika diterapkan sebagai field
//    private val duplicatingVarNames: MutableList<String> = mutableListOf()

    init {
        (elements as MutableList) += firstElement
    }

    override var operationLevel: Int = -1
/*
    override fun setFirstElement(element: Calculable) {
        (elements as MutableList)[0] = element
    }
 */

    override fun reconfigureChildParent(parent: Block?) {
        parentBlock= parent
        for(e in elements)
            if(e is Block)
                e.reconfigureChildParent(this)
    }

    override fun setElementAt(index: Int, element: Calculable): Calculable =
        (elements as MutableList).set(index, element)

    override fun setFirstElementSignum(positive: Boolean) {
        if(elements.first() == NullCalculable)
            throw IllegalStateExc(
                currentState = "Elemen pertama pada `this`.`Block`: '$this' merupakan `${NullCalculable}` sehingga tidak dapat diubah signum-nya.",
                expectedState = "Harap ganti elemen pertama dg `Calculable` yang valid."
            )

        val signum= if(positive) 1 else -1
        when(val e= elements.first()){
            is Variable<*> -> setElementAt(0, variableOf(e.name, e.coeficient * signum)) //setFirstElement
            is Constant<*> -> setElementAt(0, constantOf(e.number * signum)) //
            is Block -> setFirstElementSignum(positive)
            else -> throw UnavailableOperationExc(e::class, e, "Kelas element '$e' tidak diketahui.")
        }
    }

    override fun simply(n: Int): Calculable {
        var res: Calculable= this
        for(i in 0 until n)
            res= simply()
        return res
    }

    override fun simply(): Calculable {
//        var i= 0
//        prine("simply() operate res AWAL DW ==============")

        val isOperated= BooleanArray(elements.size)
        val uniqueEls= mutableListOf<Calculable>()
        val elHasOneDegreeUp= mutableListOf<Int>()
//        val elCounts= mutableListOf<Int>() // jml dari tiap [uniqueEl]
        val uniqueOps= mutableListOf<Operation>() // n = uniqueEl.size -1

        prine("simply() isOperated.size= ${isOperated.size}")

        fun getConFunGenerator(): (e1: Calculable) -> ((e2: Calculable) -> Boolean) = when(operationLevel){
            Operation.PLUS.level -> { e1 -> when (e1) {
                is Constant<*> -> { e2 -> e2 is Constant<*> }
                is Variable<*> -> { e2 -> e2 is Variable<*> && e2.name == e1.name }
                else -> { e2 ->
//                    if(e2 is Block) e2.simply()
                    e2.hashCode() == e1.hashCode()
                }
            }}
            Operation.TIMES.level -> { e1 -> when(e1){
                is Variable<*> -> { e2 -> e2 is Variable<*> && e2.name == e1.name }
                is Constant<*> -> { e2 -> false }  // Karena constant semuanya udah dihitung pada saat perhitungan coeficient.
                else -> { e2 ->
//                    if(e2 is Block) e2.simply()
                    e2.hashCode() == e1.hashCode()
                }
            }}
            Operation.MODULO.level -> { e1 -> { e2 -> false } }
            else -> { e1 -> when(e1){
                is Constant<*>, is Variable<*> -> { e2 -> e2 is Constant<*> }
//                is Variable<*> -> { e2 -> e2 is Constant<*> }
                else -> { e2 ->
//                    if(e2 is Block) e2.simply()
                    false 
                }
            }}
        }

        val conFunGenerator = getConFunGenerator()

// */
///*
///*
        var isCoeficientComputed= false
        elements.forEachIndexed { i, e1 ->
            if(!isOperated[i]){
                val conFun: (e2: Calculable) -> Boolean = conFunGenerator(e1)
                val e1= if(e1 is Block) e1.simply() else e1
//                uniqueEls += e1
//                elCounts += e1.operateWithSimilar(i, conFun)

                prine("simply() isOperated.size= ${isOperated.size} elements.size= ${elements.size}")
                try {
                    prine("simply() e1= $e1 i= $i operations= $operations this= $this level= $operationLevel")
                } catch (e: Exception) {
                    prine("simply() e1= <error> i= $i operations= <error> this= <error> level= $operationLevel")
                }

                if(i > 0)
                    uniqueOps += operations[i-1]
                isOperated[i] = true


                if(!isCoeficientComputed
                    && operationLevel == Operation.TIMES.level && e1 !is Block
                ){
                    var coeficientNum=
                        if(i > 0 && operations[i-1] == Operation.DIVIDES) 1.0 / e1.numberComponent!!
                        else e1.numberComponent!!

                    //#1. Menghitung angka koefisien
                    elements.forEachIndexed(i+1) { u, e2 ->
                        if(e2 is Constant<*> || e2 is Variable<*>) {
                            when(operations[u-1]){
                                Operation.TIMES -> coeficientNum *= e2.numberComponent!!
                                Operation.DIVIDES -> coeficientNum /= e2.numberComponent!!
                            }
                        }
                    }
                    prine("simply() coeficient computation res= $coeficientNum this= $this")
                    uniqueEls += constantOf(coeficientNum)
                    isCoeficientComputed= true
                }

                if(e1 is Constant<*> || e1 is Variable<*>){ // Ini untuk `Constant` dan `Variable`
                    when(operationLevel){
                        Operation.PLUS.level -> {
                            var numSum= e1.numberComponent!!
                            elements.forEachIndexed(i+1) { u, e2 ->
                                if(!isOperated[u] && conFun(e2)) {
                                    when(operations[u-1]){
                                        Operation.PLUS -> numSum += e2.numberComponent!!
                                        Operation.MINUS -> numSum -= e2.numberComponent!!
                                    }
                                    isOperated[u]= true
                                }
                            }
                            if(numSum != 0) when(e1){
                                is Constant<*> -> uniqueEls += constantOf(numSum)
                                is Variable<*> -> uniqueEls += variableOf(e1.name, numSum)
                            }
//                            else { uniqueOps[i]= Operation.PLUS //.removeLastOrNull() }
                        }
                        Operation.TIMES.level -> {
                            if(e1 is Variable<*>){
                                //#2. Menghitung pangkat variabel
                                var powerNum= if(i > 0 && operations[i-1] == Operation.DIVIDES) -1
                                else 1
                                elements.forEachIndexed(i+1) { u, e2 ->
                                    if(!isOperated[u] && conFun(e2)) {
                                        when(operations[u-1]){
                                            Operation.TIMES -> powerNum++
                                            Operation.DIVIDES -> powerNum--
                                        }
                                        isOperated[u]= true
                                    }
                                }
                                prine("simply() TIMES Var powerNum= $powerNum ")
                                if(powerNum != 0){
                                    uniqueEls += variableOf(e1.name, 1).let {
                                        if(powerNum == 1) it
                                        else blockOf(it)
                                            .addOperation(constantOf(powerNum), Operation.POWER)
                                    }
                                    elHasOneDegreeUp += uniqueEls.lastIndex
                                }
                                else { uniqueOps.removeLastOrNull() }
                            }
                        }
                        Operation.MODULO.level -> {
                            prinw("Untuk bagian modulo, operasi penyederhanaan msh dalam tahap penelitian")
                            uniqueEls += e1
                        }
                        Operation.POWER.level -> {
                            prinw("Untuk bagian pangkat, operasi penyederhanaan msh dalam tahap penelitian")
                            uniqueEls += e1
                        }
                    }
                } else { //Untuk Block
                    when(operationLevel){
                        Operation.PLUS.level -> {
                            var elCount= 1
                            elements.forEachIndexed(i+1) { u, e2 ->
                                if(!isOperated[u] && conFun(e2)) {
                                    when(operations[u-1]){
                                        Operation.PLUS -> elCount++
                                        Operation.MINUS -> elCount--
                                    }
                                    isOperated[u]= true
                                }
                            }
                            if(elCount != 0)
                                uniqueEls += e1.let {
                                    if(elCount == 1) it
                                    else blockOf(e1)
                                        .addOperation(constantOf(elCount), Operation.TIMES)
                                }
                            else { uniqueOps.removeLastOrNull() }
                        }
                        Operation.TIMES.level -> {
                            var powerNum= if(i > 0 && operations[i-1] == Operation.DIVIDES) -1
                            else 1
                            elements.forEachIndexed(i+1) { u, e2 ->
                                if(!isOperated[u] && conFun(e2)) {
                                    prine("when(operations[u-1]) u-1= ${u-1} u= $u i= $i elements[u]= ${elements[u]} elements= $elements")

                                    when(operations[u-1]){
                                        Operation.TIMES -> powerNum++
                                        Operation.DIVIDES -> powerNum--
                                    }
                                    isOperated[u]= true
                                }
                            }
                            if(powerNum != 0){
                                uniqueEls += e1.let {
                                    if(powerNum == 1) it
                                    else blockOf(it)
                                        .addOperation(constantOf(powerNum), Operation.POWER)
                                }
                                elHasOneDegreeUp += uniqueEls.lastIndex
                            }
                            else { uniqueOps.removeLastOrNull() }
                        }
                        else -> {
                            prinw("Untuk bagian modulo dan pangkat, operasi penyederhanaan `Block` msh dalam tahap penelitian")
                            uniqueEls += e1
                        }
                    }
                }
            }
        }
/*
        val newBlock= BlockImpl(uniqueEls.removeFirst())
        uniqueEls.forEachIndexed { i, e ->
            newBlock.addOperation(e, uniqueOps[i-1])
        }
*/
        elements= uniqueEls
        operations= uniqueOps
        operationLevel= if(operations.isNotEmpty()) operations.first().level else -1

        for(eUp in elHasOneDegreeUp){
            uniqueOps[eUp-1]= Operation.TIMES
        }

        prine("SBLUM AKHIR ==== elements= $elements operations= $operations operationLevel= $operationLevel")

        if(uniqueEls.size == 1)
            return uniqueEls.first()
        if(uniqueEls.size == 2
            && (operations.isEmpty() || operations.first() == Operation.TIMES)
        ){
            var constCount= 0
            var varCount= 0
            var vars: Variable<*>?= null
            var const: Constant<*>?= null
            for(e in uniqueEls){
                if(e is Constant<*>){
                    constCount++
                    const= e
                } else if(e is Variable<*>) {
                    varCount++
                    vars= e
                }
            }
            if(constCount == 1 && varCount == 1)
                return variableOf(vars!!.name, vars.coeficient * const!!.number)
        }
/*
        when(operationLevel){
            2 -> for(eUp in elHasOneDegreeUp){
                uniqueOps[eUp-1]= Operation.TIMES
            }
        }
*/
        prine("AKHIR ==== elements= $elements operations= $operations")
// */

        return this
    }

    ///*
    //
    //1+2+3-4
    //1+2+(3*5)-4
    //1+2+(3*5/7)-4
    //1+2+(3*5)-(10/7)-4
    override fun setOperationAt(index: Int, operation: Operation, prioritizePrecedence: Boolean): Operation {
        return when (operation.level) {
            operationLevel -> (operations as MutableList).set(index, operation)
            else -> {
                val elIndex= index+1
                (elements as MutableList).removeAt(elIndex)
                val oldOp= (operations as MutableList).removeAt(index)
                if(elements.size <= 1)
                    operationLevel= -1
                addOperation(elements[elIndex], operation, prioritizePrecedence = prioritizePrecedence)
                oldOp
            }
        }
    }
// */

    override fun removeOperationAt(elementIndex: Int): Pair<Calculable, Operation?> {
        if(elements.first() == NullCalculable)
            throw IllegalStateExc(currentState = "Isi dari `elements` pada `this`.`Block`: '$this' merupakan `${NullCalculable}` sehingga tidak dapat dihapus lagi.")

        val initSize= elements.size
        val el= (elements as MutableList).removeAt(elementIndex)
        var op: Operation?= null

        if(initSize > 1) {
            if(elementIndex == 0 && operations[0] == Operation.MINUS)
                setFirstElementSignum(false)
            op= (operations as MutableList).removeAt(if(elementIndex > 0) elementIndex-1 else elementIndex)
        } else {
            (elements as MutableList).add(NullCalculable)
        }

        if(elements.size <= 1)
            operationLevel= -1
        return el to op
    }
    override fun removeOperation(element: Calculable, operation: Operation?): Boolean {
        val elInd= elements.indexOfWhere { it == element }.also {
            if(it.isNegative()) return false
        }
        if(elInd == 0) {
            if(elements.size == 1){
                setElementAt(0, NullCalculable) //setFirstElement
                return true
            }
            if(operation == null){
                (elements as MutableList).removeAt(0)
                if(operations[0] == Operation.MINUS){
                    setFirstElementSignum(false)
                    (operations as MutableList).removeAt(0)
                }
                return true
            }
        }

        val elItr= elements.iterator()
        val opItr= operations.iterator()

        elItr.next()

        var i = 1
        while(elItr.hasNext()){
            val el= elItr.next()
            val op= opItr.next()

            if(el == element && op == operation){
                (elements as MutableList).removeAt(i)
                (operations as MutableList).removeAt(i-1)
                return true
            }
            i++
        }

        if(elements.size <= 1)
            operationLevel= -1
        return false
    }

    //
    //1+2+3-4
    //1+2+(3*5)-4
    //1+2+(3*5/7)-4
    //1+2+(3*5)-(10/7)-4
    //((1+2+(3*5))^2)-(10/7)-4

    //1+2+3-4
    //(5*1)+2+3-4

    //1*3*2/4
    //(10+1)*3*2/4

    //1*3*2/4
    //(10^1)*3*2/4

    //1*3*2/4
    //10-(1*3*2/4) -> precedence
    //10-11+(1*3*2/4)) -> precedence
    override fun addOperation(
        element: Calculable, operation: Operation,
        elementIndex: Int, prioritizePrecedence: Boolean
    ): Block {
        if(elements.first() == NullCalculable){
            prinw("Block.addOperation() elemen awal masih `NullCalculable`, `operation` diabaikan.")
            setElementAt(0, element) //setFirstElement
            return this
        }
        if(operationLevel == -1)
            operationLevel= operation.level

        if(prioritizePrecedence){
            when {
                operation.level == operationLevel -> {
                    (elements as MutableList).add(elementIndex, element)
                    (operations as MutableList).add(
                        if(elementIndex > 1) elementIndex -1
                        else 0, operation
                    )
                }
                // #1 - Ok
                //1+2+3-4
                //1+2+3-(4*5)
                //(5*1)+2+3-4
                //1+(2*5)+3-4
                operation.level > operationLevel -> { //Pembuatan `Block` baru karena operator baru dg precedence lebih tinggi.
                    var movedElementInd= if(elementIndex > 0) elementIndex-1 else 0
                    var addedElement= element
                    var firstElement= elements[movedElementInd]

                    if(elementIndex == 0){
                        movedElementInd= 0
                        addedElement= firstElement
                        firstElement= element
                    }

                    val newBlock= BlockImpl(firstElement, /*operation.level,*/ this)
                    newBlock.addOperation(addedElement, operation)
                    (elements as MutableList)[movedElementInd]= newBlock
                }
                else -> { //Pemecahan `this` `Block` karena operator baru dg precedence lebih rendah.
                    // #2 - Ok
                    //1*2*3/4
                    //0-(1*2*3/4)-5
                    //(1*2*3/4)-5
                    //(1*2*3)-(5/4)
                    //(1*2)-(5*3/4)
                    if(elementIndex > 1){
                        val elSize= elements.size
                        var tailElements: List<Calculable>? = elements.copy(elementIndex) //.subList(elementIndex, elSize)
                        var tailOps: List<Operation>? = operations.copy(elementIndex-1) //.subList(elementIndex-1, operations.size)

                        if(elementIndex >= elSize){
                            tailElements= null
                            tailOps= null
                        }

                        val tailBlock= BlockImpl(element, /*operationLevel,*/ parentBlock)
/*
                        tailElements?.also {
                            val opIndex= elementIndex-1
                            for(i in 0 until it.size){
                                tailBlock.addOperation(it[i], tailOps!![i])
                                (elements as MutableList).removeAt(elementIndex) //.removeLast()
                                (operations as MutableList).removeAt(opIndex) //.removeLast()
                            }
                        }
 */
                        prine("AddOp pre removeLast() elements= $elements")
                        tailElements?.forEachIndexed { index, calculable ->
                            tailBlock.addOperation(calculable, tailOps!![index])
                            (elements as MutableList).removeLast()
                            (operations as MutableList).removeLast()
                        }

                        return if(parentBlock != null){
                            val indexOfThisBlock= parentBlock!!.elements.indexOf(this)
                            parentBlock!!.addOperation(tailBlock, operation, indexOfThisBlock +1)
                            parentBlock!!
                        } else { //Untuk kasus di mana pemecahan terjadi pada `rootBlock`
                            val rootBlock= blockOf(this)
                            rootBlock.addOperation(tailBlock, operation)
                            rootBlock.reconfigureChildParent()
                            rootBlock
                        }
                    } else {
                        // #3 - Ok
                        //1*2*3/4
                        //5-(1*2*3/4)
                        //1-(5*2*3/4)
                        //1-|17+(5*2*3/4)|
                        //1-|5+(17*2*3/4)|
                        val tailBlock= this

                        return if(parentBlock != null){
                            val indexOfThisBlock= parentBlock!!.elements.indexOf(tailBlock)
                            val addedElement= if(elementIndex > 0) elements.first() else element

                            parentBlock!!.addOperation(addedElement, parentBlock!!.operations.first(), indexOfThisBlock)
                            if(elementIndex > 0)
                                setElementAt(0, element) //setFirstElement(element)
                            parentBlock!!.setOperationAt(indexOfThisBlock, operation)
                            parentBlock!!
                        } else { //Untuk kasus di mana pemecahan terjadi pada `rootBlock`
                            val rootBlock= blockOf(if(elementIndex > 0) elements.first() else element)
                            rootBlock.addOperation(tailBlock, operation)
                            if(elementIndex == 1)
                                tailBlock.setElementAt(0, element) //setFirstElement
                            rootBlock.reconfigureChildParent()
                            rootBlock
                        }
                    }
                }
            }
            return this
        } else {
            if(operation.level == operationLevel) {
                (elements as MutableList).add(elementIndex, element)
                (operations as MutableList).add(
                    if(elementIndex > 1) elementIndex -1
                    else 0
                    , operation
                )
            } else {
                // #4 - Ok
                //1*3*2/4
                //((1*3)-10)*2/4
                val headElements= if(elementIndex > 0) elements.copy(end = elementIndex).toMutableList() //.subList(0, elementIndex).toMutableList()
                    else mutableListOf(elements[0])
                val headOps= if(elementIndex > 1) operations.copy(end = elementIndex-1).toMutableList() //.subList(0, elementIndex-1).toMutableList()
                    else null

                val innerHeadBlock= blockOf(
                    if(elementIndex > 0) headElements.removeFirst()
                    else element,
                    //headOps?.firstOrNull()?.level ?: operation.level
                ) //BlockImpl(element, operationLevel, parentBlock)
                headElements.forEachIndexed { index, calculable ->
                    innerHeadBlock.addOperation(calculable, headOps?.get(index) ?: operation)
                    if(elementIndex > 0)
                        (elements as MutableList).removeFirst()
                    if(headOps != null)
                        (operations as MutableList).removeFirst()
                }
                if(elementIndex == 1)
                    innerHeadBlock.addOperation(element, operation)

                val outerHeadBlock= if(headOps != null) blockOf(innerHeadBlock/*, operation.level*/).apply {
                    addOperation(element, operation)
                } else innerHeadBlock
                //blockOf(element, operation.level, this) //BlockImpl(element, operation.level, this)

                setElementAt(0, outerHeadBlock) //setFirstElement(outerHeadBlock)
                reconfigureChildParent()
            }
            return this
        }
    }

    override fun copy(): Block = BlockImpl().also {
        it.elements= elements.copy()
        it.operations= operations.copy()
        it.operationLevel= operationLevel
    }

    override fun toString(): String {
//        var bracketChecpointIndex= 0 //Untuk menandai index start dari "("
        var res= ""
        val elItr= elements.iterator()
        val opItr= operations.iterator()

        var e= elItr.next()
        var op: Operation

        res += if(e !is Block) e.toString() else "($e)"
        var i= 0
        while(elItr.hasNext()){
            e= elItr.next()
            op= opItr.next()

            val eStr= if(e !is Block) e.toString().let {
                if(it.startsWith('-')) "($it)" else it
            } else "($e)"

            if(op.level > operationLevel && i > 0){
                res = "($res"
                res += ") $op $eStr"
            } else {
                res += " $op $eStr"
            }
            i++
        }
        res += ""
        return if(res != NullCalculable.toString()) res else "Block of $res"
    }

    /**
     * Anggapannya jika 1 `Block` terdiri dari operasi dg level yg sama,
     */
    override fun hashCode(): Int {
        if(elements.size <= 1)
            return getHashCode(elements)

        //1. Jumlahkan semua hashCode dari [elements] dan [operations] dan urutan tidak berpengaruh.
        var hash= getHashCode(elements.subList(1, elements.size), operations, calculateOrder = false)

//        prine("===== hashCode() awal ===== hash= $hash owner= $this")

        //2. Untuk kasus tanda unary minus di depan:
        // a. Tambahkan hashCode tambahan untuk operator di depan (jika element pertama bernilai positif, maka PLUS. Jika negatif, maka MINUS)
        // b. Ubah nilai angka elemen pertama menjadi mutlak karena tanda MINUS udah ditambahkan menjadi operator.
        val first= elements.first()
        if(/*elements.size > 1 && */ operationLevel == 1){
            hash += when(first){
                is Variable<*> -> {
                    first.coeficient + first.name.hashCode()
//                        .also { prine("===== hashCode() first.name.hashCode() ===== hash 1= $it owner= $this") }
                }
                is Constant<*> -> first.number
                else -> return hash + first.hashCode() // Karena ini `Block`, jadinya langsung return computed hashCode.
            }.let {
                if(it.isNotNegative()) {
                    it + Operation.PLUS.hashCode()
//                        .also { prine("===== hashCode() Operation.PLUS.hashCode() ===== hash 1= $it owner= $this") }
                } else {
                    (it * -1).toInt() + Operation.MINUS.hashCode()
//                        .also { prine("===== hashCode() Operation.MINUS.hashCode() ===== hash 1= $it owner= $this") }
                }
            }.toInt()
//                .also { prine("===== hashCode() number ===== hash 1= $it owner= $this") }
        }
//        prine("===== hashCode() akhir ===== hash= $hash owner= $this")

        return hash
    }

    override fun equals(other: Any?): Boolean = when(other){
        is Calculable -> hashCode() == other.hashCode()
        else -> super.equals(other)
    }
}