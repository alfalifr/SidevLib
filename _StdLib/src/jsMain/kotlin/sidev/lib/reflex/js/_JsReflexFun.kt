package sidev.lib.reflex.js

import sidev.lib.`val`.SuppressLiteral
import sidev.lib.reflex.core.SiReflexConst
import sidev.lib.reflex.js.kotlin.kotlinMetadata
import kotlin.js.Json
import kotlin.js.json

/*
fun getParam(func: dynamic): List<JsParameter>{
    if(!(func as Any?).isFunction)
        return emptyList()

    return getParamName(func).mapIndexed { index, s -> object : JsParameter{
        override val index: Int = index
        override val name: String? = s
    } }
}
 */
/** Mengambil property `name`. Jika tidak ada, mengambil tipe dari object `this`. */
fun jsName(any: dynamic): String =
    try{ (any as Any).kotlinMetadata.simpleName!! } //1. Coba dulu apakah dia kotlinFun
    catch (e: Throwable){
//        prine("jsName catch 1 any= ${str(any)}")
        try{ eval("any.name") as String } //2. Coba apakah punya property .name
        catch (e: Throwable){ any::class.js.name } //3. Paksa namanya dg cari di ::class.js
    }

fun jsNativeName(any: dynamic): String =
    try{ js("any.name") as? String ?: any::class.js.asDynamic().name }
    catch (e: Throwable){ SiReflexConst.TEMPLATE_NO_NAME }

/**
 * Fungsi yg sama dg `Object.defineProperty(owner, propName, attr)` pada Js.
 * @return -> `true` jika operasi berhasil,
 *   -> `false` jika gagal yg dapat disebabkan karena ada ketidaksesuaian parameter
 *   atau property dg nama [propName] udah ada dan gak bisa di-define lagi karena `configurable == false`.
 */
fun defineProperty(owner: dynamic, propName: String, vararg propAttribs: Pair<String, Any?>): Boolean{
    val attr= json(*propAttribs)
    return try{ js("Object.defineProperty(owner, propName, attr)"); true }
    catch (e: Throwable){ false }
}

fun getPropertyDescriptors(obj: Any): Json = (eval("Object.getOwnPropertyDescriptors(obj)") as Any).asJson()

/**
 * Fungsi untuk mengatur nilai property dg nama [propName] dg nilai [value] pada obj [owner].
 * Fungsi ini adalah fungsi aman untuk melakukan setProperty value karena mengecek pakah property mutable atau tidak
 * dg mengecek `writable` pada atribut property.
 * @return `true` jika [value] berhasil di-assign, `false` jika sebaliknya.
 */
fun setProperty(owner: Any, propName: String, value: Any?): Boolean{
//    prine("setProperty() propName= ${str(propName)} value= ${str(value)} type= ${jsTypeOf(value)}")
    val propDesc= getPropertyDescriptors(owner)
    if(propName in propDesc.keys){
        try {
            val isWritable= propDesc[propName].asDynamic().writable as Boolean
            if(!isWritable) return false
        } catch (e: Throwable){ /*abaikan*/ }
    }
    owner.asJson()[propName]= value
    return true
}

//TODO <26 Agustus 2020> => Ubah returntype jadi Any? agar lebih type-safe
val Any.prototype: Any
    get()= (if(isFunction) asDynamic().prototype
    else try{ asDynamic().__proto__!! }
    catch (e: Throwable){
        throw IllegalStateException("objek: \"${str(this)}\" tidak punya prototype.")
            //Walaupun Object.prototype == null, tetap tidak return Object.prototype agar sesuai konteks
            // bahwa instance dg kelas Object tidak punya superclass.
    }) //as Any dikomen agar tidak terjadi ClassCastException

/**
 * Mengambil immediate superclass, tidak disertai interface jika pada sudut pandang Kotlin.
 */
fun jsSuperclass(any: Any): Any? = try{ any.asDynamic().__proto__ } catch (e: Throwable){ null }

/**
 * Untuk mengambil constructor pada objek Js.
 * Param [any] dapat berupa fungsi maupun objek. Hal tersebut bertujuan untuk kepratktisan
 * saat fungsi yg membentuk suatu objek perlu diambil.
 */
fun jsConstructor(any: Any): dynamic {
    @Suppress(SuppressLiteral.NAME_SHADOWING)
    val any= try{ (jsPureFunction(any) as Any) } catch (e: Throwable){ any }
    val constr= any.prototype.asDynamic().constructor as? Any
    if(constr == null || constr.isUndefined)
        throw IllegalArgumentException("""Fungsi: "${str(any)}" tidak punya konstruktor.""")
    return constr
}