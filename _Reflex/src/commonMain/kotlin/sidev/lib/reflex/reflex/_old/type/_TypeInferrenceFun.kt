package sidev.lib.reflex.type
/*
import sidev.lib.check.isNull
import sidev.lib.check.notNull
import sidev.lib.collection.filterIndexed
import sidev.lib.collection.findIndexed
import sidev.lib.collection.lazy_list.asCached
import sidev.lib.collection.notEmpty
import sidev.lib.console.prine
import sidev.lib.console.prinr
import sidev.lib.reflex.inner.createType
import sidev.lib.structure.data.type.InferredType
import sidev.lib.type.Null
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection


/**
 * Menyimpulkan common-class dari element pada sebuah array.
 * Penyimpulan melibatkan semua anggota array dg batas maksimal 200.
 */
fun <T> inferElementType(array: Array<T>): KType = getCommonType(
    *(
            if(array.size <= 200) array
            else array.sliceArray(0 until 200)
    )
)

/**
 * Mengambil [InferredType] yg berisi [KType] yg disimpulkan dari properti yg ada di `this.extension` [Any].
 * Properti ini juga dapat menyimpulkan tipe dari null instance, namun tipe yg disimpulkan menjadi [Null?].
 */
val Any?.inferredType: InferredType
    get(){
        if(this == null)
            return Null.type.asInferredType()

        val typeParams= this::class.typeParameters

        val typeParamProjectionList= typeParams.map{ it.getClassProjectionIn(this) } as ArrayList
        typeParamProjectionList.filterIndexed { it.value == KTypeProjection.STAR }
            .notEmpty { indexedList -> //Jika terdapat star-projection, maka cari apakah projection ada terdapat pada type-param lainnya.
                val indexList= indexedList.map { it.index } as ArrayList //Untuk mempermudah pengecekan apakah indeks iterasi merupakan indeks untuk star-projection.

                val linkedTypeParamsSeq=
                    typeParams.asSequence() //.filterIndexed{ i, typeParam -> i !in indexList }
                        .map { it.getTypeParameterLink(this::class)!! }.asCached()
                //Digunakan untuk mencari hubungan type-param yg star-projected dg type-param lain.
                // Dalam konteks ini, nested pada type-param lain.

                var resolvedProjectionCount= 0
                //Pencairan pada type-param lain di dalam while dilatar-belakangi karena urutan nested tidak urut sesuai [KClass.typeParameters].
                while(resolvedProjectionCount in 0 until indexList.size){
                    resolvedProjectionCount= -1
                    val itrLimit= indexList.size
                    var itrProgress= 0

                    for(starProjectedIndex in typeParams.indices){
                        if(itrProgress >= itrLimit) break //Jika indeks iterasi udah melebihi jumlah star-projection, maka akhiri iterasi.
                        if(starProjectedIndex !in indexList) continue
                        itrProgress++

                        val starProjectedTypeParam= typeParams[starProjectedIndex] //Cari type-param yg star-projected.
                        //linkedTypeParamsSeq[starProjectedIndex].typeParam
                        linkedTypeParamsSeq.findIndexed {
                            it.value.upperBoundTypeParam.contains(starProjectedTypeParam) //Cari apakah type-param yg star-projected nested di type-param lain.
                                    && it.index !in indexList // Tentunya nested pada type-param yg bkn star-projected.
                        }.notNull {
                            val foundLinkedTypeParam= it.value
                            val typeParam= foundLinkedTypeParam.typeParam //Type-param lain tempat nested type-param yg star-projected.
                            val projectionsItr= typeParamProjectionList[it.index].nestedProjections.iterator()
                            //Iterator untuk projection pada type-param lain.

                            for((upperBoundIndexItr, upperBoundProjection) in typeParam.nestedUpperBoundTypeArguments.withIndex()){
                                if(upperBoundProjection.type?.classifier == starProjectedTypeParam){
                                    var itr= -1
                                    while(++itr < upperBoundIndexItr){ projectionsItr.next() } //while digunakan ketimbang CachedSequence agar tidak membebani heap.
                                    val realProjectionOfStarProjectedTypeParam= projectionsItr.next()
                                    typeParamProjectionList[starProjectedIndex]= realProjectionOfStarProjectedTypeParam
                                    resolvedProjectionCount++ //+= if(resolvedProjectionCount == -1) 2 else 1
                                    indexList -= starProjectedIndex
                                    prinr("""Berhasil menyimpulkan starProjectedTypeParam: "$starProjectedTypeParam" proyeksi: "$realProjectionOfStarProjectedTypeParam".""")
                                    break
                                }
                            }
                        }.isNull { prine("""Tidak bisa menyimpulkan starProjectedTypeParam: "$starProjectedTypeParam" karena tidak tersedia info proyeksi dari type param lain.""") }
                    }
                }
            }
        return this::class.createType(typeParamProjectionList).asInferredType()
    }

/** Mengambil [InferredType] yg berisi [KType] yg disimpulkan dari properti yg ada di `this.extension` [Any]. */
@Deprecated("Tidak dapat meng-infer nested type-projection", ReplaceWith("Any.inferredType"))
internal val Any.inferredType_old: InferredType
    get(){
        val typeParamProjectionList= this::class.typeParameters
            .map{ it.getClassProjectionIn(this) }
        return this::class.createType(typeParamProjectionList).asInferredType()
    }

/** Membungkus `this.extension` [KType] menjadi [InferredType]. */
fun KType.asInferredType(): InferredType = InferredType(this)


/**
 * Menentukan apakah [any] memiliki tipe yg kompatibel dg `this.extension` [KType].
 * Fungsi ini scr prinsip sama dg [isAssignableFrom].
 */
fun KType.isInstance(any: Any?): Boolean
        = any == null && isMarkedNullable || isAssignableFrom(any.inferredType) //any!!.clazz == classifier


 */