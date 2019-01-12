package org.artym_sysa.nihongo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.dmoral.toasty.Toasty
import org.artym_sysa.nihongo.room.AppDatabase
import org.artym_sysa.nihongo.room.entity.Group
import org.artym_sysa.nihongo.room.entity.GroupNameAndId
import org.artym_sysa.nihongo.room.entity.Word
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.toast
import java.util.*

class GroupActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, TextWatcher, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

    }

    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
        when (scrollState) {
            AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> fab.show()
            else -> fab.hide()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        println("+++++")
    }

    private lateinit var DB: AppDatabase
    private lateinit var currentGroup: Group
    private lateinit var words: List<Word>
    private lateinit var listView: ListView
    private lateinit var listadapter: WordListAdapter
    private lateinit var extraFieldsLayout: LinearLayout
    private lateinit var groups: List<GroupNameAndId>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var sortSpinner: Spinner
    private lateinit var autoCompleteGroupTextView: AutoCompleteTextView
    lateinit var fab: FloatingActionButton
    private var groupsName = mutableListOf<String>()
    private var groupPosition: Int = 0
    lateinit var toolbar: Toolbar

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        DB = AppDatabase.getInstance(this@GroupActivity)

        currentGroup = DB.groupDao().getById(intent.extras["groupId"] as Long)

        val header = LayoutInflater.from(this@GroupActivity).inflate(R.layout.word_filter_listview_header, null)

        autoCompleteGroupTextView = header.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        autoCompleteGroupTextView.addTextChangedListener(this)

        sortSpinner = header.findViewById(R.id.sortSpinner)

        header.findViewById<Spinner>(R.id.sortSpinner).onItemSelectedListener = this
        header.findViewById<Button>(R.id.clearButton).setOnClickListener({
            autoCompleteGroupTextView.text.clear()
            reloadListView()
        })

        header.findViewById<TextView>(R.id.groupTitle).text = currentGroup.name
        header.findViewById<CardView>(R.id.examCardBtn).setOnClickListener({
            if(listadapter.count!=0) openTestSettingActivity()
            else Toasty.info(this@GroupActivity, "Нельзя начать тест. В группе нет слов", Toast.LENGTH_SHORT).show()
        })

        header.findViewById<CardView>(R.id.cardsCardBtn).setOnClickListener({
            if(listadapter.count!=0)             openCardsActivity()

            else Toasty.info(this@GroupActivity, "Нельзя открыть карточки. В группе нет слов", Toast.LENGTH_SHORT).show()

        })

        listView = findViewById(R.id.wordsListView)
        listView.addHeaderView(header)
        listView.setOnItemClickListener({ adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            val intent = Intent(this@GroupActivity, GroupWordsCardsActivity::class.java)
            val bundle = Bundle()

            bundle.putLong("groupId", currentGroup.id)
            bundle.putLong("wordId", listadapter.getItem(i - 1)?.id!!)
            intent.putExtras(bundle)

            startActivity(intent)
        })

        listView.setOnScrollListener(this)


        toolbar = findViewById(R.id.toolbar)
        toolbar.findViewById<TextView>(R.id.activityTitle).text = currentGroup.name


        (LayoutInflater.from(this@GroupActivity)
                .inflate(R.layout.back_button, toolbar.findViewById(R.id.backButtonLayout), false) as Button).setOnClickListener(
                {
                    back()
                }
        )

        setSupportActionBar(toolbar)

        registerForContextMenu(listView)

        reloadListView()

        fab = findViewById<FloatingActionButton>(R.id.wFab)
        fab.setOnClickListener({ showAddNewWordDialog() })

        initGroups()

    }

    private fun back() {
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        if (v?.id === R.id.wordsListView) {
            val menuItems = resources.getStringArray(R.array.word_context_menu)
            menuItems.indices.forEach { menu?.add(Menu.NONE, it, it, menuItems[it]) }
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            0 -> openCard(item)
            1 -> editWord(item)
            2 -> replaceWord(item)
            3 -> showOnDeleteWordDialog(item)
        }

        return super.onContextItemSelected(item)
    }

    private fun openCard(item: MenuItem) {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedWordPosition = info.position

        val intent = Intent(this@GroupActivity, GroupWordsCardsActivity::class.java)
        val bundle = Bundle()

        bundle.putLong("groupId", currentGroup.id)
        bundle.putLong("wordId", listadapter.getItem(selectedWordPosition - 1)?.id!!)
        intent.putExtras(bundle)

        startActivity(intent)
    }

    private fun editWord(item: MenuItem) {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedWordPosition = info.position - 1
        val selectedWord = listadapter.getItem(selectedWordPosition)


        val layoutInflater = LayoutInflater.from(this@GroupActivity)
        val view = layoutInflater.inflate(R.layout.new_word_dialog, null)
        var text = view.findViewById<EditText>(R.id.text)
        var meaning = view.findViewById<EditText>(R.id.meaning)
        var reading = view.findViewById<EditText>(R.id.reading)
        var status = view.findViewById<RadioGroup>(R.id.status)

        var addFieldBtn = view.findViewById<Button>(R.id.addFieldBtn)
        extraFieldsLayout = view.findViewById<LinearLayout>(R.id.extraFields)

        extraFieldsLayout.removeAllViews()


        //var group = view.findViewById<Spinner>(R.id.group)

        //group.adapter = adapter
        //group.setSelection(groupPosition)

        addFieldBtn.setOnClickListener({
            LayoutInflater.from(this@GroupActivity).inflate(R.layout.extra_field, extraFieldsLayout, true)
        })

        text.setText(selectedWord?.text)
        meaning.setText(selectedWord?.meaning)
        reading.setText(selectedWord?.reading)
        status.check(if (selectedWord?.status == 0) R.id.statusNew else R.id.statusComplete)


        val fields = Gson().fromJson<List<WordField>>(selectedWord?.fields, object : TypeToken<List<WordField>>() {}.type)

        if (fields != null) {
            for (field in fields) {
                LayoutInflater.from(this@GroupActivity).inflate(R.layout.extra_field, extraFieldsLayout, true)

                extraFieldsLayout.getChildAt(extraFieldsLayout.childCount - 1).findViewById<Spinner>(R.id.extraFieldKey).setSelection(
                        when (field.key) {
                            "Значение" -> 0
                            "Чтение" -> 1
                            "Написание" -> 2
                            "Заметка" -> 3
                            else -> 4
                        }
                )
                extraFieldsLayout.getChildAt(extraFieldsLayout.childCount - 1).findViewById<TextView>(R.id.extraFieldValue).text = field.value
            }
        }


        val dialog = AlertDialog.Builder(this@GroupActivity)
                .setView(view)
                .setPositiveButton("Изменить", null)
                .setNegativeButton("Отменить", { d, _ -> d.cancel() })
                .create()

        dialog.setOnShowListener { d ->
            val button = (d as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            button.setOnClickListener { v ->
                var ok = true

                if (text.text.toString().isNullOrBlank()) {
                    Toasty.error(this@GroupActivity, "Нельзя изменить текст на пустое значение", Toast.LENGTH_SHORT).show()
                    ok = false
                }

                if (meaning.text.toString().isNullOrBlank()) {
                    Toasty.error(this@GroupActivity, "Нельзя изменить значение на пустое", Toast.LENGTH_SHORT).show()
                    ok = false
                }

                if (ok) {

                    var newWord = Word(
                            id = selectedWord?.id!!,
                            groupId = currentGroup.id,//groups.get(group.selectedItemPosition).id,
                            text = text.text.toString(),
                            meaning = meaning.text.toString(),
                            reading = reading.text.toString(),
                            status = when (status.checkedRadioButtonId) {
                                R.id.statusNew -> 0
                                else -> 1
                            }
                    )

                    if (extraFieldsLayout.childCount != 0) {
                        var fields = ArrayList<WordField>()

                        for (i in 0..extraFieldsLayout.childCount - 1) {
                            var value = extraFieldsLayout.getChildAt(i).findViewById<EditText>(R.id.extraFieldValue).text.toString()

                            if (!value.isNullOrBlank()) {
                                fields.add(
                                        WordField(
                                                when (extraFieldsLayout.getChildAt(i).findViewById<Spinner>(R.id.extraFieldKey).selectedItemPosition) {
                                                    0 -> "Значение"
                                                    1 -> "Чтение"
                                                    2 -> "Написание"
                                                    3 -> "Заметка"
                                                    else -> "Другое"
                                                },
                                                value
                                        )
                                )
                            }
                        }
                        newWord.fields = Gson().toJson(fields)
                    }

                    DB.wordDao().update(newWord)

                    dialog.cancel()

                    reloadListView()

                    longSnackbar(findViewById<CoordinatorLayout>(R.id.activityGroup), "Слово \"${text.text}\" изменено", "Отменить") {
                        DB.wordDao().update(selectedWord)
                        reloadListView()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun replaceWord(item: MenuItem) {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedWordPosition = info.position - 1

        val selectedWord = listadapter.getItem(selectedWordPosition)

        AlertDialog.Builder(this@GroupActivity)
                .setTitle("Выберите группу")
                .setItems(groupsName.toTypedArray()) { _, which ->
                    var oldGroupId = selectedWord?.groupId

                    selectedWord?.groupId = groups.get(which).id
                    DB.wordDao().update(selectedWord!!)

                    reloadListView()

                    longSnackbar(findViewById<CoordinatorLayout>(R.id.activityGroup), "Слово \"${selectedWord.text}\" перенесено", "Отменить") {
                        selectedWord.groupId = oldGroupId!!
                        DB.wordDao().update(selectedWord)

                        reloadListView()
                    }
                }
                .setNegativeButton("Отменить") { _, _ -> }
                .create()
                .show()
    }

    fun DeleteView(v: View) {
        extraFieldsLayout.removeView(v.parent as View)
    }

    private fun showOnDeleteWordDialog(item: MenuItem) {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedWordPosition = info.position - 1

        val selectedWord = listadapter.getItem(selectedWordPosition)

        AlertDialog.Builder(this@GroupActivity).setTitle("Подтверждение")
                .setMessage("Вы действительно хотите удалить слово \"${selectedWord?.text}\"?\nВсе данные о этом слове будут удалены и их нельзя будет восстановить")
                .setPositiveButton("Удалить") { _, _ ->


                    Snackbar.make(findViewById<CoordinatorLayout>(R.id.activityGroup), " ", Snackbar.LENGTH_LONG)
                            .setAction("Отменить удаление", {})
                            .setDuration(3000).addCallback(object : Snackbar.Callback() {

                        override fun onDismissed(snackbar: Snackbar?, event: Int) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                deleteWord(selectedWord!!)
                            }

                            if (event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                                deleteWord(selectedWord!!)
                            }
                        }

                        override fun onShown(snackbar: Snackbar?) {
                        }
                    }).show()
                }
                .setNegativeButton("Отменить") { _, _ -> }
                .create()
                .show()
    }

    private fun deleteWord(selectedWord: Word) {
        DB.wordDao().delete(selectedWord)

        reloadListView()

        toast("Слово удалено").show()
    }

    private fun reloadListView() {
        words = DB.wordDao().getByGroupId(currentGroup.id).sortedByDescending { it.id }
        listadapter = WordListAdapter(this, R.layout.word_listview_item, R.layout.meaning_item, words)
        listView.adapter = listadapter
        listadapter.notifyDataSetChanged()
    }

    private fun initGroups() {
        groups = DB.groupDao().getGroupsIdAndName()

        groupsName = mutableListOf()

        var i = 0

        groups.forEach {
            groupsName.add(it.name)

            when (it.id) {
                currentGroup.id -> groupPosition = i
                else -> i++
            }
        }

        adapter = ArrayAdapter(this@GroupActivity, android.R.layout.simple_spinner_item, groupsName)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    }

    private fun showAddNewWordDialog() {
        val layoutInflater = LayoutInflater.from(this@GroupActivity)
        var view = layoutInflater.inflate(R.layout.new_word_dialog, null)
        var text = view.findViewById<EditText>(R.id.text)
        var meaning = view.findViewById<EditText>(R.id.meaning)
        var reading = view.findViewById<EditText>(R.id.reading)
        var status = view.findViewById<RadioGroup>(R.id.status)

        var addFieldBtn = view.findViewById<Button>(R.id.addFieldBtn)
        extraFieldsLayout = view.findViewById<LinearLayout>(R.id.extraFields)

        extraFieldsLayout.removeAllViews()

        //var group = view.findViewById<Spinner>(R.id.group)

        //group.adapter = adapter
        //group.setSelection(groupPosition)

        status.check(R.id.statusNew)

        addFieldBtn.setOnClickListener({
            LayoutInflater.from(this@GroupActivity).inflate(R.layout.extra_field, extraFieldsLayout, true)
        })

        val dialog = AlertDialog.Builder(this@GroupActivity)
                .setView(view)
                .setPositiveButton("Добавить", null)
                .setNegativeButton("Отменить", { d, _ -> d.cancel() })
                .create()

        dialog.setOnShowListener { d ->
            val button = (d as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            button.setOnClickListener { _ ->
                var ok = true

                if (text.text.toString().isNullOrBlank()) {
                    Toasty.error(this@GroupActivity, "Нельзя добавить слово без текста", Toast.LENGTH_SHORT).show()
                    ok = false
                }

                if (meaning.text.toString().isNullOrBlank()) {
                    Toasty.error(this@GroupActivity, "Нельзя добавить слово без значения", Toast.LENGTH_SHORT).show()
                    ok = false
                }

                if (ok) {

                    var insertedWordId: Long

                    var newWord = Word(
                            id = 0,
                            groupId = currentGroup.id,//groups.get(group.selectedItemPosition).id,
                            text = text.text.toString(),
                            meaning = meaning.text.toString(),
                            reading = reading.text.toString(),
                            status = when (status.checkedRadioButtonId) {
                                R.id.statusNew -> 0
                                else -> 1
                            }
                    )

                    if (extraFieldsLayout.childCount != 0) {
                        var fields = ArrayList<WordField>()

                        for (i in 0 until extraFieldsLayout.childCount) {
                            var value = extraFieldsLayout.getChildAt(i).findViewById<EditText>(R.id.extraFieldValue).text.toString()

                            if (!value.isBlank()) {
                                fields.add(
                                        WordField(
                                                when (extraFieldsLayout.getChildAt(i).findViewById<Spinner>(R.id.extraFieldKey).selectedItemPosition) {
                                                    0 -> "Значение"
                                                    1 -> "Чтение"
                                                    2 -> "Написание"
                                                    3 -> "Заметка"
                                                    else -> "Другое"
                                                },
                                                value
                                        )
                                )
                            }
                        }
                        newWord.fields = Gson().toJson(fields)
                    }

                    insertedWordId = DB.wordDao().insert(newWord)

                    dialog.cancel()

                    reloadListView()

                    longSnackbar(findViewById<CoordinatorLayout>(R.id.activityGroup), "Слово \"${text.text}\" добавленно", "Отменить") {
                        DB.wordDao().delete(DB.wordDao().getById(insertedWordId))
                        reloadListView()
                    }
                }
            }
        }

        dialog.show()
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        reloadListView(
                when (sortSpinner.selectedItemPosition) {
                    1 -> getListForSort().sortedBy { it.text }
                    2 -> getListForSort().sortedByDescending { it.text }
                    else -> getListForSort().sortedByDescending { it.id }
                }
        )

        autoCompleteGroupTextView.requestFocus()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        reloadListView(
                when (position) {
                    1 -> getListForSort().sortedBy { it.text }
                    2 -> getListForSort().sortedByDescending { it.text }
                    3 -> getListForSort().filter { it.status == 1 }.sortedByDescending { it.id }
                    4 -> getListForSort().filter { it.status == 0 }.sortedByDescending { it.id }
                    else -> getListForSort().sortedByDescending { it.id }
                }
        )


    }

    private fun reloadListView(list: List<Word>) {
        listadapter = WordListAdapter(this, R.layout.word_listview_item, R.layout.meaning_item, list)
        listView.adapter = listadapter
        listadapter.notifyDataSetChanged()
    }

    private fun getListForSort(): List<Word> {
        return if (isFilterInput())
            words.filter { it.text.toLowerCase().contains(autoCompleteGroupTextView.text.toString().toLowerCase()) }
        else words
    }

    private fun isFilterInput(): Boolean = !autoCompleteGroupTextView.text.isNullOrBlank()

    private fun openCardsActivity() {
        val intent = Intent(this@GroupActivity, GroupWordsCardsActivity::class.java)
        val bundle = Bundle()

        bundle.putLong("groupId", currentGroup.id)
        bundle.putLong("wordId", -1)
        intent.putExtras(bundle)

        startActivity(intent)
    }

    private fun openTestSettingActivity() {
        val intent = Intent(this@GroupActivity, TestSettingActivity::class.java)
        val bundle = Bundle()

        bundle.putLong("groupId", currentGroup.id)
        intent.putExtras(bundle)

        startActivity(intent)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
        super.onBackPressed()
    }
}