package ehn.techiop.hcert.android.chain

class ValSuiteService {

    private val prefix = "AT01"

    fun encode(input: String): String {
        return "$prefix$input";
    }

    fun decode(input: String): String {
        return input.drop(prefix.length)
    }

}