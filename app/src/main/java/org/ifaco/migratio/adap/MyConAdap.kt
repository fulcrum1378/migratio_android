package org.ifaco.migratio.adap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.blure.complexview.ComplexView
import org.ifaco.migratio.Computation
import org.ifaco.migratio.Fun
import org.ifaco.migratio.Fun.Companion.textFont
import org.ifaco.migratio.R
import org.ifaco.migratio.data.Country
import kotlin.math.round

class MyConAdap(val c: Context, val list: ArrayList<Computation>, val cons: List<Country>) :
    RecyclerView.Adapter<MyConAdap.MyViewHolder>() {

    class MyViewHolder(val v: ComplexView) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_con, parent, false) as ComplexView
        val cl = v[clPos] as ConstraintLayout
        val tvName = cl[tvNamePos] as TextView
        val tvScore = cl[tvScorePos] as TextView

        // Fonts
        tvName.setTypeface(textFont, Typeface.BOLD)
        tvScore.setTypeface(textFont, Typeface.BOLD)

        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        val cl = h.v[clPos] as ConstraintLayout
        val tvName = cl[tvNamePos] as TextView
        val tvScore = cl[tvScorePos] as TextView

        // Texts
        tvName.text = "${i + 1}. ${Fun.countryNames()[Computation.findConById(list[i].id, cons)!!.id.toInt()]}"
        tvScore.text = "${round(list[i].score).toInt()}%"//DecimalFormat("#").format(list[i].score)

        // Clicks
        cl.setOnClickListener {
            Toast.makeText(c, "${list[h.layoutPosition].score}%", Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount() = list.size


    companion object {
        const val clPos = 0
        const val tvNamePos = 0
        const val tvScorePos = 1
    }
}