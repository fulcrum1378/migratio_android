package org.ifaco.migratio

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blure.complexview.ComplexView
import org.ifaco.migratio.Fun.Companion.c
import org.ifaco.migratio.Fun.Companion.connected
import org.ifaco.migratio.Fun.Companion.defaultMyCriteria
import org.ifaco.migratio.Fun.Companion.dm
import org.ifaco.migratio.Fun.Companion.logoFont
import org.ifaco.migratio.Fun.Companion.now
import org.ifaco.migratio.Fun.Companion.sp
import org.ifaco.migratio.Fun.Companion.textFont
import org.ifaco.migratio.Fun.Companion.titleFont
import org.ifaco.migratio.Fun.Companion.vis
import org.ifaco.migratio.adap.MyConAdap
import org.ifaco.migratio.data.*
import java.lang.StringBuilder
import kotlin.math.round
import org.ifaco.migratio.data.Criterion as Criterion1

@Suppress("UNCHECKED_CAST")
class Panel : AppCompatActivity() {
    lateinit var body: ConstraintLayout
    lateinit var toolbar: Toolbar
    lateinit var tbShadow: View
    lateinit var main: ConstraintLayout
    lateinit var rvMyConEmpty: ConstraintLayout
    lateinit var tvRVMCE: TextView
    lateinit var pMyCriteria: ConstraintLayout
    lateinit var fabGTS: ComplexView
    lateinit var goToSelect: ConstraintLayout
    lateinit var load: ConstraintLayout
    lateinit var logo: ImageView
    lateinit var logoText: TextView
    lateinit var logoReload: ImageView
    lateinit var loading: ImageView

    var gotCriteria: List<Criterion1>? = null
    var myCriteria: List<MyCriterion>? = null
    var tapToExit = false
    var loaded = false
    var canGoToSelect = true
    var myCountries: MutableSet<String>? = null
    var anReload: ObjectAnimator? = null
    var selectGuide: AnimatorSet? = null
    var showingHelp = false
    var showingAbout = false
    var computeSuspendedForRepair = false

    companion object {
        lateinit var rvMyCon: RecyclerView

        lateinit var handler: Handler

        const val exLastUpdated = "lastUpdated"
        const val exCensor = "censor"
        const val exRepair = "repair"
        var gotCountries: List<Country>? = null
        var myconAdapter: MyConAdap? = null
        var rvScrollY = 0
        var allComputations: List<Computation>? = null
        var computations: ArrayList<Computation>? = null

        fun search(text: String) {
            if (allComputations == null || computations == null || gotCountries == null || myconAdapter == null) return
            computations = ArrayList()
            for (p in allComputations!!)
                if (Fun.countryNames()[
                            Computation.findConById(p.id, gotCountries!!)!!.id.toInt()
                    ].contains(text, true)
                ) computations!!.add(p)
            arrange(0)
        }

        fun arrange(scrollY: Int) {
            myconAdapter = MyConAdap(c, computations!!, gotCountries!!)
            rvMyCon.adapter = myconAdapter
            rvMyCon.scrollBy(0, scrollY)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.panel)

        body = findViewById(R.id.body)
        toolbar = findViewById(R.id.toolbar)
        tbShadow = findViewById(R.id.tbShadow)
        main = findViewById(R.id.main)
        rvMyCon = findViewById(R.id.rvMyCon)
        rvMyConEmpty = findViewById(R.id.rvMyConEmpty)
        tvRVMCE = findViewById(R.id.tvRVMCE)
        pMyCriteria = findViewById(R.id.pMyCriteria)
        fabGTS = findViewById(R.id.fabGTS)
        goToSelect = findViewById(R.id.goToSelect)
        load = findViewById(R.id.load)
        logo = findViewById(R.id.logo)
        logoText = findViewById(R.id.logoText)
        logoReload = findViewById(R.id.logoReload)
        loading = findViewById(R.id.loading)

        Fun.init(this, body)
        Fun.isOnlineOld()
        rvScrollY = 0
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Works.DOWNLOAD.ordinal -> if (msg.obj != null) when (msg.arg1) {
                        Types.COUNTRY.ordinal -> Work(
                            c, handler, Works.CLEAR_AND_INSERT_ALL, Types.COUNTRY,
                            listOf(msg.obj as List<Country>, Types.COUNTRY.ordinal)
                        ).start()
                        Types.CRITERION.ordinal -> Work(
                            c, handler, Works.CLEAR_AND_INSERT_ALL, Types.CRITERION,
                            listOf(msg.obj as List<Criterion1>, Types.CRITERION.ordinal)
                        ).start()
                    }

                    Works.GET_ALL.ordinal -> when (msg.arg1) {
                        Works.CHECK.ordinal -> when (msg.arg2) {
                            Types.COUNTRY.ordinal -> {
                                gotCountries = msg.obj as List<Country>
                                if (gotCountries.isNullOrEmpty() || doRefreshData()) {
                                    if (connected) Parse(c, handler, Types.COUNTRY).start()
                                    else noInternet()
                                } else postLoading()
                            }
                            Types.CRITERION.ordinal -> {
                                gotCriteria = msg.obj as List<Criterion1>
                                if (gotCriteria.isNullOrEmpty() || doRefreshData()) {
                                    if (connected) Parse(c, handler, Types.CRITERION).start()
                                    else noInternet()
                                } else postLoading()// defaultMyCriteria() is MESSY here
                            }
                            Types.MY_CRITERION.ordinal -> {
                                myCriteria = msg.obj as List<MyCriterion>
                                if (!needsRepair(true)) compute(rvScrollY)
                            }
                        }
                    }

                    Works.CLEAR_AND_INSERT_ALL.ordinal -> when (msg.arg1) {
                        Types.COUNTRY.ordinal -> {
                            gotCountries = msg.obj as List<Country>
                            if (dataLoaded()) loaded()
                            postLoading(true)
                        }
                        Types.CRITERION.ordinal -> {
                            gotCriteria = msg.obj as List<Criterion1>
                            if (myCriteria.isNullOrEmpty())
                                defaultMyCriteria(gotCriteria!!, handler)
                            else sp.edit().putBoolean(exRepair, true).apply()
                            postLoading(true)
                        }
                        Types.MY_CRITERION.ordinal -> when (msg.arg2) {
                            Works.REPAIR.ordinal -> {
                                sp.edit().putBoolean(exRepair, false).apply()
                                if (computeSuspendedForRepair) {
                                    computeSuspendedForRepair = false
                                    compute()
                                }
                            }
                        }
                    }
                }
            }
        }
        restoration(savedInstanceState)


        // Loading
        load.setOnClickListener { }
        logoText.typeface = logoFont
        logoReload.setOnClickListener {
            if (loading.visibility == View.VISIBLE) return@setOnClickListener
            if (connected) {
                anReload = Fun.load1(loading, logoReload)
                Parse(c, handler, Types.COUNTRY).start()
                Parse(c, handler, Types.CRITERION).start()
            } else noInternet()
        }
        if (loaded) body.removeView(load)
        else if (gotCountries == null || gotCriteria == null) {
            Work(
                c, handler, Works.GET_ALL, Types.COUNTRY,
                listOf(Works.CHECK.ordinal, Types.COUNTRY.ordinal)
            ).start()
            Work(
                c, handler, Works.GET_ALL, Types.CRITERION,
                listOf(Works.CHECK.ordinal, Types.CRITERION.ordinal)
            ).start()
        }
        Fun.handleTB(this, toolbar, titleFont)

        // Go to Select
        goToSelect.setOnClickListener {
            if (!canGoToSelect || gotCountries == null || gotCriteria == null) return@setOnClickListener
            startActivity(
                Intent(c, Select::class.java)
                    .putExtra("countries", gotCountries!!.toTypedArray())
                    .putExtra("criteria", gotCriteria!!.toTypedArray())
            )
            canGoToSelect = false
            object : CountDownTimer(1000, 1000) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    canGoToSelect = true
                }
            }.start()
        }
        selectGuide = Fun.bolden(fabGTS, 1.22f)

        // Help
        tvRVMCE.setOnClickListener { help() }
    }

    override fun onResume() {
        super.onResume()

        // List (DOESN'T NEED INTERNET!)
        myCountries = sp.getStringSet(Select.exMyCountries, null)
        /*object : CountDownTimer(5000, 50) {
            override fun onFinish() {}
            override fun onTick(millisUntilFinished: Long) {
                if (myCountries.isNullOrEmpty()) return
                cancel()
            }
        }.start()*/
        Work(
            c, handler, Works.GET_ALL, Types.MY_CRITERION,
            listOf(Works.CHECK.ordinal, Types.MY_CRITERION.ordinal)
        ).start()

        // Other
        canGoToSelect = true
    }

    override fun onPause() {
        super.onPause()
        rvScrollY = rvMyCon.computeVerticalScrollOffset()
    }

    override fun onSaveInstanceState(state: Bundle) {
        state.putBoolean("loaded", loaded)
        state.putBoolean("showingHelp", showingHelp)
        state.putBoolean("showingAbout", showingAbout)
        if (gotCountries != null)
            state.putParcelableArray("gotCountries", gotCountries!!.toTypedArray())
        if (gotCriteria != null)
            state.putParcelableArray("gotCriteria", gotCriteria!!.toTypedArray())
        state.putInt("rvScrollY", rvMyCon.scrollY)
        super.onSaveInstanceState(state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoration(savedInstanceState)
    }

    override fun onBackPressed() {
        if (!tapToExit) {
            Toast.makeText(c, R.string.tapToExit, Toast.LENGTH_LONG).show()
            tapToExit = true
            object : CountDownTimer(3000, 3000) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    tapToExit = false
                }
            }.start(); return
        }
        sp.edit().apply {
            remove(Select.exSwitchedTo2nd)
            apply()
        }
        moveTaskToBack(true)
        Process.killProcess(Process.myPid())
        kotlin.system.exitProcess(1)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.panel, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.pmSearch -> true
        R.id.pmRefresh -> {
            refresh(); true
        }
        R.id.pmShareResults -> {
            if (allComputations != null && gotCountries != null)
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.shareResSubject))
                    putExtra(Intent.EXTRA_TEXT, share())
                }, resources.getString(R.string.shareResChooser)))
            true
        }
        R.id.pmHelp -> {
            help(); true
        }
        R.id.pmAbout -> {
            about(); true
        }
        else -> super.onOptionsItemSelected(item)
    }


    fun restoration(state: Bundle?) {
        if (state == null) return
        loaded = state.getBoolean("loaded", false)
        if (state.getBoolean("showingHelp", false)) help()
        if (state.getBoolean("showingAbout", false)) about()
        if (gotCountries == null)
            gotCountries = state.getParcelableArray("gotCountries")?.toList() as List<Country>
        if (gotCriteria == null)
            gotCriteria = state.getParcelableArray("gotCriteria")?.toList() as List<Criterion1>
    }

    fun refresh() {
        Fun.isOnlineOld()
        if (connected) {
            Parse(c, handler, Types.COUNTRY).start()
            Parse(c, handler, Types.CRITERION).start()
        }
    }

    fun dataLoaded() = !gotCountries.isNullOrEmpty() && !gotCriteria.isNullOrEmpty()

    fun loaded() {
        if (loaded) return
        loaded = true
        ObjectAnimator.ofFloat(load, "translationX", -dm.widthPixels * 1.5f).apply {
            startDelay = 1110
            duration = 870
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    body.removeView(load)
                }
            })
            start()
        }
    }

    fun noInternet() {
        Toast.makeText(c, R.string.noInternet, Toast.LENGTH_SHORT).show()
        vis(logoText, false)
        vis(logoReload)
    }

    fun postLoading(didUpdate: Boolean = false) {
        if (didUpdate) sp.edit().apply {
            putLong(exLastUpdated, now())
            apply()
        }
        if (dataLoaded()) loaded()
        vis(loading, false)
        if (!needsRepair(true)) compute()
    }

    fun needsRepair(computeAfter: Boolean = false): Boolean {
        if (myCriteria == null || gotCriteria == null) return true
        var rut = sp.getBoolean(exRepair, false)
        if (rut) {
            computeSuspendedForRepair = computeAfter
            Fun.repairMyCriteria(gotCriteria!!, myCriteria!!, handler)
        }
        return rut
    }

    fun doRefreshData() = (now() - sp.getLong(exLastUpdated, 0)) > Fun.doRefreshTime

    fun compute(scrollY: Int = 0) {
        if (myCountries.isNullOrEmpty() || gotCountries.isNullOrEmpty() ||
            gotCriteria.isNullOrEmpty() || myCriteria.isNullOrEmpty()
        ) {
            resetMyCon(); return; }
        var atLeastOneCri = false
        for (mycri in myCriteria!!) if (mycri.isOn) atLeastOneCri = true
        if (!atLeastOneCri) {
            resetMyCon(); return; }

        selectGuide?.cancel()
        selectGuide = null
        fabGTS.scaleX = 1f
        fabGTS.scaleY = 1f

        vis(rvMyConEmpty, false)
        allComputations =
            Computation.compute(gotCountries!!, gotCriteria!!, myCountries!!.toList(), myCriteria!!)
        if (allComputations == null) return
        computations = allComputations!!.toCollection(ArrayList())
        arrange(scrollY)
    }

    fun resetMyCon() {
        vis(rvMyConEmpty)
        myconAdapter = null
        rvMyCon.adapter = null
    }

    fun share(): String {
        if (allComputations == null || gotCountries == null) return ""
        val sb = StringBuilder()
        for (p in allComputations!!.indices)
            sb.append(
                "${p + 1}. ${
                    Fun.countryNames()[Computation.findConById(
                        allComputations!![p].id, gotCountries!!
                    )!!.id.toInt()]
                } (${round(allComputations!![p].score).toInt()}%)\n"
            )
        return sb.toString()
    }

    fun help() {
        if (showingHelp) return
        showingHelp = true
        Fun.alertDialogue1(this, R.string.pmHelp, R.string.pHelp, textFont,
            { _, _ -> showingHelp = false }, { showingHelp = false }
        )
    }

    fun about() {
        if (showingAbout) return
        showingAbout = true
        Fun.alertDialogue1(this, R.string.pmAbout, R.string.about, textFont,
            { _, _ -> showingAbout = false }, { showingAbout = false }
        )
    }
}
