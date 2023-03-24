package com.example.metrology2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    companion object {
        private const val FUN_WORD = "def"
        private const val ONE_LINE_COMMENT = "#"
        private const val BEGIN_MULTI_LINE_COMMENT = "=begin"
        private const val END_MULTI_LINE_COMMENT = "=end"
        private const val END_WORD = "end"
        private const val WHEN_WORD = "when "
    }

    private val vlozhOperators = arrayOf(
        "case when else end",
        "if then else end",
        "while do end",
        "for in do end",
        "until do end",
    )

    private val searchWords = arrayOf(
        "when", "then", "do", "do", "do"
    )

    private val rubyReservedWords = arrayOf(
        "end",
        "begin",
        "def",
        "return",
        "break",
        "continue",
        "else",
        "elsif",
    )


    private var functions = arrayOf("")

    private var vlozhOperatorsMap: MutableMap<String, Int> = mutableMapOf()
    private var operatorsMap: MutableMap<String, Int> = mutableMapOf()
    private var operatorsKol = 0
    private var stack = ArrayDeque<String>()
    private var level = 0
    private var whenKol = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val solutionView = findViewById<TextView>(R.id.solutionView)
        val inputView = findViewById<EditText>(R.id.inputView)
        findViewById<Button>(R.id.button).setOnClickListener {
            clean()
            val input = inputView.text.toString()
            val correctInput = getCorrectInput(input)
            for (str in correctInput) {
                println(str)
            }
            solution(correctInput)
            if (level != 0) level--
//            val lev = whenKol - 1
//            if (lev> level) level = lev
            var output = ""

            var allOperatorsKol = 0

            var number = 1
            for (operator in operatorsMap) {
                allOperatorsKol += operator.value
//                output += "   $number    ->       ${operator.key}  ->  ${operator.value}\n"
//                number++
            }

            var abslSlozh = 0
            number = 1
            for (operator in vlozhOperatorsMap) {
                abslSlozh += operator.value
//                output += "   $number    ->       ${operator.key}  ->  ${operator.value}\n"
//                number++
            }
            if (whenKol != 0) abslSlozh += whenKol - 1
            output += "Абсолютная сложность программы = $abslSlozh\n"
            output += "Количество операторов = $allOperatorsKol\n"
            output += "Относительная сложность программы = $abslSlozh / $allOperatorsKol = ${abslSlozh.toFloat() / allOperatorsKol.toFloat()}\n"
            output += "Максимальная вложенность = $level."
            solutionView.text = output
        }
    }

    private fun clean() {
        vlozhOperatorsMap.clear()
        operatorsMap.clear()
        stack.clear()
        level = 0
        whenKol = 0
    }

    private fun getCorrectInput(input: String): Array<String> {
        val correctInput: MutableList<String> = mutableListOf()
        var j = 0
        for (i in input.indices) {
            if (input[i] == '\n') {
                correctInput.add(input.substring(j, i).trim())
                j = i + 1
            }
        }
        return correctInput.toTypedArray()
    }

    private fun addElementToMap(
        map: MutableMap<String, Int>,
        element: String
    ): MutableMap<String, Int> {
        if (map[element] != null) map[element] =
            map[element]?.plus(1) ?: 0
        else map[element] = 1
        return map
    }

    private fun solution(input: Array<String>) {

        var isCommented = false
        for (i in input.indices) {
            if (input[i].isNotEmpty()) {
                if (input[i].indexOf(ONE_LINE_COMMENT) != -1) input[i] =
                    input[i].substring(0, input[i].indexOf(ONE_LINE_COMMENT))

                var j = 0
                var comment = ""
                var l = j
                while (l < input[i].length && input[i][l] != ' ') {
                    comment += input[i][l]
                    l++
                }

                if (comment == BEGIN_MULTI_LINE_COMMENT) {
                    j = l
                    isCommented = true
                }
                if (comment == END_MULTI_LINE_COMMENT) isCommented = false
                if (isCommented) continue

                if (input[i].startsWith(WHEN_WORD)) {
                    whenKol++
                    continue
                }

                var wasVlozh = false
                while (j < input[i].length) {
                    var k = j
                    var word = ""
                    while (k < input[i].length && input[i][k] != ' ') {
                        word += input[i][k]
                        k++
                    }

                    val wordIndex = findOperator(word)
                    if (wordIndex == -1) {
                        checkEndWord(word)
                    } else {
                        stack.add(word)
                        wasVlozh = true
                        var found = false
                        while (!found) {
                            var vlozhWord = ""
                            if (k < input[i].length && input[i][k] == ' ') k++
                            while (k < input[i].length && input[i][k] != ' ') {
                                vlozhWord += input[i][k]
                                k++
                            }

                            if (vlozhWord == searchWords[wordIndex] || k == input[i].length) {
                                found = true
                                vlozhOperatorsMap =
                                    addElementToMap(vlozhOperatorsMap, input[i].substring(0, k))
                                operatorsMap =
                                    addElementToMap(operatorsMap, input[i].substring(0, k))
                                if (wordIndex == 3) {
                                    var t = 0
                                    while (t < 2) {
                                        operatorsMap =
                                            addElementToMap(operatorsMap, "for cycle")
                                        t++
                                    }
                                }

                                var ostStr = ""
                                while (k < input[i].length) {
                                    var ostWord = ""
                                    while (k < input[i].length && input[i][k] != ' ') {
                                        ostWord += input[i][k]
                                        k++
                                    }

                                    if (!checkEndWord(ostWord)) {
                                        if (!rubyReservedWords.contains(ostWord)) {
                                            ostStr += ostWord
                                            if (k == input[i].length) operatorsMap =
                                                addElementToMap(operatorsMap, ostStr)
                                        } else if (ostStr != "") {
                                            operatorsMap = addElementToMap(operatorsMap, ostStr)
                                            ostStr = ""
                                        }
                                    }
                                    if (k < input[i].length && input[i][k] == ' ') k++
                                }
                            }
                        }
                    }

                    if (j == k) j++
                    else j = k
                }
                if (!wasVlozh && !rubyReservedWords.contains(input[i])) operatorsMap =
                    addElementToMap(operatorsMap, input[i])
            }
        }
    }


    private fun findOperator(
        word: String
    ): Int {
        var i = 0
        var operator = ""
        for (elem in vlozhOperators) {
            operator = if (elem.contains(' ')) elem.substring(0, elem.indexOf(' '))
            else elem
            if (operator == word) {
                return i
            }
            i++
        }
        return -1
    }

    private fun checkEndWord(word: String): Boolean {
        if (word == END_WORD) {
            if (stack[stack.size - 1].startsWith("case")) {
                val lev = stack.size - 1 + whenKol
                if (lev > level) level = lev
                whenKol = 0
            } else if (stack.size > level) {
                level = stack.size
            }
            stack.removeLast()
            return true
        }
        return false
    }

//    private fun analyzeStack() {
//        var ifKol = 0
//        var cycleKol = 0
//        var caseKol = 0
//        for (elem in stack) {
//            if (elem == "if") ifKol++
//            else if (elem == "case") caseKol++
//            else cycleKol++
//        }
//        if (ifKol > ifLevel) ifLevel = ifKol
//        if (caseKol > caseLevel) caseLevel = caseKol
//        if (cycleKol > cycleLevel) cycleLevel = cycleKol
//    }
}

