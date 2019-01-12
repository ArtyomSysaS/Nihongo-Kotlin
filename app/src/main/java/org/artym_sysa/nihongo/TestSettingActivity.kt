package org.artym_sysa.nihongo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import es.dmoral.toasty.Toasty
import org.artym_sysa.nihongo.room.AppDatabase
import org.jetbrains.anko.design.longSnackbar

class TestSettingActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    lateinit var DB: AppDatabase
    lateinit var startTestBtn: Button

    lateinit var selectedWords: SeekBar
    lateinit var selectedWordsTextView: TextView

    lateinit var nIzuchCbx: CheckBox
    lateinit var izuchCbx: CheckBox

    lateinit var wordMeaningSwitch: Switch
    lateinit var meaningWordSwitch: Switch
    lateinit var wordReadingSwitch: Switch
    lateinit var readingWordSwitch: Switch
    lateinit var readingMeaningSwitch: Switch
    lateinit var meaningReadingSwitch: Switch

    lateinit var correctSelectionSwitch: Switch
    lateinit var correctIncorrectSwitch: Switch
    lateinit var writeCorrectSwitch: Switch

    lateinit var repeatIncorrectExercisesSwitch: Switch

    lateinit var valid: MutableList<Boolean>

    var groidId: Long = 0
    var maxWords: Int = 0
    var selectedWordQuantity = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_setting)

        groidId = intent.extras["groupId"] as Long

        DB = AppDatabase.getInstance(this@TestSettingActivity)
        maxWords = DB.wordDao().getQuantityByGroupId(groidId).toInt()

        startTestBtn = findViewById(R.id.startTestBtn)
        startTestBtn.setOnClickListener(this)

        selectedWords = findViewById(R.id.quantitySeekbar)
        selectedWords.max = maxWords
        selectedWords.progress = maxWords
        selectedWords.setOnSeekBarChangeListener(this)

        selectedWordsTextView = findViewById(R.id.quantityTextView)
        selectedWordsTextView.text = "Слов: $maxWords"

        nIzuchCbx = findViewById(R.id.nIzuchCbx)
        izuchCbx = findViewById(R.id.izuchCbx)

        nIzuchCbx.setOnCheckedChangeListener(this)
        izuchCbx.setOnCheckedChangeListener(this)

        wordMeaningSwitch = findViewById(R.id.WordMeaningSwitch)
        meaningWordSwitch = findViewById(R.id.MeaningWordSwitch)
        wordReadingSwitch = findViewById(R.id.WordReadingSwitch)
        readingWordSwitch = findViewById(R.id.ReadingWordSwitch)
        readingMeaningSwitch = findViewById(R.id.ReadingMeaningSwitch)
        meaningReadingSwitch = findViewById(R.id.MeaningRadingSwitch)

        wordMeaningSwitch.setOnCheckedChangeListener(this)
        meaningWordSwitch.setOnCheckedChangeListener(this)
        wordReadingSwitch.setOnCheckedChangeListener(this)
        readingWordSwitch.setOnCheckedChangeListener(this)
        readingMeaningSwitch.setOnCheckedChangeListener(this)
        meaningReadingSwitch.setOnCheckedChangeListener(this)

        correctSelectionSwitch = findViewById(R.id.CorrectSelectionSwitch)
        correctIncorrectSwitch = findViewById(R.id.CorrectIncorrectSwitch)
        writeCorrectSwitch = findViewById(R.id.WriteCorrectSwitch)

        if (maxWords < 4) {
            correctSelectionSwitch.isEnabled = false
            correctSelectionSwitch.isChecked = false

            if (maxWords < 2) {
                correctIncorrectSwitch.isChecked = false
                correctIncorrectSwitch.isEnabled = false

                writeCorrectSwitch.isChecked = true
            } else {
                correctIncorrectSwitch.isChecked = true
            }
        }


        correctSelectionSwitch.setOnCheckedChangeListener(this)
        correctIncorrectSwitch.setOnCheckedChangeListener(this)
        writeCorrectSwitch.setOnCheckedChangeListener(this)

        repeatIncorrectExercisesSwitch = findViewById(R.id.RepeatIncorrectExercisesSwitch)

        valid = mutableListOf(true, true, true)
        selectedWordQuantity = maxWords
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView) {
            izuchCbx, nIzuchCbx -> {

                val delta = DB.wordDao().getQuantityByGroupIdAndFilterByStatus(groidId,
                        when (buttonView.id) {
                            R.id.nIzuchCbx -> 0
                            R.id.izuchCbx -> 1
                            else -> -1
                        }
                ).toInt()

                when (isChecked) {
                    true -> maxWords += delta
                    false -> maxWords -= delta
                }

                valid[0] =
                        if (!izuchCbx.isChecked && !nIzuchCbx.isChecked) {
                            Toasty.error(this@TestSettingActivity, "Выберите хотя бы одну группу слов", Toast.LENGTH_SHORT).show()

                            selectedWords.isEnabled = false
                            selectedWordQuantity = 0
                            selectedWordsTextView.text = "Слов: $selectedWordQuantity"

                            false
                        } else {
                            var prevProgress = selectedWords.progress

                            with(selectedWords) {
                                isEnabled = true
                                max = maxWords
                                progress = if (prevProgress > max) max else prevProgress
                            }

                            true
                        }
            }
            wordMeaningSwitch, meaningWordSwitch, wordReadingSwitch, readingWordSwitch ->
                valid[1] =
                        if (!wordMeaningSwitch.isChecked
                                && !wordReadingSwitch.isChecked
                                && !meaningWordSwitch.isChecked
                                && !readingWordSwitch.isChecked
                                && !readingMeaningSwitch.isChecked
                                && !meaningReadingSwitch.isChecked) {
                            Toasty.error(this@TestSettingActivity, "Выберите хотя бы один тип", Toast.LENGTH_SHORT).show()

                            false
                        } else true

            correctSelectionSwitch, correctIncorrectSwitch, writeCorrectSwitch ->
                valid[2] =
                        if (!correctSelectionSwitch.isChecked
                                && !correctIncorrectSwitch.isChecked
                                && !writeCorrectSwitch.isChecked) {
                            Toasty.error(this@TestSettingActivity, "Выберите хотя бы один режим", Toast.LENGTH_SHORT).show()

                            false
                        } else true
        }

        checkValid()
    }

    private fun checkValid(): Boolean = if (!valid[0] || !valid[1] || !valid[2]) false else true

    override fun onClick(v: View?) {
        if (!checkValid()) {
            var msg = ""

            if (!valid[0])
                msg = "Выберите хотя бы одну группу слов"

            if (!valid[1])
                if (msg.isBlank()) msg = "Выберите хотя бы один тип"
                else msg += ", тип"

            if (!valid[2]) {
                if (msg.isBlank()) msg = "Выберите хотя бы один режим тестирования"
                else msg += " и режим тестирования"
            } else {
                if (!msg.contains(",")) msg += " тестирования"
                else msg = "Выберите хотя бы одну группу слов и тип тестирования"
            }

            longSnackbar(findViewById<ScrollView>(R.id.testSettingScrolView), msg)
        } else {
            startTest()
        }
    }

    private fun startTest() {
        var intent = Intent(this@TestSettingActivity, TestActivity::class.java)
        var bundle = Bundle()

        with(bundle) {

            putLong("groupId", groidId)
            putInt("quantity", selectedWordQuantity)

            putBoolean("W2M_type", wordMeaningSwitch.isChecked)
            putBoolean("M2W_type", meaningWordSwitch.isChecked)
            putBoolean("W2R_type", wordReadingSwitch.isChecked)
            putBoolean("R2W_type", readingWordSwitch.isChecked)
            putBoolean("R2M_type", readingMeaningSwitch.isChecked)
            putBoolean("M2R_type", meaningReadingSwitch.isChecked)

            putBoolean("selection_mode", correctSelectionSwitch.isChecked)
            putBoolean("correct_incorrect_mode", correctIncorrectSwitch.isChecked)
            putBoolean("write_mode", writeCorrectSwitch.isChecked)

            putBoolean("repeatIncorrectExercises", repeatIncorrectExercisesSwitch.isChecked)
        }

        intent.putExtras(bundle)

        startActivity(intent)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        selectedWordQuantity = if (progress < 1) 1 else progress
        selectedWordsTextView.text = "Слов: $selectedWordQuantity"
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
