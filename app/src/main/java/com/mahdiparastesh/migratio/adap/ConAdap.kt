package com.mahdiparastesh.migratio.adap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.mahdiparastesh.migratio.Fun
import com.mahdiparastesh.migratio.Fun.Companion.td1Dur
import com.mahdiparastesh.migratio.Fun.Companion.textFont
import com.mahdiparastesh.migratio.R
import com.mahdiparastesh.migratio.Select
import com.mahdiparastesh.migratio.Select.Companion.conCheck
import com.mahdiparastesh.migratio.data.Country
import com.mahdiparastesh.migratio.data.Works

class ConAdap(val c: Context, val list: List<Country>) :
    RecyclerView.Adapter<ConAdap.MyViewHolder>() {

    class MyViewHolder(val v: LinearLayout) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_con, parent, false) as LinearLayout
        val clickable = v[clickablePos] as ConstraintLayout
        val main = clickable[mainPos] as LinearLayout
        val tvName = main[tvNamePos] as TextView
        val tvCont = main[tvContPos] as TextView

        // Fonts
        tvName.setTypeface(textFont, Typeface.BOLD)
        tvCont.setTypeface(textFont, Typeface.NORMAL)

        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        val clickable = h.v[clickablePos] as ConstraintLayout
        val main = clickable[mainPos] as LinearLayout
        val tvName = main[tvNamePos] as TextView
        val tvCont = main[tvContPos] as TextView
        val check = clickable[checkPos]

        // Texts
        tvName.text = "${i + 1}. ${Fun.countryNames()[list[i].id.toInt()]}"
        tvCont.text =
            c.resources.getString(com.mahdiparastesh.migratio.data.Continents.values()[list[i].continent].label)

        // Clicks
        (check.background as TransitionDrawable).apply {
            resetTransition()
            if (conCheck[i]) startTransition(td1Dur)
        }
        clickable.setOnClickListener {
            if (conCheck.size <= h.layoutPosition) return@setOnClickListener
            (check.background as TransitionDrawable).apply {
                conCheck[h.layoutPosition] = !conCheck[h.layoutPosition]
                if (conCheck[h.layoutPosition]) startTransition(td1Dur)
                else reverseTransition(td1Dur)
            }
            Select.handler?.obtainMessage(Works.SAVE_MY_COUNTRIES.ordinal, null)?.sendToTarget()
        }
    }

    override fun getItemCount() = list.size


    companion object {
        const val clickablePos = 1
        const val mainPos = 0
        const val tvNamePos = 0
        const val tvContPos = 1
        const val checkPos = 1
    }
}
