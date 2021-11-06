package studio.clapp.wheelpicker.extensions

fun Int.formatLeadingZero(): String = if (this.toString().length == 1) {
    "0$this"
} else {
    this.toString()
}

fun Int.checkSixtyMinutes(): Int = if (this == 60) {
    0
} else {
    this
}
