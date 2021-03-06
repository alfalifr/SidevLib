package sidev.lib.number

import sidev.lib.annotation.Note
import sidev.lib.annotation.Unsafe

/**
 * Mengambil angka pada digit [digitPlace]. Fungsi ini tidak mengambil angka di belakang koma.
 * [digitPlace] dihitung dari belakang, bkn dari depan. [digitPlace] dimulai dari 0.
 * Jika [digitPlace] negatif, brarti angka yg diambil berada di belakang koma.
 */
fun Number.getNumberAtDigit(digitPlace: Int): Int{
//    if(digitPlace.isNegative()) throw ParameterExc(paramName = "digitPlace", detMsg = "Tidak boleh negatif.")
    if(digitPlace.isNegative()){
        if(!this.isFloatingType()) return 0 //Jika ternyata angka yg diambil adalah di belakang koma,
        // sedangkan tipe data angka kelas ini tidak memiliki koma, maka return 0.
        val newThis= this * (10 powCast -digitPlace).toInt()
        return newThis.getNumberAtDigit(0)
    }
    val digitPlaceDividerFactor= (digitPlace).notNegativeOr(0)
    val digitPlaceModderFactor= (digitPlace+1).notNegativeOr(0)

    val digitPlaceDivider= (10 powCast digitPlaceDividerFactor).toInt()
    val digitPlaceModder= (10 powCast digitPlaceModderFactor).toInt()

    return ((this % digitPlaceModder) / digitPlaceDivider).toInt() //as T
}

operator fun Number.get(digitPlace: Int): Int = getNumberAtDigit(digitPlace)

/**
 * Mengambil angka desimal saja. Kemungkinan @return 0 jika `this.extension` adalah angka bulat.
 * Fungsi ini tidak menjamin angka desimal yg diambil bulat dan sesuai input.
 */
@Unsafe("Angka yg dihasilkan blum memiliki presisi yg tinggi.")
fun Number.getDecimal(): Number = this -(this.toLong())

//123.123
//@Experimental("Cara mudah dg menjadikannya string dan mencari titik (.). Namun, Hal tersebut bkn cara optimal.")
@Note("Tidak ada cara lebih akurat dan optimal dari toString().")
fun Float.getDigitBehindDecimal(): Int = toString().run {
    val decIndex= indexOf(".")
    (length - decIndex - 1).let {
        if(it == 1 && this[decIndex +1] == '0') 0
        else it
    }
}

//@Experimental("Cara mudah dg menjadikannya string dan mencari titik (.). Namun, Hal tersebut bkn cara optimal.")
@Note("Tidak ada cara lebih akurat dan optimal dari toString().")
fun Double.getDigitBehindDecimal(): Int = toString().run {
    val decIndex= indexOf(".")
    (length - decIndex - 1).let {
        if(it == 1 && this[decIndex +1] == '0') 0
        else it
    }
}

fun Number.getDigitBehindDecimal(): Int = when {
    !isFloatingType() -> 0
    else -> toString().run {
        val decIndex= indexOf(".")
        (length - decIndex - 1).let {
            if(it == 1 && this.length == 3 && this[decIndex +1] == '0') 0
            else it
        }
    }
}

/*
fun Float.getDigitBehindDecimal(): Int {
    var decimal= getDecimal()
    var digit= 0
    while(decimal > 0){
        digit++
        prine("decimal= $decimal digit= $digit")
        decimal= (decimal * 10).getDecimal()
    }
    return digit
}

fun Double.getDigitBehindDecimal(): Int {
    var decimal= getDecimal()
    var digit= 0
    while(decimal > 0){
        digit++
        prine("decimal= $decimal digit= $digit")
        decimal= (decimal * 10).getDecimal()
    }
    return digit
}
 */