package me.wanttobee.tasktussle

object Util {
    fun String.toLore(maxLineLength: Int): List<String> {
        val loreResult = mutableListOf<String>()
        var colorBuffer = ""
        // currentIndex: the pointer where we are currently at
        var currentIndex = 0

        while (currentIndex < this.length) {
            // endIndex: the position that our currentIndex pointer may never exceed
            var endIndex = currentIndex + maxLineLength
            if (endIndex >= this.length)
                endIndex = this.length
            // if the endIndex is longer/the same as the length of the string, we don't have to bother finding whitespace and stuff
            else {
                // TODO: instead of looping from the end to the start,
                //   we need to loop from the start and increase the end whenever we encounter a colorCode
                //   hereby eliminating spacial difference due to colorCodes
                // we start at the end and find what the closest last whitespace is
                while (endIndex > currentIndex && this[endIndex] != ' ') {
                    endIndex--
                }
                // If no space found, just split at the maxLength
                if (endIndex == currentIndex) {
                    endIndex = currentIndex + maxLineLength
                    // and if the last character happens to be a colorCode,
                    // we remove that from this line and pass it through to the next line, we don't want to cut a code in 2 pieces
                    if(this[endIndex] == '§') endIndex--
                }
            }
            val loreLine = this.substring(currentIndex, endIndex).trim()
            loreResult.add(colorBuffer + loreLine)
            currentIndex = endIndex

            // Find all color codes in loreLine and update colorBuffer
            var colorCodeIndex = loreLine.indexOf('§')
            while (colorCodeIndex != -1 && colorCodeIndex + 1 < loreLine.length) {
                colorBuffer += loreLine.substring(colorCodeIndex, colorCodeIndex + 2)
                colorCodeIndex = loreLine.indexOf('§', colorCodeIndex + 1)
            }
        }
        return loreResult
    }
}
