package org.ifaco.migratio

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import org.ifaco.migratio.Fun.Companion.c
import org.ifaco.migratio.Fun.Companion.dirLtr
import org.ifaco.migratio.Fun.Companion.sp
import org.ifaco.migratio.Fun.Companion.textFont
import org.ifaco.migratio.Fun.Companion.titleFont
import org.ifaco.migratio.Panel.Companion.exCensor
import org.ifaco.migratio.adap.ConAdap
import org.ifaco.migratio.adap.CriAdap
import org.ifaco.migratio.data.*
import org.ifaco.migratio.dirchooser.DirectoryChooserConfig
import org.ifaco.migratio.dirchooser.DirectoryChooserFragment
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class Select : AppCompatActivity(), DirectoryChooserFragment.OnFragmentInteractionListener {
    lateinit var body: ConstraintLayout
    lateinit var toolbar: Toolbar
    lateinit var sNav: ConstraintLayout
    lateinit var sNav1: ConstraintLayout
    lateinit var sNav1TV: TextView
    lateinit var sNav2: ConstraintLayout
    lateinit var sNav2TV: TextView
    lateinit var sNavHL: View
    lateinit var tbShadow: View
    lateinit var main: ConstraintLayout
    lateinit var sSwitcher: ViewSwitcher
    lateinit var rvCountries: RecyclerView
    lateinit var rvCriteria: RecyclerView

    lateinit var dirChos: DirectoryChooserFragment

    var conAdap: ConAdap? = null
    var criAdap: CriAdap? = null
    var countries: MutableList<Country>? = null
    var allCriteria: MutableList<Criterion>? = null
    var criteria: MutableList<Criterion>? = null
    var switchedTo2nd = false
    var doSave = true
    var showingHelp = false
    val permExport = 111
    val reqImport = 112
    val permImport = 113
    var toBeImported: Uri? = null

    companion object {
        const val exMyCountries = "myCountries"
        const val exSwitchedTo2nd = "switchedTo2nd"
        const val exLastExportPath = "lastExportPath"
        var handler: Handler? = null
        val conCheck = ArrayList<Boolean>()
        var myCountries: MutableSet<String>? = null
        var myCriteria: ArrayList<MyCriterion>? = null
        var criOFOpened: ArrayList<Boolean>? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select)

        body = findViewById(R.id.body)
        toolbar = findViewById(R.id.toolbar)
        sNav = findViewById(R.id.sNav)
        sNav1 = findViewById(R.id.sNav1)
        sNav1TV = sNav1[0] as TextView
        sNav2 = findViewById(R.id.sNav2)
        sNav2TV = sNav2[0] as TextView
        sNavHL = findViewById(R.id.sNavHL)
        tbShadow = findViewById(R.id.tbShadow)
        main = findViewById(R.id.main)
        sSwitcher = findViewById(R.id.sSwitcher)
        rvCountries = findViewById(R.id.rvCountries)
        rvCriteria = findViewById(R.id.rvCriteria)

        Fun.init(this, body)
        switchedTo2nd = sp.getBoolean(exSwitchedTo2nd, false)
        //Panel.rvScrollY = 0


        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Works.GET_ALL.ordinal -> when (msg.arg1) {
                        Types.MY_CRITERION.ordinal -> {
                            myCriteria = msg.obj as ArrayList<MyCriterion>
                            if (myCriteria != null) arrangeCriteria()
                            //////////////////////////////////////////////else
                        }
                    }

                    Works.SAVE_MY_COUNTRIES.ordinal -> sp.edit().apply {
                        if (countries == null) return@apply
                        var ss = mutableSetOf<String>()
                        for (i in conCheck.indices)
                            if (conCheck[i] && countries!!.size > i) ss.add(countries!![i].tag)
                        putStringSet(exMyCountries, ss)
                        apply()
                    }

                    Works.INSERT_ALL.ordinal -> when (msg.arg1) {
                        Works.NONE.ordinal -> {
                        }
                        Works.EXIT_ON_SAVED.ordinal -> cut()
                        Works.NOTIFY_ON_SAVED.ordinal -> when (msg.arg2) {
                            Types.MY_CRITERION.ordinal -> criAdap?.notifyDataSetChanged()
                        }
                    }

                    Works.BREAK_CENSOR.ordinal -> sp.edit()
                        .putBoolean(exCensor, !sp.getBoolean(exCensor, true)).apply()

                    Works.CLEAR_AND_INSERT_ALL.ordinal -> when (msg.arg1) {
                        Types.MY_CRITERION.ordinal -> when (msg.arg2) {
                            Works.NONE.ordinal -> {
                                myCriteria =
                                    (msg.obj as List<MyCriterion>).toCollection(ArrayList())
                                criAdap?.notifyDataSetChanged()
                            }
                            Works.IMPORT.ordinal -> cut()
                        }
                    }
                }
            }
        }

        // Receive Data
        myCountries = sp.getStringSet(exMyCountries, null)
        myCriteria = null
        if (intent.extras != null) {
            if (intent.extras!!.containsKey("countries") && intent.extras!!.containsKey("criteria")) {
                countries =
                    (intent.extras!!.getParcelableArray("countries") as Array<Parcelable>).toList() as MutableList<Country>
                if (countries != null)
                    Collections.sort(countries!!, Country.Companion.SortCon(1))
                arrangeCountries()
                criteria =
                    (intent.extras!!.getParcelableArray("criteria") as Array<Parcelable>).toList() as MutableList<Criterion>
                allCriteria = criteria
                if (criteria != null)
                    Collections.sort(criteria!!, Criterion.Companion.SortCri(1))
                val toBeCensored = ArrayList<Int>()
                if (criteria != null) for (cri in criteria!!.indices)
                    if (criteria!![cri].censor > 0 && sp.getBoolean(exCensor, true))
                        toBeCensored.add(cri)
                for (ce in toBeCensored) criteria!!.removeAt(ce)
                if (myCriteria != null) arrangeCriteria()
                else Work(
                    c, handler, Works.GET_ALL, Types.MY_CRITERION,
                    listOf(Types.MY_CRITERION.ordinal)
                ).start()
            } else onBackPressed()
        } else onBackPressed()

        // Loading
        Fun.handleTB(this, toolbar, titleFont)

        // Navigation
        nav()
        if (switchedTo2nd) switchedTo2nd = Fun.switcher(c, sSwitcher, dirLtr, false)
        sNav1.setOnClickListener {
            if (switchedTo2nd) switchedTo2nd = Fun.switcher(c, sSwitcher, dirLtr); nav()
        }
        sNav2.setOnClickListener {
            if (!switchedTo2nd) switchedTo2nd = Fun.switcher(c, sSwitcher, dirLtr); nav()
        }
        sNav1TV.setTypeface(textFont, Typeface.BOLD)
        sNav2TV.setTypeface(textFont, Typeface.BOLD)

        // Export
        dirChos = DirectoryChooserFragment.newInstance(
            DirectoryChooserConfig.builder()
                .allowNewDirectoryNameModification(true)!!
                .newDirectoryName(resources.getString(R.string.dirChosNew))!!
                .build()!!
        )

        restoration(savedInstanceState)
    }

    override fun onSaveInstanceState(state: Bundle) {
        state.putBoolean("switchedTo2nd", switchedTo2nd)
        state.putInt("rvConY", rvCountries.scrollY)
        state.putInt("rvCriY", rvCriteria.scrollY)
        if (criOFOpened != null)
            state.putBooleanArray("criOFOpened", criOFOpened!!.toBooleanArray())
        state.putBoolean("showingHelp", showingHelp)
        super.onSaveInstanceState(state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoration(savedInstanceState)
    }

    override fun onBackPressed() {
        if (!saveFocused() || !doSave) super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.smSelectAll -> {
            selectAll(true); true
        }
        R.id.smDeselectAll -> {
            selectAll(false); true
        }
        R.id.smExport -> {
            if (myCountries != null && myCriteria != null && toBeImported == null) {
                val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
                    ContextCompat.checkSelfPermission(c, perm) !=
                    PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 23
                ) ActivityCompat.requestPermissions(this, arrayOf(perm), permExport)
                else selectDirForExport()
            }
            true
        }
        R.id.smImport -> {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
            }, reqImport)
            true
        }
        R.id.smResetAll -> {
            Fun.alertDialogue2(this, R.string.smResetAll, R.string.sureResetAll,
                DialogInterface.OnClickListener { _, _ ->
                    if (allCriteria == null) return@OnClickListener
                    Fun.defaultMyCriteria(allCriteria!!, handler)
                })
            true
        }
        R.id.smHelp -> {
            help(); true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        val b = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        when (requestCode) {
            permExport -> if (b) selectDirForExport()
            permImport -> if (b) import()
        }
    }

    override fun onSelectDirectory(path: String) {
        dirChos.dismiss()
        val b = Exporter.export(path)
        Toast.makeText(
            c, if (b) R.string.exportDone else R.string.exportUndone, Toast.LENGTH_LONG
        ).show()
        sp.edit().apply {
            putString(exLastExportPath, path)
            apply()
        }
    }

    override fun onCancelChooser() {
        dirChos.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            reqImport -> if (resultCode == RESULT_OK) {
                toBeImported = data!!.data
                val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
                    ContextCompat.checkSelfPermission(c, perm) !=
                    PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 23
                ) ActivityCompat.requestPermissions(this, arrayOf(perm), permImport)
                else import()
            }
        }
    }


    fun restoration(state: Bundle?) {
        if (state == null) return
        if (state.containsKey("switchedTo2nd")) {
            switchedTo2nd = state.getBoolean("switchedTo2nd"); nav(); }
        if (state.containsKey("rvConY")) rvCountries.scrollTo(0, state.getInt("rvConY", 0))
        if (state.containsKey("rvCriY")) rvCriteria.scrollTo(0, state.getInt("rvCriY", 0))
        if (state.containsKey("criOFOpened"))
            criOFOpened = state.getBooleanArray("criOFOpened")?.toCollection(ArrayList())
        if (state.getBoolean("showingHelp", false)) help()
    }

    fun arrangeCountries() {
        if (countries == null) return
        conCheck.clear()
        for (i in countries!!.indices)
            conCheck.add(if (myCountries != null) myCountries!!.contains(countries!![i].tag) else false)
        conAdap = ConAdap(c, countries!!)
        rvCountries.adapter = conAdap
    }

    fun arrangeCriteria() {
        Collections.sort(criteria!!, Criterion.Companion.SortCri())
        if (criOFOpened == null) {
            criOFOpened = ArrayList()
            for (i in criteria!!) criOFOpened!!.add(false)
        }
        criAdap = CriAdap(c, criteria!!, this)
        rvCriteria.adapter = criAdap
    }

    fun nav(dur: Long = resources.getInteger(R.integer.anim_lists_dur).toLong()) {
        var cs = ConstraintSet()// EACH OF ITS CHILDREN MUST HAVE AN ID IN BOTH LAYOUTS
        cs.clone(sNav)
        TransitionManager.beginDelayedTransition(sNav, AutoTransition().setDuration(dur))
        val att = if (switchedTo2nd) sNav2.id else sNav1.id
        cs.connect(sNavHL.id, ConstraintSet.START, att, ConstraintSet.START)
        cs.connect(sNavHL.id, ConstraintSet.END, att, ConstraintSet.END)
        cs.applyTo(sNav)
    }

    fun saveFocused(): Boolean {
        var isFocused = false
        if (criteria == null) return isFocused
        for (f in 0 until rvCriteria.childCount) {
            var i = rvCriteria[f] as ViewGroup
            var overflow = (i[CriAdap.clickablePos] as ViewGroup)[CriAdap.overflowPos] as ViewGroup
            var et = (overflow[CriAdap.ofo2Pos] as ViewGroup)[CriAdap.ofoETPos] as EditText
            if (et.hasFocus()) {
                val cri = criteria!![rvCriteria.getChildLayoutPosition(i)]
                doSave = false
                CriAdap.saveMyC(
                    c, CriAdap.findMyC(cri.tag).apply { good = CriAdap.good(1, et, cri.medi) }, true
                )
                isFocused = true
            }
        }
        return isFocused
    }

    fun selectAll(b: Boolean = true) {
        if (!switchedTo2nd) {
            for (con in conCheck.indices) conCheck[con] = b
            handler?.obtainMessage(Works.SAVE_MY_COUNTRIES.ordinal, null)?.sendToTarget()
            conAdap?.notifyDataSetChanged()
        } else if (myCriteria != null) {
            for (i in myCriteria!!.indices)
                if (shouldBeAdded(myCriteria!![i])) myCriteria!![i].isOn = b
            Work(
                c, handler, Works.INSERT_ALL, Types.MY_CRITERION,
                listOf(myCriteria, Works.NOTIFY_ON_SAVED.ordinal, Types.MY_CRITERION.ordinal)
            ).start()
        }
    }

    fun shouldBeAdded(mycri: MyCriterion): Boolean =
        Computation.findCriByTag(mycri.tag, criteria!!.toList()) != null

    fun selectDirForExport() {
        dirChos.show(supportFragmentManager, null)
    }

    fun import() {
        if (toBeImported == null) return
        Exporter.import(toBeImported!!)?.let {
            sp.edit().apply {
                putStringSet(exMyCountries, it.MYCON)
                apply()
            }
            Work(
                c, handler, Works.CLEAR_AND_INSERT_ALL, Types.MY_CRITERION,
                listOf(it.MYCRI, Types.MY_CRITERION.ordinal, Works.IMPORT.ordinal)
            ).start()
        }
        toBeImported = null
    }

    fun help() {
        if (showingHelp) return
        showingHelp = true
        Fun.alertDialogue1(
            this, R.string.pmHelp, R.string.pHelp, textFont,
            { _, _ -> showingHelp = false }, { showingHelp = false }, true
        )
    }

    fun cut() {
        try {
            onBackPressed()
            finish()
        } catch (ignored: NullPointerException) {
        }
    }
}
