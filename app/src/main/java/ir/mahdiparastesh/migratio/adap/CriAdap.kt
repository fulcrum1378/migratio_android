package ir.mahdiparastesh.migratio.adap

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import ir.mahdiparastesh.migratio.Fun
import ir.mahdiparastesh.migratio.Fun.Companion.dp
import ir.mahdiparastesh.migratio.Fun.Companion.td1Dur
import ir.mahdiparastesh.migratio.Fun.Companion.textFont
import ir.mahdiparastesh.migratio.Fun.Companion.vis
import ir.mahdiparastesh.migratio.R
import ir.mahdiparastesh.migratio.Select.Companion.criOFOpened
import ir.mahdiparastesh.migratio.Select.Companion.handler
import ir.mahdiparastesh.migratio.Select.Companion.myCriteria
import ir.mahdiparastesh.migratio.data.*

class CriAdap(val c: Context, val list: List<Criterion>, val that: AppCompatActivity) :
    RecyclerView.Adapter<CriAdap.MyViewHolder>() {
    var scrolling = false
    val overflowHeight = dp(250)

    class MyViewHolder(val v: LinearLayout) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cri, parent, false) as LinearLayout
        val clickable = v[clickablePos] as ConstraintLayout
        val tvName = clickable[tvNamePos] as TextView
        val switch = clickable[switchPos] as SwitchMaterial
        val overflow = clickable[overflowPos] as LinearLayout
        val ofo1 = overflow[ofo1Pos] as ConstraintLayout
        //val ofo1Sw = ofo1[ofoSwPos]
        val ofo1TV = ofo1[ofoTVPos] as TextView
        val ofo2 = overflow[ofo2Pos] as ConstraintLayout
        //val ofo2Sw = ofo2[ofoSwPos]
        val ofo2TV = ofo2[ofoTVPos] as TextView
        val ofo2ET = ofo2[ofoETPos] as EditText
        val ofo3 = overflow[ofo3Pos] as ConstraintLayout
        //val ofo3Sw = ofo3[ofoSwPos]
        val ofo3TV = ofo3[ofoTVPos] as TextView
        val ofo4 = overflow[ofo4Pos] as ConstraintLayout
        val ofo4SkInfo = ofo4[ofoSkInfoPos] as TextView
        //val ofo4Sk = ofo4[ofoSkPos] as SeekBar
        val swissTV = listOf(ofo1TV, ofo2TV, ofo3TV)

        // Constraints
        tvName.id = View.generateViewId()
        (switch.layoutParams as ConstraintLayout.LayoutParams).apply {
            topToTop = tvName.id
            topToBottom = tvName.id
            switch.layoutParams = this
        }
        (overflow.layoutParams as ConstraintLayout.LayoutParams).apply {
            topToBottom = tvName.id
            overflow.layoutParams = this// NECESSARY
        }

        // Fonts
        tvName.setTypeface(textFont, Typeface.BOLD)
        for (tv in swissTV) tv.setTypeface(textFont, Typeface.BOLD)
        ofo2ET.setTypeface(textFont, Typeface.BOLD)
        ofo4SkInfo.setTypeface(textFont, Typeface.NORMAL)

        return MyViewHolder(v)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        val clickable = h.v[clickablePos] as ConstraintLayout
        val tvName = clickable[tvNamePos] as TextView
        val switch = clickable[switchPos] as SwitchMaterial
        val overflow = clickable[overflowPos] as LinearLayout
        val ofo1 = overflow[ofo1Pos] as ConstraintLayout
        val ofo1Sw = ofo1[ofoSwPos]
        //val ofo1TV = ofo1[ofoTVPos] as TextView
        val ofo2 = overflow[ofo2Pos] as ConstraintLayout
        val ofo2Sw = ofo2[ofoSwPos]
        //val ofo2TV = ofo2[ofoTVPos] as TextView
        val ofo2ET = ofo2[ofoETPos] as EditText
        val ofo3 = overflow[ofo3Pos] as ConstraintLayout
        val ofo3Sw = ofo3[ofoSwPos]
        //val ofo3TV = ofo3[ofoTVPos] as TextView
        val ofo4 = overflow[ofo4Pos] as ConstraintLayout
        val ofo4SkInfo = ofo4[ofoSkInfoPos] as TextView
        val ofo4Sk = ofo4[ofoSkPos] as SeekBar
        val swiss = listOf(ofo1Sw, ofo2Sw, ofo3Sw, ofo2ET)

        // Texts
        tvName.text = list[i].parseName()
        ofo2ET.hint = list[i].medi

        // Settings
        val myc = findMyC(list[i].tag)
        switch.isChecked = myc.isOn
        resetCheck(ofo1Sw); resetCheck(ofo2Sw); resetCheck(ofo3Sw)
        ofo2ET.alpha = etAlpha
        ofo2ET.isEnabled = false
        when (myc.good) {
            "+" -> defCheck(ofo1Sw)
            "-" -> defCheck(ofo3Sw)
            else -> {
                defCheck(ofo2Sw)
                ofo2ET.setText(myc.good)
                ofo2ET.alpha = 1f
                ofo2ET.isEnabled = true
            }
        }
        vis(overflow, false)
        (overflow.layoutParams as ConstraintLayout.LayoutParams).apply {
            height = 0; overflow.layoutParams = this
        }
        if (criOFOpened != null && criOFOpened!!.size > i) if (criOFOpened!![i]) {
            vis(overflow)
            (overflow.layoutParams as ConstraintLayout.LayoutParams).apply {
                height = overflowHeight; overflow.layoutParams = this
            }
        }
        if (switch.isChecked) overflow.alpha = if (myc.isOn) 1f else ofAlpha
        ofo2ET.isEnabled = myc.isOn
        ofo4Sk.isEnabled = myc.isOn
        ofo4Sk.progress = myc.importance
        importanceInfo(c, ofo4SkInfo, myc.importance)

        // Clicks
        switch.setOnCheckedChangeListener { _, b ->
            saveMyC(c, findMyC(list[h.layoutPosition].tag).apply { isOn = b })
            overflow.alpha = if (b) 1f else ofAlpha
            ofo2ET.isEnabled = b
            ofo4Sk.isEnabled = b
        }
        tvName.setOnClickListener {
            if (scrolling) return@setOnClickListener
            scrolling = true
            criOFOpened!![h.layoutPosition] = !criOFOpened!![h.layoutPosition]
            val goDown = criOFOpened!![h.layoutPosition]

            ValueAnimator.ofInt(
                if (goDown) 0 else overflowHeight, if (goDown) overflowHeight else 0
            ).apply {
                duration = 148
                addUpdateListener {
                    (overflow.layoutParams as ConstraintLayout.LayoutParams).apply {
                        height = it.animatedValue as Int
                        overflow.layoutParams = this
                    }
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        if (goDown) vis(overflow)
                        val maxAlpha = if (findMyC(list[i].tag).isOn) 1f else ofAlpha
                        ObjectAnimator.ofFloat(overflow, "alpha", if (goDown) maxAlpha else 0f)
                            .apply { duration = 18; start(); }
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        scrolling = false
                        if (!goDown) vis(overflow, false)
                    }
                })
                start()
            }
        }
        tvName.setOnLongClickListener {
            PopupMenu(c, it).apply {
                setOnMenuItemClickListener { it1 ->
                    return@setOnMenuItemClickListener when (it1.itemId) {
                        R.id.clcSource -> {
                            Fun.alertDialogue3(
                                that, R.string.clcSource, list[h.layoutPosition].reference,
                                copyable = true, linkify = true
                            )
                            true
                        }
                        else -> false
                    }
                }
                inflate(R.menu.cri_long_click)
                show()
            }
            true
        }
        ofo1.setOnClickListener {
            if (!switch.isChecked) return@setOnClickListener
            val cri = list[h.layoutPosition]
            saveMyC(c, findMyC(cri.tag).apply { good = radio(swiss, 0, cri.medi) })
        }
        ofo2.setOnClickListener {
            if (!switch.isChecked) return@setOnClickListener
            val cri = list[h.layoutPosition]
            saveMyC(c, findMyC(cri.tag).apply { good = radio(swiss, 1, cri.medi) })
        }
        ofo3.setOnClickListener {
            if (!switch.isChecked) return@setOnClickListener
            val cri = list[h.layoutPosition]
            saveMyC(c, findMyC(cri.tag).apply { good = radio(swiss, 2, cri.medi) })
        }
        ofo2ET.setOnFocusChangeListener { view, b ->
            if (view == null || b) return@setOnFocusChangeListener
            val cri = list[h.layoutPosition]
            saveMyC(c, findMyC(cri.tag).apply { good = good(1, ofo2ET, cri.medi) })
        }
        ofo4Sk.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (p0 == null) return
                saveMyC(c, findMyC(list[h.layoutPosition].tag).apply { importance = p0.progress })
            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                importanceInfo(c, ofo4SkInfo, p1)
            }
        })
    }

    override fun getItemCount() = list.size


    companion object {
        const val clickablePos = 1
        const val tvNamePos = 0
        const val switchPos = 1
        const val overflowPos = 2
        const val ofo1Pos = 0
        const val ofo2Pos = 1
        const val ofo3Pos = 2
        const val ofo4Pos = 3
        const val ofoSwPos = 0
        const val ofoTVPos = 1
        const val ofoETPos = 2
        const val ofoSkInfoPos = 0
        const val ofoSkPos = 1

        const val ofAlpha = 0.68f
        const val etAlpha = 0.48f

        fun radio(swiss: List<View>, i: Int, med: String): String {
            (swiss[0].background as TransitionDrawable).apply {
                resetTransition(); if (i == 0) startTransition(td1Dur)
            }
            (swiss[1].background as TransitionDrawable).apply {
                resetTransition(); if (i == 1) startTransition(td1Dur)
            }
            (swiss[2].background as TransitionDrawable).apply {
                resetTransition(); if (i == 2) startTransition(td1Dur)
            }
            val ofoET = swiss[3] as EditText
            ofoET.alpha = if (i == 1) 1f else etAlpha
            ofoET.isEnabled = i == 1
            return good(i, ofoET, med)
        }

        fun good(i: Int, et: EditText, med: String): String {
            var value = et.text.toString()
            if (value == "") {
                et.setText(med); value = med; }
            return when (i) {
                0 -> "+"
                1 -> value
                2 -> "-"
                else -> ""
            }
        }

        fun findMyC(tag: String): MyCriterion {
            var pos = -1
            for (i in myCriteria!!.indices) if (myCriteria!![i].tag == tag) pos = i
            return myCriteria!![pos]
        }

        fun findMyCPos(myc: MyCriterion): Int {
            var pos = -1
            for (i in myCriteria!!.indices) if (myCriteria!![i].tag == myc.tag) pos = i
            return pos
        }

        fun saveMyC(c: Context, myc: MyCriterion, exitOnSaved: Boolean = false) {
            if (myCriteria != null && myCriteria!!.size > findMyCPos(myc))
                myCriteria!![findMyCPos(myc)] = myc
            val purp = if (exitOnSaved) Works.EXIT_ON_SAVED else Works.NONE
            Work(
                c, handler, Works.INSERT_ALL, Types.MY_CRITERION,
                listOf(listOf(myc), purp.ordinal)
            ).start()
        }

        fun resetCheck(switch: View) {
            (switch.background as TransitionDrawable).resetTransition()
        }

        fun defCheck(switch: View) {
            (switch.background as TransitionDrawable).startTransition(td1Dur)
        }

        @SuppressLint("SetTextI18n")
        fun importanceInfo(c: Context, tv: TextView, importance: Int) {
            tv.text = "${c.resources.getString(R.string.criImportance)} $importance%"
        }
    }
}
