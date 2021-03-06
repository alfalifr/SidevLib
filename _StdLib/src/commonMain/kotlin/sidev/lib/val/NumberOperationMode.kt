package sidev.lib.`val`

enum class NumberOperationMode(val sign: String, val reverseSign: String) {
    /**
     * Operasi angka yang menggunakan operator tambah atau kurang (+ -)
     */
    INCREMENTAL("+", "-"),

    /**
     * Operasi angka yang menggunakan operator kali atau bagi (* /)
     */
    MULTIPLICATIONAL("*", "/"),

    /**
     * Operasi angka yang menggunakan operator pangkat atau akar
     */
    EXPONENTIAL("^", "~")
}