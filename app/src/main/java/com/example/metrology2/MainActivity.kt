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
    private var ifLevel = 0
    private var cycleLevel = 0
    private var caseLevel = 0

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
            if (ifLevel != 0) ifLevel--
            if (caseLevel != 0) caseLevel--
            if (cycleLevel != 0) cycleLevel--
            var output = ""

            var allOperatorsKol = 0

            var number = 1
            for (operator in operatorsMap) {
                allOperatorsKol += operator.value
                output += "   $number    ->       ${operator.key}  ->  ${operator.value}\n"
                number++
            }

            var ifKol = 0
            output += "\n"
            number = 1
            for (operator in vlozhOperatorsMap) {
                if (operator.key.startsWith("if")) ifKol += operator.value
                output += "   $number    ->       ${operator.key}  ->  ${operator.value}\n"
                number++
            }
            output += "Абсолютная сложность программы = $ifKol\n"
            output += "Относительная сложность программы = $ifKol / $allOperatorsKol = ${ifKol.toFloat() / allOperatorsKol.toFloat()}\n"
            output += "Расширение метрики Джимба: if - $ifLevel, cycles - $cycleLevel, case - $caseLevel."
            solutionView.text = output
        }
    }

    private fun clean() {
        vlozhOperatorsMap.clear()
        operatorsMap.clear()
        ifLevel = 0
        caseLevel = 0
        cycleLevel = 0
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

                var wasVlozh = false
                var notVlozhWord = ""
                while (j < input[i].length) {
                    var k = j
                    var word = ""
                    while (k < input[i].length && input[i][k] != ' ') {
                        word += input[i][k]
                        k++
                    }

//                    if (word == FUN_WORD) {
//                        var funOperator = ""
//                        k++
//                        while (k < input[i].length && input[i][k] != '(' && input[i][k] != ' ') {
//                            funOperator += input[i][k]
//                            k++
//                        }
//
//                    } else
                    val wordIndex = findOperator(word)
                    if (wordIndex == -1) {
                        checkEndWord(word)
                        // if (!rubyReservedWords.contains(input[i])) notVlozhWord+= "$word "
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
                //  if (notVlozhWord!="" && notVlozhWord!=input[i]) operatorsMap = addElementToMap(operatorsMap, notVlozhWord)
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
//                if (vlozhOperatorsMap[elem] != null) vlozhOperatorsMap[elem] =
//                    vlozhOperatorsMap[elem]?.plus(1) ?: 0
//                else vlozhOperatorsMap[elem] = 1
            }
            i++
        }
        return -1
    }

    private fun checkEndWord(word: String): Boolean {
        if (word == END_WORD) {
            analyzeStack()
            stack.removeLast()
            return true
        }
        return false
    }

    private fun analyzeStack() {
        var ifKol = 0
        var cycleKol = 0
        var caseKol = 0
        for (elem in stack) {
            if (elem == "if") ifKol++
            else if (elem == "case") caseKol++
            else cycleKol++
        }
        if (ifKol > ifLevel) ifLevel = ifKol
        if (caseKol > caseLevel) caseLevel = caseKol
        if (cycleKol > cycleLevel) cycleLevel = cycleKol
    }
}
/*
puts "Number:"
number = gets
sc = 0
l = number%3 #number+=4
arr= [1,2,3,4,5]
for i in 0..10 do
    sc = sc + (arr[i]**l)
    while sc <= 5 do
      puts sc
      sc+=1
       for j in 0..arr.length-1 do
         until item>0 do
           if (item==5) then
             if (item==4) then
              item-=4
               if (item==3) then
                item-=3
                if (item==2) then item-=2
                end
               end
             end
         end
       end
    end
end
 */
