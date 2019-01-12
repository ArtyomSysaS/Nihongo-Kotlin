package org.artym_sysa.nihongo

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import org.artym_sysa.nihongo.room.AppDatabase
import org.artym_sysa.nihongo.room.entity.Group
import org.artym_sysa.nihongo.room.entity.GroupPojo
import org.artym_sysa.nihongo.room.entity.Word
import org.jetbrains.anko.design.longSnackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), TextWatcher, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

    }

    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
        when (scrollState) {
            AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> fab.show()
            else -> fab.hide()
        }
    }

    lateinit var DB: AppDatabase
    lateinit var groups: List<GroupPojo>
    lateinit var adapter: GroupListAdapter
    lateinit var listView: ListView
    lateinit var autoCompleteGroupTextView: AutoCompleteTextView
    lateinit var fab: FloatingActionButton
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.groupsListView)

        val header = LayoutInflater.from(this@MainActivity).inflate(R.layout.filter_listview_header, null)

        autoCompleteGroupTextView = header.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        autoCompleteGroupTextView.addTextChangedListener(this)

        header.findViewById<Spinner>(R.id.sortSpinner).onItemSelectedListener = this
        header.findViewById<Button>(R.id.clearButton).setOnClickListener({
            autoCompleteGroupTextView.text.clear()
            reloadListView()
        })

        listView.addHeaderView(header)
        listView.onItemClickListener = this
        listView.setOnScrollListener(this)

        DB = AppDatabase.getInstance(this@MainActivity)

        //DBCreate()
        reloadListView()

        registerForContextMenu(listView)

        fab = findViewById<FloatingActionButton>(R.id.gFab)
        fab.setOnClickListener({ showGroupDialog() })
        toolbar = findViewById(R.id.toolbar)

        toolbar.findViewById<TextView>(R.id.activityTitle).text = "Группы"
        setSupportActionBar(toolbar)
    }

    override fun onDestroy() {
        DB.close()
        finish()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        reloadListView()
    }

    //Sort Spinner Method
    override fun onNothingSelected(parent: AdapterView<*>?) {}

    //Sort Spinner Method
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        reloadListView(
                when (position) {
                    1 -> getListForSort().sortedBy { it.name }
                    2 -> getListForSort().sortedByDescending { it.name }
                    3 -> getListForSort().sortedByDescending { SimpleDateFormat("dd.mm.yyyy").parse(it.date) }
                    4 -> getListForSort().sortedBy { SimpleDateFormat("dd.mm.yyyy").parse(it.date) }
                    5 -> getListForSort().sortedByDescending { it.wordsCount }
                    6 -> getListForSort().sortedBy { it.wordsCount }
                    else -> getListForSort().sortedByDescending { it.id }
                }
        )
    }

    //Filter method
    override fun afterTextChanged(s: Editable?) {}

    //Filter method
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    //Filter method
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        adapter.data = DB.groupDao().getAll()
        adapter.filter.filter(s)
    }

    //ListView item ContextMenu method
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        if (v?.id == R.id.groupsListView) {
            val menuItems = resources.getStringArray(R.array.context_menu)

            menuItems.indices.forEach { menu?.add(Menu.NONE, it, it, menuItems[it]) }
        }
    }

    //ListView item ContextMenu method
    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            0 -> openGroupActivity(item)
            1 -> showRenameGroupDialog(item)
            2 -> showOnDeleteContextMenuItemClicked(item)
        }
        return super.onContextItemSelected(item)
    }

    private fun openGroupActivity(item: MenuItem) {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo

        openActivity(info.position)
    }

    //Dialog
    private fun showOnDeleteContextMenuItemClicked(item: MenuItem) {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedGroupPosition = info.position - 1

        val selectedGroup = DB.groupDao().getById(adapter.getItem(selectedGroupPosition)?.id)

        AlertDialog.Builder(this@MainActivity).setTitle("Подтверждение")
                .setMessage("Вы действительно хотите удалить группу \"${selectedGroup.name}\"?\nВсе данные о этой группе будут удалены и их нельзя будет восстановить")
                .setPositiveButton("Удалить") { _, _ ->

                    Snackbar.make(findViewById<CoordinatorLayout>(R.id.activityMain), " ", Snackbar.LENGTH_LONG)
                            .setAction("Отменить удаление", {})
                            .setDuration(3000)
                            .addCallback(object : Snackbar.Callback() {

                                override fun onDismissed(snackbar: Snackbar?, event: Int) {
                                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                        deleteGroup(selectedGroup)
                                    }

                                    if (event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                                        deleteGroup(selectedGroup)
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

    //Dialog
    private fun showGroupDialog() {
        val layoutInflater = LayoutInflater.from(this@MainActivity)
        val view = layoutInflater.inflate(R.layout.add_group_dialog, null)
        val text = view.findViewById(R.id.newGroupTitle) as EditText

        val dialog = AlertDialog.Builder(this@MainActivity)
                .setView(view)
                .setPositiveButton("Добавить", null)
                .setNegativeButton("Отменить", { d, _ -> d.cancel() })
                .create()

        dialog.setOnShowListener { d ->
            val button = (d as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            button.setOnClickListener { _ ->
                var ok = true
                var exists: Boolean

                val title = text.text.toString()

                if (title.isEmpty()) {
                    ok = false
                    Toasty.error(this@MainActivity, "Нельзя создать группу без названия", Toast.LENGTH_SHORT).show()

                } else {
                    exists = false

                    groups.forEach {
                        if (it.name.equals(title)) {
                            exists = true
                        }
                    }

                    if (exists) {
                        ok = false
                        Toasty.warning(this@MainActivity, "Группа с таким названием уже существует", Toast.LENGTH_SHORT).show()
                    }
                }

                if (ok) {
                    val id = DB.groupDao().insert(Group(name = title, date = SimpleDateFormat("dd.MM.yyyy").format(Date())))

                    reloadListView()
                    dialog.cancel()

                    longSnackbar(findViewById<CoordinatorLayout>(R.id.activityMain), "Группа \"$title\" создана", "Отменить") {
                        DB.groupDao().delete(DB.groupDao().getById(id))
                        reloadListView()
                        Toasty.success(this@MainActivity, "Группа \"$title\" удалена", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

    //Dialog
    private fun showRenameGroupDialog(item: MenuItem?) {
        val info = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedGroupPosition = info.position - 1
        var groupForRename = DB.groupDao().getById(adapter.getItem(selectedGroupPosition)?.id)

        val layoutInflater = LayoutInflater.from(this@MainActivity)
        val view = layoutInflater.inflate(R.layout.add_group_dialog, null)
        var text = view.findViewById(R.id.newGroupTitle) as EditText

        text.setText(groupForRename.name)

        val dialog = AlertDialog.Builder(this@MainActivity)
                .setView(view)
                .setPositiveButton("Изменить", null)
                .setNegativeButton("Отменить", { d, _ -> d.cancel() })
                .create()

        dialog.setOnShowListener { d ->
            val button = (d as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            button.setOnClickListener { _ ->
                var ok = true
                var exists: Boolean

                val title = text.text.toString()

                if (title.isEmpty()) {
                    ok = false
                    Toasty.error(this@MainActivity, "Нельзя изменить название на пустое", Toast.LENGTH_SHORT).show()

                } else {
                    exists = false

                    groups.forEach {
                        if (it.name.equals(title) && it.id != groupForRename.id) {
                            exists = true
                        }
                    }

                    if (exists) {
                        ok = false
                        Toasty.error(this@MainActivity, "Группа с таким названием уже существует", Toast.LENGTH_SHORT).show()
                    }
                }

                if (ok) {
                    var renamedGroup = DB.groupDao().getById(groupForRename.id)
                    renamedGroup.name = title

                    DB.groupDao().update(renamedGroup)

                    dialog.cancel()
                    reloadListView()

                    longSnackbar(findViewById<CoordinatorLayout>(R.id.activityMain), "Группа \"${groupForRename.name}\" переименована на \"$title\"", "Отменить") {
                        renamedGroup.name = groupForRename.name

                        DB.groupDao().update(renamedGroup)
                        reloadListView()
                        Toasty.success(this@MainActivity, "Переименование отмененно", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        openActivity(position)
    }

    private fun openActivity(position: Int) {
        var intent = Intent(this@MainActivity, GroupActivity::class.java)
        var bundle = Bundle()

        bundle.putLong("groupId", adapter.getItem(position - 1)?.id!!)
        intent.putExtras(bundle)

        startActivity(intent)
    }

    private fun getListForSort(): List<GroupPojo> {
        return if (isFilterInput())
            groups.filter { it.name.toLowerCase().contains(autoCompleteGroupTextView.text.toString().toLowerCase()) }
        else groups
    }

    private fun isFilterInput(): Boolean = !autoCompleteGroupTextView.text.isNullOrBlank()

    private fun reloadListView(list: List<GroupPojo>) {
        adapter = GroupListAdapter(this, R.layout.list_view_item, list)

        listView.adapter = adapter

        adapter.notifyDataSetChanged()
    }

    private fun DBCreate() {
        if (DB.groupDao().getAll().size === 0)
            for (i in 1..10) {
                DB.groupDao().insert(Group(name = "Group $i", date = "17.01.2018"))


                for (j in i..20) {
                    var fields = ArrayList<WordField>()

                    for (k in 1..10) {
                        when (k) {
                            in 0..5 -> fields.add(WordField("Значение", "доп. значние #$k"))
                            in 6..8 -> fields.add(WordField("Чтение", "доп. чтение #$k"))
                            in 9..10 -> fields.add(WordField("Другие", "что-то её связанное со словом #$j под номером $k"))
                        }
                    }

                    DB.wordDao().insert(Word(groupId = i.toLong(), text = "Слово $j", meaning = "Значение $j", reading = "Чтение $j", fields = Gson().toJson(fields)))

                }
            }
    }

    private fun deleteGroup(selectedGroup: Group) {
        DB.groupDao().delete(selectedGroup)

        reloadListView()

        Toasty.success(this@MainActivity, "Группа удалена", Toast.LENGTH_SHORT).show()
    }

    private fun reloadListView() {
        groups = DB.groupDao().getAll().sortedByDescending { it.id }
        adapter = GroupListAdapter(this, R.layout.list_view_item, groups)

        listView.adapter = adapter

        if (autoCompleteGroupTextView.text.toString().isNullOrBlank()) {
            adapter.notifyDataSetChanged()
        } else {
            adapter.filter.filter(autoCompleteGroupTextView.text.toString())
        }
    }
}