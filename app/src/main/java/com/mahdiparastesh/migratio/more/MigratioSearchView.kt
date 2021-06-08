package com.mahdiparastesh.migratio.more

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.mahdiparastesh.migratio.Fun
import com.mahdiparastesh.migratio.Panel
import com.mahdiparastesh.migratio.R

class MigratioSearchView(
    val c: Context = ContextThemeWrapper(Fun.c, R.style.SearchViewTheme)
) : SearchView(c) {
    init {
        for (view in Fun.findChildrenByClass(this, TextView::class.java)) {
            val tv = (view as TextView)
            tv.setTextColor(ContextCompat.getColor(c, R.color.migratioSearchView1))
            if (tv is EditText) tv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(c: CharSequence?, strt: Int, con: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    Panel.search(s.toString())
                }
            })
        }
    }
}
