package sidev.lib.math.arithmetic

import sidev.lib.collection.copy
import sidev.lib.math.fpb
import sidev.lib.number.*

/**
 * Objek yg menyelesaikan operasi antara 2 [Calculable].
 */
object Solver {
    fun plus(n1: Calculable, n2: Calculable): Calculable = when(n1){
        is Constant<*> -> when(n2){
            is Constant<*> -> plus(n1, n2)
            is Variable<*> -> plus(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.PLUS)
        }
        is Variable<*> -> when(n2){
            is Constant<*> -> plus(n1, n2)
            is Variable<*> -> plus(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.PLUS)
        }
        is Block -> when(n2){
            is Block -> plus(n1, n2)
            else -> n1.addOperation(n2, Operation.PLUS)
        }
        else -> blockOf(n1).addOperation(n2, Operation.PLUS)
    }
    fun minus(n1: Calculable, n2: Calculable): Calculable = when(n1){
        is Constant<*> -> when(n2){
            is Constant<*> -> minus(n1, n2)
            is Variable<*> -> minus(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.MINUS)
        }
        is Variable<*> -> when(n2){
            is Constant<*> -> minus(n1, n2)
            is Variable<*> -> minus(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.MINUS)
        }
        is Block -> when(n2){
            is Block -> minus(n1, n2)
            else -> n1.addOperation(n2, Operation.MINUS)
        }
        else -> blockOf(n1).addOperation(n2, Operation.MINUS)
    }
    fun times(n1: Calculable, n2: Calculable): Calculable = when(n1){
        is Constant<*> -> when(n2){
            is Constant<*> -> times(n1, n2)
            is Variable<*> -> times(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.TIMES)
        }
        is Variable<*> -> when(n2){
            is Constant<*> -> times(n1, n2)
            is Variable<*> -> times(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.TIMES)
        }
        is Block -> when(n2){
            is Block -> times(n1, n2)
            else -> n1.addOperation(n2, Operation.TIMES)
        }
        else -> blockOf(n1).addOperation(n2, Operation.TIMES)
    }
    fun div(n1: Calculable, n2: Calculable): Calculable = when(n1){
        is Constant<*> -> when(n2){
            is Constant<*> -> div(n1, n2)
            is Variable<*> -> div(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.DIVIDES)
        }
        is Variable<*> -> when(n2){
            is Constant<*> -> div(n1, n2)
            is Variable<*> -> div(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.DIVIDES)
        }
        is Block -> when(n2){
            is Block -> div(n1, n2)
            else -> n1.addOperation(n2, Operation.DIVIDES)
        }
        else -> blockOf(n1).addOperation(n2, Operation.DIVIDES)
    }
    fun rem(n1: Calculable, n2: Calculable): Calculable = when(n1){
        is Constant<*> -> when(n2){
            is Constant<*> -> rem(n1, n2)
            is Variable<*> -> rem(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.MODULO)
        }
        is Variable<*> -> when(n2){
            is Constant<*> -> rem(n1, n2)
            is Variable<*> -> rem(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.MODULO)
        }
        is Block -> when(n2){
            is Block -> rem(n1, n2)
            else -> n1.addOperation(n2, Operation.MODULO)
        }
        else -> blockOf(n1).addOperation(n2, Operation.MODULO)
    }
    fun pow(n1: Calculable, n2: Calculable): Calculable = when(n1){
        is Constant<*> -> when(n2){
            is Constant<*> -> pow(n1, n2)
            is Variable<*> -> pow(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.POWER)
        }
        is Variable<*> -> when(n2){
            is Constant<*> -> pow(n1, n2)
            is Variable<*> -> pow(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.POWER)
        }
        is Block -> when(n2){
            is Block -> pow(n1, n2)
            else -> n1.addOperation(n2, Operation.POWER)
        }
        else -> blockOf(n1).addOperation(n2, Operation.POWER)
    }
    fun root(n1: Calculable, n2: Calculable): Calculable = when(n1){
        is Constant<*> -> when(n2){
            is Constant<*> -> root(n1, n2)
            is Variable<*> -> root(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.ROOT)
        }
        is Variable<*> -> when(n2){
            is Constant<*> -> root(n1, n2)
            is Variable<*> -> root(n1, n2)
            else -> blockOf(n1).addOperation(n2, Operation.ROOT)
        }
        is Block -> when(n2){
            is Block -> root(n1, n2)
            else -> n1.addOperation(n2, Operation.ROOT)
        }
        else -> blockOf(n1).addOperation(n2, Operation.ROOT)
    }


    fun plus(n1: Constant<*>, n2: Constant<*>): Constant<*> = constantOf(n1.number + n2.number)
    fun minus(n1: Constant<*>, n2: Constant<*>): Constant<*> = constantOf(n1.number - n2.number)
    fun times(n1: Constant<*>, n2: Constant<*>): Constant<*> = constantOf(n1.number * n2.number)
    fun div(n1: Constant<*>, n2: Constant<*>): Constant<*> = constantOf(n1.number / n2.number)
    fun rem(n1: Constant<*>, n2: Constant<*>): Constant<*> = constantOf(n1.number % n2.number)
    fun pow(n1: Constant<*>, n2: Constant<*>): Constant<*> = constantOf(n1.number pow n2.number)
    fun root(n1: Constant<*>, n2: Constant<*>): Constant<*> = constantOf(n1.number root n2.number)

    //#1
    //2 % 3x -> x=7
    //2 % 3.7 = 2 % 14 = 2
    //2 % 3.7 -> (2%3) . 7 = 2 . 7 = 14 (x)
    //2 % 3.7 -> (2%3) % 7 = 2 % 7 = 2 (v)

    //#1.2
    //3 % 5x -> x=2
    //3 % 5.2 = 3 % 10 = 3
    //3 % 5.2 -> (3%5) % 2 = 1 (x)

    fun plus(n1: Constant<*>, n2: Variable<*>): Variable<*> = variableOf(n2.name, n1.number + n2.coeficient)
    fun minus(n1: Constant<*>, n2: Variable<*>): Variable<*> = variableOf(n2.name, n1.number - n2.coeficient)
    fun times(n1: Constant<*>, n2: Variable<*>): Variable<*> = variableOf(n2.name, n1.number * n2.coeficient)
    fun div(n1: Constant<*>, n2: Variable<*>): Variable<*> = variableOf(n2.name, n1.number / n2.coeficient)
    fun rem(n1: Constant<*>, n2: Variable<*>): Block { //variableOf(n2.name, n1.number % n2.coeficient) //TODO
        val fpb= n1.number fpb n2.coeficient
        return blockOf(constantOf(fpb)).addOperation(
            blockOf(constantOf(n1.number / fpb))
                .addOperation(variableOf(n2.name, n2.coeficient / fpb), Operation.MODULO),
            Operation.TIMES
        )
    }
    fun pow(n1: Constant<*>, n2: Variable<*>): Block = blockOf(n1).addOperation(n2, Operation.POWER)
    fun root(n1: Constant<*>, n2: Variable<*>): Block = blockOf(n1).addOperation(n2, Operation.ROOT)



    //#1
    //2x % 3 -> x=7
    //2.7 % 3 = 14 % 3 = 2
    //2.7 % 3 -> 7 . (2%3) = 7.2 = 14

    //#2.1
    //3x % 6 -> x=7
    //3.7 % 6 = 21 % 6 = 3
    //3.7 % 6 -> 3 . (7%2) = 3.1 = 3 (v)
    //3.x % 6 -> 3 . (x%2) (v)

    //#2.2
    //8x % 12 -> x=7
    //8.7 % 12 = 56 % 12 = 8
    //8.7 % 12 -> 4 . (2.7%3) = 4.2 = 8 (v)
    //8.x % 12 -> 4 . (2x%3) (v)

    //#2.3
    //3x % 6 -> x=2
    //3.2 % 6 = 6 % 6 = 0
    //3.2 % 6 -> 3 . (2%2) = 3.0 = 0 (v)

    fun plus(n1: Variable<*>, n2: Constant<*>): Variable<*> = plus(n2, n1)
    fun minus(n1: Variable<*>, n2: Constant<*>): Variable<*> = variableOf(n1.name, n1.coeficient - n2.number)
    fun times(n1: Variable<*>, n2: Constant<*>): Variable<*> = times(n2, n1)
    fun div(n1: Variable<*>, n2: Constant<*>): Variable<*> = variableOf(n1.name, n1.coeficient / n2.number)
    fun rem(n1: Variable<*>, n2: Constant<*>): Block { //variableOf(n2.name, n1.number % n2.coeficient) //TODO
        val fpb= n1.coeficient fpb n2.number
        return blockOf(constantOf(fpb)).addOperation(
            blockOf(variableOf(n1.name, n1.coeficient / fpb))
                .addOperation(constantOf(n2.number / fpb), Operation.MODULO),
            Operation.TIMES
        )
    }
    fun pow(n1: Variable<*>, n2: Constant<*>): Block =
        blockOf(variableOf(n1.name, n1.coeficient pow n2.number)).addOperation(n2, Operation.POWER)
    fun root(n1: Variable<*>, n2: Constant<*>): Block =
        blockOf(variableOf(n1.name, n1.coeficient root n2.number)).addOperation(n2, Operation.ROOT)


    fun plus(n1: Variable<*>, n2: Variable<*>): Calculable =
        if(n1.name == n2.name) variableOf(n1.name, n1.coeficient + n2.coeficient)
        else blockOf(n1).addOperation(n2, Operation.PLUS)
    fun minus(n1: Variable<*>, n2: Variable<*>): Calculable =
        if(n1.name == n2.name) variableOf(n1.name, n1.coeficient - n2.coeficient)
        else blockOf(n1).addOperation(n2, Operation.MINUS)
    fun times(n1: Variable<*>, n2: Variable<*>): Block =
        blockOf(constantOf(n1.coeficient * n2.coeficient))
            .addOperation(variableOf(n1.name, 1), Operation.TIMES).run {
                if(n1.name == n2.name) addOperation(constantOf(2), Operation.POWER)
                else addOperation(variableOf(n2.name, 1), Operation.TIMES)
            }
    fun div(n1: Variable<*>, n2: Variable<*>): Calculable =
        constantOf(n1.coeficient / n2.coeficient).let {
            if(n1.name == n2.name) it
            else blockOf(it)
                .addOperation(variableOf(n1.name, 1), Operation.TIMES)
                .addOperation(variableOf(n2.name, 1), Operation.DIVIDES)
        }

    //#1 name sama
    //2x % 3x -> x=5
    //2.5 % 3.5 = 10 % 15 = 10
    //2.5 % 3.5 -> 2 % 3 = 2 (x)
    //2.5 % 3.5 -> 5 . (2 % 3) = 5 . 2 = 10 (v)
    //2.x % 3.x -> x . (2 % 3) = 5 . 2 = 10 (v)

    //#1 name sama #2
    //3x % 2x -> x=5
    //3.5 % 2.5 = 15 % 10 = 5
    //3.5 % 2.5 -> 3 % 2 = 1 (x)
    //3.5 % 2.5 -> 5 . (3 % 2) = 5 . 1 = 5 (v)
    //3.x % 2.x -> x . (3 % 2) = x . 1 = x (v)

    //#1 name sama #2
    //8x % 12x -> x=5
    //8.5 % 12.5 = 40 % 60 = 40
    //8.5 % 12.5 -> 5 . (8%12) = 5.4 . (2%3) = 20 . (2%3) = 20.2 = 40 (v)
    //8.x % 12.x -> 4x . (2%3)

    //#2 name berbeda #1
    //2x % 3y -> x=5 y=2
    //2.5 % 3.2 = 10 % 6 = 4
    //2.5 % 3.2 -> 2 . 5%2 = 2 . 1 = 2 (x)
    //2.5 % 3.2 -> 2 . (5%3) = 2 . 2 = 4 (v)

    //#3 name berbeda #2
    //2x % 3y -> x=5 y=7
    //2.5 % 3.7 = 10 % 21 = 10
    //2.5 % 3.7 -> (2%3) . (5%7) = 2 . 5 = 10 (v)

    //#4 name berbeda #3
    //2x % 3y -> x=5 y=4
    //2.5 % 3.4 = 10 % 12 = 10
    //2.5 % 3.4 -> (2%3) . (5%4) = 2 . 1 = 2 (x)
    //2.5 % 3.4 -> (2%3) . (4%5) = 2 . 4 = 8 (x)

    fun rem(n1: Variable<*>, n2: Variable<*>): Calculable =
        if(n1.name == n2.name) {
            if(n1.coeficient != n2.coeficient) variableOf(n1.name, n1.coeficient % n2.coeficient)
            else constantOf(0)
        } else {
            val fpb= n1.coeficient fpb n2.coeficient
            blockOf(constantOf(fpb)).addOperation(
                blockOf(variableOf(n1.name, n1.coeficient / fpb))
                    .addOperation(variableOf(n2.name, n2.coeficient / fpb), Operation.MODULO),
                Operation.TIMES
            )
        }

    // #1
    // 2x ^ 3x -> x=2
    // 2.2 ^ 3.2 = 4 ^ 6 = 4 ^ 2 . 4 ^ 2 . 4 ^ 2 = 16.16.16 = 4096
    // 2.2 ^ 3.2 = 4 ^ 6 = 4 ^ 2 . 4 ^ 2 . 4 ^ 2 = 16.16.16 = 4096
    fun pow(n1: Variable<*>, n2: Variable<*>): Block = blockOf(n1).addOperation(n2, Operation.POWER)
    fun root(n1: Variable<*>, n2: Variable<*>): Block = blockOf(n1).addOperation(n2, Operation.ROOT)


    fun plus(n1: Block, n2: Block): Block =
        if(n1.hashCode() == n2.hashCode()) blockOf(constantOf(2)).addOperation(n1, Operation.TIMES)
        else blockOf(n1).addOperation(n2, Operation.PLUS)
    fun minus(n1: Block, n2: Block): Calculable =
        if(n1.hashCode() == n2.hashCode()) constantOf(0)
        else blockOf(n1).addOperation(n2, Operation.MINUS)
    fun times(n1: Block, n2: Block): Block =
        if(n1.hashCode() == n2.hashCode()) blockOf(constantOf(2)).addOperation(n1, Operation.POWER)
        else blockOf(n1).addOperation(n2, Operation.TIMES)
    fun div(n1: Block, n2: Block): Calculable =
        if(n1.hashCode() == n2.hashCode()) constantOf(1)
        else blockOf(n1).addOperation(n2, Operation.DIVIDES)
    fun rem(n1: Block, n2: Block): Calculable =
        if(n1.hashCode() == n2.hashCode()) constantOf(0)
        else blockOf(n1).addOperation(n2, Operation.MODULO)
    fun pow(n1: Block, n2: Block): Block = n1.addOperation(n2, Operation.POWER)
    fun root(n1: Block, n2: Block): Block = n1.addOperation(n2, Operation.ROOT)



    //4(x^2) + 8x + 4
    //4(x.x) + 8x + 4

    /**
     * Memproses nested `Block` sehingga [block] memiliki jml nested `Block` seminimal mungkin.
     * Pemrosesan yang dimaksud adalah mengubah bentuk dari [Operation.level] yg lebih tinggi ke yg lebih rendah.
     */
///*
    @ExperimentalStdlibApi
    fun flatten(block: Block): Calculable {
        val uniqueEls= mutableListOf<Calculable>()
        val uniqueOps= mutableListOf<Operation>()

        when(block.operationLevel){
            Operation.POWER.level -> {

            }
        }
        return block
    }
// */
    @ExperimentalStdlibApi
    fun sortLevel1Block(block: Block): Block {
        if(block.operationLevel != 1)
            return block
        return block
    }


    //(2x+4) * (6y-2)
    // + .. - .. + .. - ..
    //(2x-4) * (6y+2)
    // + .. + .. - .. - ..
    //(2x-4) * (6y-2)
    // + .. - .. - .. + ..
    // + 12xy - 4x - 24y + 8
    // + 4x.(3x - 1) - 8.(3y + 1)
    // (4x-8) * (3x - 1)

    // + 12xy - 24y + 8 - 4x
    // + 12y(x - 2) + 4(2 - x)
    // + 12y(x - 2) + (-4(x - 2))
    // (12y-4) * (x-2)

    // (12x ^ 2) ^ 24y = 12x ^ 48y

    // + 12xy + 8 - 4x - 24y
    // 4(3xy + 2) - 4(x + 6y)

    //(2x-4) * (6y+2)

    //(12xy + 4x - 24y - 8) * (2z+4)
    //+ .. + .. + .. + .. - .. - .. - .. -

    //4(3xy + x - 6y - 2)
    //4y(3x + 4x/y - 24 - 8/y)
    //4x(3y + 4 - 24y/x - 8/x)
    //4x(y(3 + 4/y + 24/x - 8/xy))
    //y()

    //(2x*4) + (18xy/2x)
    //(2x+4) + (18xy-2x)

///*
    //#1
    //4x-2 = 2.(2x-1)
    //#2
    //4x-(6x^4)+2 = 2.(2x-)
    //4x-(6x^4)+2 = 4x-((6^4).(x^4))+2 = 4x-(1296.(x^4))+2 = 2.(2x-(648.(x^4))+1)
    //#3
    //(2x-4) + (18xy+2x) = 2(x-2) +  2x(9y+1) = 2((x-2) + x(9y+1)) =
    //(2x-4) * (18xy+2x)

    // + (12 * x * y) - (4 * x) - (24 * y) + 8
    // + 4((3 * x * y) - (1 * x) - (6 * y) + 2)

    // (2x + 3) * (x + 1)
    // 2(x^2) + 2x + 3x + 3
    @ExperimentalStdlibApi
    fun factorize(block: Block, simplifyFirst: Boolean= true): Calculable {
        val simplified= if(simplifyFirst) block.simply() else block
        if(simplified !is Block)
            return simplified

        val elementDupl= simplified.elements.copy().toMutableList()

        elementDupl.forEachIndexed { i, e ->
            if(e is Block) elementDupl[i]= factorize(e, false)
        }

        when(simplified.operationLevel){
            Operation.PLUS.level -> {
                val factorPerElement= elementDupl.map { it.numberComponent(false) }
                val nameFactorPerElement= elementDupl.map { it }
                val fpb= fpb(*elementDupl.mapNotNull { it.numberComponent(false) }.toTypedArray())

            }
            Operation.TIMES.level -> {
                return simplified // Karena `Block` dg level TIMES itu sendiri adalah faktornya.
            }
            Operation.POWER.level -> {
                return simplified // Karena `Block` dg level POWER tidak bisa difaktorkan.
            }
        }
        return simplified
    }

    @ExperimentalStdlibApi
    fun fpb(vararg els: Calculable): Calculable {
        if(els.all { it is SingleElement<*> })
            return fpb(*(els as Array<SingleElement<*>>))

        val blockElFactors= els.toMutableList()
        val singleElFactors= els.mapIndexedNotNull { i, e ->
            if(e is SingleElement<*>){
                blockElFactors.removeAt(i)
                e
            } else null
        }

        val singleElFpb= fpb(*singleElFactors.toTypedArray())
        return singleElFpb
    }

    fun fpb(vararg els: SingleElement<*>): SingleElement<*> {
        val numFactorPerElement= els.map { it.numberComponent }
        val nameFactorPerElement= els.map { if(it is Variable<*>) it.name else null }

        val numFpb= fpb(*numFactorPerElement.toTypedArray())
        val nameFpb= if(nameFactorPerElement.all { it != null }) nameFactorPerElement.first()
            else null

        return if(nameFpb != null) variableOf(nameFpb, numFpb)
        else constantOf(numFpb)
    }
// */
}