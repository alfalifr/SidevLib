package sidev.lib

import sidev.lib.`val`.NumberOperationMode
import sidev.lib.`val`.RoundingMode
import sidev.lib.collection.*
import sidev.lib.collection.common.*
import sidev.lib.collection.lazy_list.CachedSequence
import sidev.lib.collection.lazy_list.LazyHashMap
import sidev.lib.collection.lazy_list.rangeTo
import sidev.lib.console.prin
import sidev.lib.console.prine
import sidev.lib.date.Date
import sidev.lib.number.*
import sidev.lib.progression.*
import sidev.lib.reflex.getHashCode
import sidev.lib.util.Locale
import kotlin.math.exp
import kotlin.math.log
import kotlin.ranges.until
import kotlin.test.Test
import kotlin.test.assertTrue

class StdLibCommonSampleTests {

    @Test
    fun nestedSeqTest(){
/*
        prin("========= Halo bro ===========")
        skippingNestedSequenceSimple<KClass<*>>(ArrayList::class){
            it.supertypes.asSequence().filter { it.classifier is KClass<*> }.map { it.classifier }.iterator() as Iterator<KClass<*>>
        }.also {
            prin("hasNext()= ${it.iterator().hasNext()}")
        }.forEachIndexed { index, kClass ->
            prin("i= $index cls= $kClass")
        }

        var i= 0
        prin("========= Halo bro - 2 ===========")
        nestedIteratorSimple<KClass<*>>(ArrayList::class){
            it.supertypes.asSequence().filter {
//                prin("nestedIteratorSimple it= $it it is KClass<*>= ${it is KClass<*>} it::class= ${it::class}")
                it.classifier is KClass<*>
            }.map{ it.classifier }.iterator() as Iterator<KClass<*>>
        }.also {
            prin("hasNext()= ${it.iterator().hasNext()}")
        }.forEach { kClass ->
            prin("i= ${i++} cls= $kClass")
        }

        i= 0
        prin("========= Halo bro - 3 ===========")
        val itr: LeveledNestedIterator_2<*, KClass<*>> = leveledNestedIteratorSimple<KClass<*>>(ArrayList::class){
            it.supertypes.asSequence().filter {
//                prin("nestedIteratorSimple it= $it it is KClass<*>= ${it is KClass<*>} it::class= ${it::class}")
                it.classifier is KClass<*>
            }.map{ it.classifier }.iterator() as Iterator<KClass<*>>
        }.also {
            prin("hasNext()= ${it.iterator().hasNext()}")
        }
        itr.forEach { kClass ->
            prin("i= ${i++} level= ${itr.currentLevel} cls= $kClass")
        }

        i= 0
        prin("========= Halo bro - 4 ===========")
        val itr2= leveledNestedIterator<KType, String>(ArrayList::class.supertypes.iterator(), {
            if(i < 2) it::class.supertypes.iterator() else null
        }){
            iteratorSimple(it.toString())
        }//.withLevel()
        itr2.forEach { kClass ->
            prin("i= ${i++} level= ${itr2.currentLevel} cls= $kClass")
        }

        prin("========= ArrayList::class.supertypes ===========")
        ArrayList::class.supertypes.forEachIndexed{ i, e ->
            prin("i= $i cls= $e")
        }
// */
    }

    @Test
    fun testMe() {
        assertTrue(Sample().checkMe() > 0)
    }


/*
    @Test
    fun stdReflexTest(){
        prin("AC::class.si= ${AC::class.si}")
        for((i, member) in AC::class.si.members.withIndex()){
            prin("i= $i member= $member")
        }
    }
 */


    @Test
    fun mathTest(){
        val int= 2.3 as Number
        val int2= (2 as Number / int)
        prin("int= $int2")

        prin(ipow(2, 3))
        prin(ipow(2, -2))
        prin("2.pow(-2)= ${2 powCast -2}")
        prin("125 root 3= ${125 rootCast 3}")
        prin("81 root 4= ${81 rootCast 4}")
        prin("81 root 2= ${81 rootCast 2}")
        prin("81.sqrt()= ${81.sqrtCast()}")
        prin("exp(2.0)= ${exp(2.0)}")
        prin(ipow(3, 4))
        prin(ipow(5, 5))
        log(2f, 3f)
        prin("10 log 1000= ${10 logCast 1000}")
        prin("3 log 81= ${3 logCast 81}")

        prin("223.1352= ${223.1352}")
        prin("223.1352.round()= ${223.1352.round()}")
        prin("-223.1352.round()= ${(-223.1352).round()}")
        prin("223.1352.round(-1)= ${223.1352.round(-1)}")
        prin("-223.1352.round(-1)= ${(-223.1352).round(-1)}")
        prin("223.1352.round(-2)= ${223.1352.round(-2)}")

        prin("-223.1352.round(-2, RoundingMode.UP)= ${(-223.1352).round(-2, RoundingMode.UP)}")
        prin("223.1352.round(-2, RoundingMode.UP)= ${(223.1352).round(-2, RoundingMode.UP)}")
        prin("-223.1352.round(-2, RoundingMode.CEIL)= ${(-223.1352).round(-2, RoundingMode.CEIL)}")
        prin("223.1352.round(-2, RoundingMode.CEIL)= ${(223.1352).round(-2, RoundingMode.CEIL)}")
        prin("-223.1352.round(-2, RoundingMode.DOWN)= ${(-223.1352).round(-2, RoundingMode.DOWN)}")
        prin("223.1352.round(-2, RoundingMode.DOWN)= ${(223.1352).round(-2, RoundingMode.DOWN)}")
        prin("-223.1352.round(-2, RoundingMode.FLOOR)= ${(-223.1352).round(-2, RoundingMode.FLOOR)}")
        prin("223.1352.round(-2, RoundingMode.FLOOR)= ${(223.1352).round(-2, RoundingMode.FLOOR)}")
        prin("223.1352.round(-2, RoundingMode.HALF_UP)= ${(223.1352).round(-2, RoundingMode.HALF_UP)}")
        prin("223.1352.round(-2, RoundingMode.HALF_DOWN)= ${(223.1352).round(-2, RoundingMode.HALF_DOWN)}")
        prin("223.1352[-2]= ${223.1352[-2]}")
    }


    fun ipow(base: Int, exp: Int): Int {
        prine("base= $base exp= $exp")
        if (exp == 0) return 1
        val temp = ipow(base, (exp / 2))
        return if (exp % 2 == 0) temp * temp else base * temp * temp
    }

    @Test
    fun commonListTest(){
        val arrayWrap= arrayWrapperOf(1,3,5,6.778,"hkj")
        val array by arrayWrap

        prin("\n================ array.forEach ================\n")
        array.forEach {
            prin("array foreeach it= $it")
        }

        val commonList = commonMutableListOf<String, Int>()
        commonList.put("haha", 1)
//        commonList["haha" to 1]
        commonList["hihe"] = 2
//        commonList.set("hoha", 3)
        commonList["hoha"]

        prin("\n================ commonList.forEachCommonEntry ================\n")
        commonList.forEachCommonEntry { prin("commonList entry= $it") }

        val commonIndexedList = commonIndexedMutableListOf<String>()
        commonIndexedList += "as"
        commonIndexedList += "asa"
        commonIndexedList += "asap"
        commonIndexedList[1] = "asf"
//        commonIndexedList[4] = "asafr"

        prin("\n================ commonIndexedList.forEachCommonEntry ================\n")
        commonIndexedList.forEachCommonEntry { prin("commonIndexedList entry= $it") }
    }

    @Test
    fun lazyListTest(){
        println("\n============= BATAS CachedSequence ==============\n")
        val strSeq= sequenceOf("Aku", "Mau", "Makan")
        val strSeq2= sequenceOf("Kamu" , "Dan", "Dia")
        val lazySeq= CachedSequence<String>()
        lazySeq += "Halo"
        lazySeq += "Bro"

        prin("lazySeq.size= ${lazySeq.size}")
        //lazySeq.addValues(strSeq)
        lazySeq .. strSeq .. strSeq2
///*
        val containsAku= "Aku" in lazySeq //.contains("Aku")
        val containsKamu= "Kamu" in lazySeq //.contains("Kamu")
        val containsDiaJelek= "Dia Jelek" in lazySeq //.contains("Kamu")
        val indexMau= lazySeq.indexOf("Mau")
        val indexKamu= lazySeq.indexOf("Kamu")
        val ke4= lazySeq[4]
        prin("lazySeq.size= ${lazySeq.size}")
        prin("indexMau= $indexMau ke4= $ke4 containsAku= $containsAku containsKamu= $containsKamu containsDiaJelek= $containsDiaJelek indexKamu= $indexKamu")
// * /

        println("\n============= BATAS CachedSequence.iterator() ==============\n")
        for((i, data) in lazySeq.withIndex()){
            prin("i= $i data= $data")
        }

        println("\n============= BATAS LazyHashMap ==============\n")

        val pairSeq= sequenceOf("Aku" to 3, "Dia" to 10, "Kamu" to 1)
        val pairSeq2= sequenceOf("Makan" to 2, "Belajar" to 5, "Tidur" to 0)
        val lazyMap= LazyHashMap<String, Int>()
        lazyMap["Mau"]= 7
        lazyMap["Iya"]= 6
        lazyMap .. pairSeq .. pairSeq2

        prin("lazyMap= $lazyMap")
//        lazyMap.addIterator(pairSeq.iterator())

        println("\n============= BATAS LazyHashMap.iterator() ==============\n")
        for((i, data) in lazyMap.withIndex()){
            prin("i= $i data= $data")
        }

        val valueMau= lazyMap["Mau"]
        val containsKamuMap= lazyMap.containsKey("Kamu")
        val valueIya= lazyMap["Iya"]
        val valueKamu= lazyMap["Kamu"]
        val valueTidur= lazyMap["Tidur"]
        val contains5= lazyMap.containsValue(5)
        val contains0= lazyMap.containsValue(0)
        val contains4= lazyMap.containsValue(4)

        prin("valueMau= $valueMau valueIya= $valueIya valueKamu= $valueKamu contains5= $contains5 contains0= $contains0 contains4= $contains4 valueTidur= $valueTidur containsKamuMap= $containsKamuMap")
    }

    @Test
    fun dateTest(){
        val date= Date()
        val locale= Locale("id-ID") //"ar-SA"
        prin("Date().time= ${date.time}")
        prin("Date().toFormatString()= ${date.toFormatString(locale = locale)}")
///*
        prin("locale.country= ${locale.country}")
        prin("locale.language= ${locale.language}")
        prin("locale.displayName= ${locale.displayName}")
        prin("locale.script= ${locale.script}")
        prin("locale.qualifiedId= ${locale.qualifiedId}")
// */
    }

    @Test
    fun queueTest(){
        val stack= stackOf(1,2,3,4,5)
        val stack2= stackOf(6,7,8,9)
        stack.forEach { prin(it) }
        stack2.forEach { stack.push(it) }
        stack.forEach { prine(it) }
        for(i in 0 until stack.size){
            prin(stack[i])
        }

        var i= 0
        var itr= stack.listIterator()
        while(itr.hasNext()){
            if(i == 3)
                prine("prev= ${itr.previous()}")
            prine(itr.next())
            i++
        }

        i= 0
        itr= mutableListOf(1,2,3,4,5).listIterator()
        while(itr.hasNext()){
            if(i == 3)
                prin("prev= ${itr.previous()}")
            prin(itr.next())
            i++
        }

        val list= listOf(1,2,3,4,5)
        prine("list.peek()= ${list.peek()}")
        prine("list.peek()= ${list.peek()}")

        val mutList= mutableListOf(1,2,3,4,5)
        prine("mutList.pop()= ${mutList.pop()}")
        prine("mutList.pop()= ${mutList.pop()}")
        prine("mutList.pop()= ${mutList.pop()}")
        mutList.push(6)
        prine("mutList.pop()= ${mutList.pop()}")
        prine("mutList.peek()= ${mutList.peek()}")
        prine("mutList.peek()= ${mutList.peek()}")

        prin(listOf(1,2,3,4,5).subList(1,3))

        val array= arrayOf(1,2,3,4,5)
        val arr2= array[1..3]
        val mutList2= mutList[1 .. 1]


        prin("mutList= $mutList mutList2= $mutList2")
        prin("array= ${array.joinToString()} arr2= ${arr2.joinToString()}")
    }

    @Test
    fun progressionTest(){
        prin("halo")

        1.progressTo(2).forEach {
            prin(it)
        }

        (2.3f percentileTo 5.0f).forEach {
            prine(it)
        }

        (1 quartileTo 3).forEach {
            prin(it)
        }

        (1.3 centileTo 3).forEach {
            prine(it)
        }

        (1.3 quartileTo 3).forEach {
            prin(it)
        }

        (4 downTo 3).forEach {
            prine(it)
        }

        (1.3 downUntil -3).forEach {
            prin(it)
        }
//        1f as Int
        (1 .. 10)[1..3].forEach {
            prine(it)
        }

        (0.04.progressTo(100, step = 10, operationMode = NumberOperationMode.MULTIPLICATIONAL)).forEach {
            prin(it)
        }

        (2.progressTo(300, step = 2, operationMode = NumberOperationMode.EXPONENTIAL)).forEach {
            prine(it)
        }

        (2.3.progressTo(100, step = -2, operationMode = NumberOperationMode.MULTIPLICATIONAL)).forEach {
            prin(it)
        }

        (2f.progressTo(100.5, step = 20.3)).forEach {
            prine(it)
        }
/*
        (16.0.progressTo(0.5, step = 0.5, operationMode = NumberOperationMode.EXPONENTIAL)).forEachIndexed { index, d ->
            if(index >= 20) return@forEachIndexed
            prine(d)
        }
 */

        var powRes= 16
        powRes= powRes powCast 0.5

        prin("powRes= $powRes")
        prin("16 pow 0.5= ${16 powCast 0.5}")

        prine((1 .. 10)[1])
        prin("size= ${(1 .. 10).size}")
        prin("range= ${(1 .. 10).range}")
    }

    @Test
    fun randomTest(){
        val list= ArrayList<List<*>>()
        for(i in 0 until 14)
            prin(listOf(1,2,"F",3).shuffled().also { list += it }) //.random()

        val uniqueList= mutableMapOf<List<*>, Int>()
        list.forEach {
            uniqueList[it]= (uniqueList[it]?.plus(1) ?: 1)
        }

        val len= list.size
        val percents= mutableListOf<Float>()
        uniqueList.forEach { (ls, count) ->
            val percent= count / len.toFloat()
            prin("list= $ls len= $len count= $count percent= $percent")
        }
    }

    @Test
    fun decimalNumberTest(){
        prin("123.123 * 1000= ${123.123 * 1000}")
        prin("123.123f.getDigitBehindDecimal() = ${123.123f.getDigitBehindDecimal()}")
    }

    @Test
    fun hashCodeTest(){
        class A
        val h1= getHashCode(listOf(1,2,3), calculateOrder = false)
        val h2= getHashCode(listOf(3,1,2), calculateOrder = false)

        prin("h1= $h1")
        prin("h2= $h2")

        val a= A()
        val b= A()
        val c= A()

        prin("a.hashCode()= ${a.hashCode()}")
        prin("b.hashCode()= ${b.hashCode()}")
        prin("c.hashCode()= ${c.hashCode()}")

        prin("a.hashCode()= ${a.hashCode()}")
        prin("b.hashCode()= ${b.hashCode()}")
        prin("c.hashCode()= ${c.hashCode()}")

        val h3= getHashCode(listOf(a,b,c).also { prin("list hashCode= ${it.hashCode()}") }, calculateOrder = false)
        val h4= getHashCode(listOf(b,c,a), calculateOrder = false)

        prin("h3= $h3")
        prin("h4= $h4")

        val x= 1624113833
        val y= 789160464
        val z= 436643214

        prin("x+y+z= ${x+y+z}")
        prin("z+y+x= ${z+y+x}")
    }
}