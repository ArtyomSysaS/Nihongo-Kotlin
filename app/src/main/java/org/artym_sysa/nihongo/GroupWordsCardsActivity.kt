package org.artym_sysa.nihongo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import org.artym_sysa.nihongo.room.AppDatabase

class GroupWordsCardsActivity : AppCompatActivity() {
    lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_words_cards)

        mRecyclerView = findViewById(R.id.card_view)

        var DB = AppDatabase.getInstance(this@GroupWordsCardsActivity)
        mRecyclerView.adapter = CardsRecycleViewAdapter(
                this,
                if ((intent.extras["wordId"] as Long) == (-1).toLong())
                    DB.wordDao().getByGroupId(intent.extras["groupId"] as Long)
                else
                    listOf(DB.wordDao().getById(intent.extras["wordId"] as Long))
        )

        mRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView.setHasFixedSize(true)

        PagerSnapHelper().attachToRecyclerView(mRecyclerView)
    }

    override fun onBackPressed() {
        back()
    }

    override fun onDestroy() {
        back()
        super.onDestroy()
    }

    private fun back() {
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
    }


}
