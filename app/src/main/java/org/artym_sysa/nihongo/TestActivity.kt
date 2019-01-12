package org.artym_sysa.nihongo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.gson.Gson
import org.artym_sysa.nihongo.room.AppDatabase
import org.artym_sysa.nihongo.room.entity.Word


class TestActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var DB: AppDatabase

    private var groupId: Long = 0
    private var quantity: Int = 0

    private lateinit var selectedModes: MutableList<MODE>
    private lateinit var selectedTypes: MutableList<TYPE>
    private lateinit var words: MutableList<Word>
    private lateinit var allWords: MutableList<Word>

    private lateinit var testControls: LinearLayout

    private lateinit var taskText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var correctCounter: TextView
    private lateinit var incorrectCounter: TextView

    private lateinit var answerLeftCounter: TextView
    private var correct: Int = 0
    private var incorrect: Int = 0
    private var correctIncorrectState: Boolean = true
    private var repeatExercise: Boolean = false
    private var allowRepeatIncorrectExercises = false
    private lateinit var currentRepeatExercise: TestRecord

    private lateinit var currentMode: MODE
    private lateinit var currentType: TYPE
    private lateinit var currentWord: Word

    private lateinit var history: MutableList<TestRecord>
    private lateinit var incorrectExercises: MutableList<TestRecord>

    private lateinit var currentControls: LinearLayout

    override fun onBackPressed() {
        AlertDialog.Builder(this@TestActivity)
                .setMessage("Вы действительно хотите закончить тест? Прогрсс не будет сохранён")
                .setPositiveButton("Да", { d, _ ->
                    setResult(Activity.RESULT_CANCELED, Intent())
                    finish()
                })
                .setNegativeButton("Отменить", { d, _ -> d.cancel() })
                .create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        testControls = findViewById(R.id.testControls)
        taskText = findViewById(R.id.cardText)

        progressBar = findViewById(R.id.testProgressBar)
        correctCounter = findViewById(R.id.correctAnswerCounter)
        incorrectCounter = findViewById(R.id.incorrectAnswerCounter)
        answerLeftCounter = findViewById(R.id.answetLeftCounter)

        initTestData()

        generateTask()
    }

    private fun generateTask() {
        repeatExercise = false

        if (!words.isEmpty()) {
            currentMode = selectedModes.shuffled().first()
            currentType = selectedTypes.shuffled().first()
            currentWord = words.shuffled().first()
            allWords.remove(currentWord)
        } else {
            if (!incorrectExercises.isEmpty()) {
                currentRepeatExercise = incorrectExercises.shuffled().first()

                currentMode = currentRepeatExercise.mode
                currentType = currentRepeatExercise.type
                currentWord = allWords.first { it.id == currentRepeatExercise.wordId }

                allWords.remove(currentWord)

                repeatExercise = true
            }
        }

        if (!words.isEmpty() || repeatExercise) {
            testControls.removeAllViews()

            when (currentMode) {
                MODE.SELECTION_MODE -> {
                    setTask(currentWord, currentMode, currentType)

                    var j = 0
                    var variant: Word
                    var variantWords = mutableListOf<Word>(currentWord)

                    allWords.shuffle()

                    do {
                        do {
                            variant = allWords.get(j)
                            j++
                        } while (variantWords.contains(variant))
                        variantWords.add(variant)
                    } while (variantWords.size < 4)

                    variantWords.shuffle()

                    currentControls = LayoutInflater.from(this).inflate(R.layout.test_selection_layout, testControls, false) as LinearLayout

                    var v1Btn = currentControls.findViewById<Button>(R.id.selectionMode1stVariant)
                    var v2Btn = currentControls.findViewById<Button>(R.id.selectionMode2ndVariant)
                    var v3Btn = currentControls.findViewById<Button>(R.id.selectionMode3rdVariant)
                    var v4Btn = currentControls.findViewById<Button>(R.id.selectionMode4thVariant)

                    when (currentType) {
                        TYPE.W2M_TYPE, TYPE.R2M_TYPE -> {
                            v1Btn.text = variantWords[0].meaning
                            v2Btn.text = variantWords[1].meaning
                            v3Btn.text = variantWords[2].meaning
                            v4Btn.text = variantWords[3].meaning
                        }

                        TYPE.M2W_TYPE, TYPE.R2W_TYPE -> {
                            v1Btn.text = variantWords[0].text
                            v2Btn.text = variantWords[1].text
                            v3Btn.text = variantWords[2].text
                            v4Btn.text = variantWords[3].text
                        }
                        TYPE.W2R_TYPE, TYPE.M2R_TYPE -> {
                            v1Btn.text = variantWords[0].reading
                            v2Btn.text = variantWords[1].reading
                            v3Btn.text = variantWords[2].reading
                            v4Btn.text = variantWords[3].reading
                        }
                    }

                    v1Btn.setOnClickListener(this)
                    v2Btn.setOnClickListener(this)
                    v3Btn.setOnClickListener(this)
                    v4Btn.setOnClickListener(this)

                    testControls.addView(currentControls)

                }
                MODE.WRITE_MODE -> {
                    setTask(currentWord, currentMode, currentType)

                    currentControls = LayoutInflater.from(this).inflate(R.layout.test_input_correct_layout, testControls, false) as LinearLayout
                    val applyBtn = currentControls.findViewById<Button>(R.id.writeTestControlApply)
                    applyBtn.setOnClickListener(this)

                    testControls.addView(currentControls)
                    currentControls.findViewById<TextView>(R.id.writeTestControlAnswer).requestFocus()

                }
                MODE.CORRECT_INCORRECT_MODE -> {
                    setTask(currentWord, currentMode, currentType)

                    currentControls = LayoutInflater.from(this).inflate(R.layout.test_correct_incorect_buttons_layout, testControls, false) as LinearLayout

                    var correctBtn = currentControls.findViewById<Button>(R.id.correctBtn)
                    var incorrectBtn = currentControls.findViewById<Button>(R.id.incorrectBtn)

                    correctBtn.setOnClickListener(this)
                    incorrectBtn.setOnClickListener(this)

                    testControls.addView(currentControls)
                }
            }
        } else {

            var intent = Intent(this@TestActivity, ResultTestActivity::class.java)
            var bundle = Bundle()

            bundle.putInt("correct", correct)
            bundle.putInt("incorrect", incorrect)
            bundle.putString("history", Gson().toJson(history))
            intent.putExtras(bundle)
            this.finish()
            startActivity(intent)
        }
    }

    private fun setTask(currentWord: Word, currentMode: MODE, currentType: TYPE) {
        taskText.text =
                when (currentMode) {
                    MODE.WRITE_MODE ->
                        when (currentType) {
                            TYPE.W2M_TYPE -> "Слово: ${currentWord.text}\nВведите значение"
                            TYPE.W2R_TYPE -> "Слово: ${currentWord.text}\nВведите чтение"
                            TYPE.R2W_TYPE -> "Чтение: ${currentWord.reading}\nВведите слово"
                            TYPE.R2M_TYPE -> "Чтение: ${currentWord.reading}\nВведите значение"
                            TYPE.M2W_TYPE -> "Значение: ${currentWord.meaning}\nВведите слово"
                            TYPE.M2R_TYPE -> "Значение: ${currentWord.meaning}\nВведите чтение"
                        }
                    MODE.CORRECT_INCORRECT_MODE -> {
                        correctIncorrectState = generateCorrectIncorrectState()

                        when (correctIncorrectState) {
                            false -> {
                                var incorrectWord = allWords.shuffled().first()
                                "Правильно ли составленна пара:\n\n" +
                                        when (currentType) {
                                            TYPE.W2M_TYPE -> "Слово: ${currentWord.text}\nЗначение: ${incorrectWord.meaning}"
                                            TYPE.W2R_TYPE -> "Слово: ${currentWord.text}\nЧтение: ${incorrectWord.reading}"
                                            TYPE.R2W_TYPE -> "Чтение: ${currentWord.reading}\nCлово: ${incorrectWord.text}"
                                            TYPE.R2M_TYPE -> "Чтение: ${currentWord.reading}\nЗначение:${incorrectWord.meaning}"
                                            TYPE.M2W_TYPE -> "Значение: ${currentWord.meaning}\nСлово: ${incorrectWord.text}"
                                            TYPE.M2R_TYPE -> "Значение: ${currentWord.meaning}\nЧтение: ${incorrectWord.reading}"
                                        }
                            }
                            else -> {
                                "Правильно ли составленна пара:\n\n" +
                                        when (currentType) {
                                            TYPE.W2M_TYPE -> "Слово: ${currentWord.text}\nЗначение: ${currentWord.meaning}"
                                            TYPE.W2R_TYPE -> "Слово: ${currentWord.text}\nЧтение: ${currentWord.reading}"
                                            TYPE.R2W_TYPE -> "Чтение: ${currentWord.reading}\nCлово: ${currentWord.text}"
                                            TYPE.R2M_TYPE -> "Чтение: ${currentWord.reading}\nЗначение:${currentWord.meaning}"
                                            TYPE.M2W_TYPE -> "Значение: ${currentWord.meaning}\nСлово: ${currentWord.text}"
                                            TYPE.M2R_TYPE -> "Значение: ${currentWord.meaning}\nЧтение: ${currentWord.reading}"
                                        }
                            }
                        }
                    }
                    MODE.SELECTION_MODE ->
                        when (currentType) {
                            TYPE.W2M_TYPE -> "Слово: ${currentWord.text}\nВыберите значение"
                            TYPE.W2R_TYPE -> "Слово: ${currentWord.text}\nВыберите чтение"
                            TYPE.R2W_TYPE -> "Чтение: ${currentWord.reading}\nВыберите слово"
                            TYPE.R2M_TYPE -> "Чтение: ${currentWord.reading}\nВыберите значение"
                            TYPE.M2W_TYPE -> "Значение: ${currentWord.meaning}\nВыберите слово"
                            TYPE.M2R_TYPE -> "Значение: ${currentWord.meaning}\nВыберите чтение"
                        }
                }
    }

    private fun generateCorrectIncorrectState(): Boolean = Math.random() < 0.5

    private fun initTestData() {
        groupId = intent.extras["groupId"] as Long
        quantity = intent.extras["quantity"] as Int

        selectedTypes = mutableListOf()

        if (getBooleanExtra("W2M_type")) selectedTypes.add(TYPE.W2M_TYPE)
        if (getBooleanExtra("M2W_type")) selectedTypes.add(TYPE.M2W_TYPE)
        if (getBooleanExtra("W2R_type")) selectedTypes.add(TYPE.W2R_TYPE)
        if (getBooleanExtra("R2W_type")) selectedTypes.add(TYPE.R2W_TYPE)
        if (getBooleanExtra("R2M_type")) selectedTypes.add(TYPE.R2M_TYPE)
        if (getBooleanExtra("M2R_type")) selectedTypes.add(TYPE.M2R_TYPE)

        selectedModes = mutableListOf()

        if (getBooleanExtra("selection_mode")) selectedModes.add(MODE.SELECTION_MODE)
        if (getBooleanExtra("correct_incorrect_mode")) selectedModes.add(MODE.CORRECT_INCORRECT_MODE)
        if (getBooleanExtra("write_mode")) selectedModes.add(MODE.WRITE_MODE)

        allowRepeatIncorrectExercises = getBooleanExtra("repeatIncorrectExercises")

        DB = AppDatabase.getInstance(this@TestActivity)

        allWords = mutableListOf<Word>()
        DB.wordDao().getByGroupId(groupId).forEach { allWords.add(it) }
        words = mutableListOf()
        words.addAll(allWords.shuffled().take(quantity))

        correct = 0
        incorrect = 0

        correctCounter.text = correct.toString()
        incorrectCounter.text = incorrect.toString()
        answerLeftCounter.text = quantity.toString()

        history = mutableListOf()
        incorrectExercises = mutableListOf()

        progressBar.max = quantity
        progressBar.progress = 0
    }

    override fun onClick(v: View?) {
        var historyStatus = true
        var answer = ""

        when (v?.id) {
            R.id.correctBtn, R.id.incorrectBtn -> {
                currentControls.findViewById<Button>(R.id.correctBtn).isClickable = false
                currentControls.findViewById<Button>(R.id.incorrectBtn).isClickable = false

                when (v.id) {
                    R.id.correctBtn -> {
                        answer = "Верно"

                        if (correctIncorrectState == true) {
                            historyStatus = true
                            currentControls.findViewById<Button>(v.id).setBackgroundResource(R.drawable.bordered_btn_color_correct)
                        } else {
                            historyStatus = false

                            currentControls.findViewById<Button>(R.id.incorrectBtn).setBackgroundResource(R.drawable.bordered_btn_color_correct)
                            currentControls.findViewById<Button>(v.id).setBackgroundResource(R.drawable.bordered_btn_color_incorrect)
                        }
                    }
                    R.id.incorrectBtn -> {
                        answer = "Неверно"
                        if (correctIncorrectState == false) {
                            historyStatus = true
                            currentControls.findViewById<Button>(v.id).setBackgroundResource(R.drawable.bordered_btn_color_correct)
                        } else {
                            historyStatus = false

                            currentControls.findViewById<Button>(R.id.incorrectBtn).setBackgroundResource(R.drawable.bordered_btn_color_correct)
                            currentControls.findViewById<Button>(v.id).setBackgroundResource(R.drawable.bordered_btn_color_incorrect)
                        }
                    }
                }
            }

            R.id.selectionMode1stVariant,
            R.id.selectionMode2ndVariant,
            R.id.selectionMode3rdVariant,
            R.id.selectionMode4thVariant -> {

                currentControls.findViewById<Button>(R.id.selectionMode1stVariant).isClickable = false
                currentControls.findViewById<Button>(R.id.selectionMode2ndVariant).isClickable = false
                currentControls.findViewById<Button>(R.id.selectionMode3rdVariant).isClickable = false
                currentControls.findViewById<Button>(R.id.selectionMode4thVariant).isClickable = false

                listOf(
                        R.id.selectionMode1stVariant,
                        R.id.selectionMode2ndVariant,
                        R.id.selectionMode3rdVariant,
                        R.id.selectionMode4thVariant
                ).forEach {
                    answer = findViewById<Button>(it).text.toString()

                    if (answer.toLowerCase() ==
                            when (currentType) {
                                TYPE.W2M_TYPE, TYPE.R2M_TYPE -> currentWord.meaning.toLowerCase()
                                TYPE.M2W_TYPE, TYPE.R2W_TYPE -> currentWord.text.toLowerCase()
                                TYPE.W2R_TYPE, TYPE.M2R_TYPE -> currentWord.reading.toLowerCase()
                            }
                    ) {
                        currentControls.findViewById<Button>(it).setBackgroundResource(R.drawable.bordered_btn_color_correct)

                        if (it == v.id) historyStatus = true
                        else {
                            historyStatus = false
                            currentControls.findViewById<Button>(v.id).setBackgroundResource(R.drawable.bordered_btn_color_incorrect)
                        }
                    }
                }
            }

            R.id.writeTestControlApply -> {
                currentControls.findViewById<Button>(R.id.writeTestControlApply).isClickable = false

                answer = currentControls.findViewById<EditText>(R.id.writeTestControlAnswer).text.toString()

                currentControls.findViewById<Button>(v.id).setBackgroundResource(
                        if (when (currentType) {
                                    TYPE.W2M_TYPE, TYPE.R2M_TYPE -> answer.toLowerCase() == currentWord.meaning.toLowerCase()
                                    TYPE.M2W_TYPE, TYPE.R2W_TYPE -> answer.toLowerCase() == currentWord.text.toLowerCase()
                                    TYPE.W2R_TYPE, TYPE.M2R_TYPE -> answer.toLowerCase() == currentWord.reading.toLowerCase()
                                }
                        ) {
                            historyStatus = true
                            R.drawable.bordered_btn_color_correct
                        } else {
                            historyStatus = false
                            R.drawable.bordered_btn_color_incorrect
                        }
                )
            }
        }

        when (historyStatus) {
            true -> {
                correct++

                history.add(TestRecord(currentWord.id, currentMode, currentType))

                if (repeatExercise) incorrectExercises.remove(currentRepeatExercise)
                else words.remove(currentWord)

                progressBar.progress = progressBar.progress + 1
                answerLeftCounter.text = (--quantity).toString()
            }
            false -> {
                var record = TestRecord(currentWord.id, currentMode, currentType, false, answer)

                incorrect++

                history.add(record)

                if (allowRepeatIncorrectExercises) {
                    if (!incorrectExercises.any { it.wordId == currentWord.id }) {
                        incorrectExercises.add(record)
                    }
                } else {
                    progressBar.progress = progressBar.progress + 1
                    answerLeftCounter.text = (--quantity).toString()
                }

                words.remove(currentWord)
            }
        }

        correctCounter.text = correct.toString()
        incorrectCounter.text = incorrect.toString()

        allWords.add(currentWord)

        Handler().postDelayed({ generateTask() }, 500)
    }

    private fun getBooleanExtra(key: String): Boolean = intent.extras[key] as Boolean

    enum class MODE { SELECTION_MODE, CORRECT_INCORRECT_MODE, WRITE_MODE }
    enum class TYPE { W2M_TYPE, M2W_TYPE, W2R_TYPE, R2W_TYPE, R2M_TYPE, M2R_TYPE }
}