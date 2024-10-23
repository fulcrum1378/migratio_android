package ir.mahdiparastesh.migratio

import android.animation.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.util.Linkify
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ir.mahdiparastesh.migratio.data.*
import ir.mahdiparastesh.migratio.misc.BaseActivity
import ir.mahdiparastesh.migratio.misc.Fonts
import java.util.*

object Fun {
    const val td1Dur = 168
    var censorBreak = 0

    fun switcher(
        c: BaseActivity, vs: ViewSwitcher, dirLtr: Boolean, animate: Boolean = true,
        exSwitched: String = Select.exSwitchedTo2nd
    ): Boolean {
        val ir = if (dirLtr) R.anim.slide_in_right else R.anim.slide_in_left
        val il = if (dirLtr) R.anim.slide_in_left else R.anim.slide_in_right
        val ol = if (dirLtr) R.anim.slide_out_left else R.anim.slide_out_right
        val or = if (dirLtr) R.anim.slide_out_right else R.anim.slide_out_left
        vs.inAnimation = if (animate)
            AnimationUtils.loadAnimation(c, if (vs.displayedChild == 0) ir else il)
        else null
        vs.outAnimation = if (animate)
            AnimationUtils.loadAnimation(c, if (vs.displayedChild == 0) ol else or)
        else null
        vs.displayedChild = if (vs.displayedChild == 0) 1 else 0
        c.sp.edit().putBoolean(exSwitched, vs.displayedChild == 1).apply()
        return vs.displayedChild == 1
    }

    fun load1(iv: ImageView, placeHolder: View? = null, dur: Long = 444): ObjectAnimator =
        ObjectAnimator.ofFloat(iv, "rotation", 0f, 360f).apply {
            duration = dur
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    placeHolder?.isVisible = false
                    iv.isVisible = true
                }

                override fun onAnimationCancel(animation: Animator) {
                    iv.isVisible = false
                    placeHolder?.isVisible = true
                }
            })
            start()
        }

    fun bolden(v: View, scale: Float, dur: Long = 870): AnimatorSet = AnimatorSet().apply {
        playTogether(
            ObjectAnimator.ofFloat(v, "scaleX", 1f, scale),
            ObjectAnimator.ofFloat(v, "scaleY", 1f, scale)
        )
        childAnimations.forEach {
            (it as ObjectAnimator).apply {
                duration = dur
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE//RESTART
            }
        }
        start()
    }

    fun countryNames(): Array<String> = when (Locale.getDefault().language) {
        "fa" -> Countries.FA
        else -> Countries.EN
    }

    fun fonts(c: Context, which: Fonts): Typeface =
        Typeface.createFromAsset(c.assets, c.resources.getString(which.id))

    fun now(): Long = Calendar.getInstance().timeInMillis

    fun alertDialogue1(
        c: BaseActivity, title: Int, message: Int, font: Typeface,
        onOk: DialogInterface.OnClickListener? = null,
        onCancel: DialogInterface.OnCancelListener? = null,
        censorBreaker: Boolean = false
    ): Boolean {
        AlertDialog.Builder(c, R.style.AlertDialogue).apply {
            setTitle(title)
            setMessage(message)
            setIcon(R.mipmap.launcher_round)
            setPositiveButton(R.string.ok, onOk)
            setOnCancelListener(onCancel)
        }.create().apply {
            show()
            fixADButton(c, getButton(AlertDialog.BUTTON_POSITIVE), font)
            fixADTitle(c, font)
            var tvMsg = fixADMsg(c, font)

            // Censor Breaker
            if (censorBreaker) tvMsg?.setOnClickListener {
                censorBreak += 1
                if (censorBreak >= 10) {
                    Select.handler?.obtainMessage(Works.BREAK_CENSOR.ordinal, null)
                        ?.sendToTarget()
                    cancel()
                }
                val time: Long = 5000
                object : CountDownTimer(time, time) {
                    override fun onTick(p0: Long) {}
                    override fun onFinish() {
                        censorBreak = 0
                    }
                }.start()
            }
        }
        return true
    }

    fun alertDialogue2(
        c: BaseActivity, title: Int, message: Int,
        onYes: DialogInterface.OnClickListener? = null,
        onNo: DialogInterface.OnClickListener? = null,
        onCancel: DialogInterface.OnCancelListener? = null,
        font: Typeface = c.textFont
    ): Boolean {
        AlertDialog.Builder(c, R.style.AlertDialogue).apply {
            setTitle(title)
            setMessage(message)
            setIcon(R.mipmap.launcher_round)
            setPositiveButton(R.string.yes, onYes)
            setNegativeButton(R.string.no, onNo)
            setOnCancelListener(onCancel)
        }.create().apply {
            show()
            fixADButton(c, getButton(AlertDialog.BUTTON_POSITIVE), font, c.dirLtr)
            fixADButton(c, getButton(AlertDialog.BUTTON_NEGATIVE), font, !c.dirLtr)
            fixADTitle(c, font)
            fixADMsg(c, font)
        }
        return true
    }

    fun alertDialogue3(
        c: BaseActivity, title: Int, message: String,
        copyable: Boolean, linkify: Boolean = false
    ): Boolean {
        AlertDialog.Builder(c, R.style.AlertDialogue).apply {
            setTitle(title)
            setMessage(message)
            setIcon(R.mipmap.launcher_round)
            setPositiveButton(R.string.ok, null)
        }.create().apply {
            show()
            var font = c.textFont
            fixADButton(c, getButton(AlertDialog.BUTTON_POSITIVE), font)
            fixADTitle(c, font)
            var tvMsg = fixADMsg(c, font, linkify)
            if (copyable) tvMsg?.setOnLongClickListener {
                copyItsText(c, tvMsg)
                true
            }
        }
        return true
    }

    fun fixADButton(c: BaseActivity, button: Button, font: Typeface, sMargin: Boolean = false) {
        button.apply {
            setTextColor(ContextCompat.getColor(c, R.color.CA))
            setBackgroundColor(ContextCompat.getColor(c, R.color.CP))
            typeface = font
            textSize = c.resources.getDimension(R.dimen.alert1Button) / c.dm.density
            if (sMargin) (layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = textSize.toInt()
            }
        }
    }

    fun fixADTitle(c: BaseActivity, font: Typeface): TextView? {
        var tvTitle = c.window?.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
        tvTitle?.setTypeface(font, Typeface.BOLD)
        tvTitle?.textSize = c.resources.getDimension(R.dimen.alert1Title) / c.dm.density
        return tvTitle
    }

    fun fixADMsg(c: BaseActivity, font: Typeface, linkify: Boolean = false): TextView? {
        var tvMsg = c.window?.findViewById<TextView>(android.R.id.message)
        tvMsg?.typeface = font
        tvMsg?.setLineSpacing(
            c.resources.getDimension(R.dimen.alert1MsgLine) / c.dm.density, 0f
        )
        tvMsg?.textSize = c.resources.getDimension(R.dimen.alert1Msg) / c.dm.density
        tvMsg?.setPadding(c.dp(28), c.dp(15), c.dp(28), c.dp(15))
        if (tvMsg != null && linkify) tvMsg.autoLinkMask = Linkify.WEB_URLS // FIXME
        return tvMsg
    }

    fun copyText(c: Context, s: String) {
        (c.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(
            ClipData.newPlainText("simple text", s)
        )
    }

    fun copyItsText(c: Context, tv: TextView) {
        copyText(c, tv.text.toString())
        Toast.makeText(c, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    fun z(n: Int): String {
        var s = n.toString()
        return if (s.length == 1) "0$s" else s
    }
}
