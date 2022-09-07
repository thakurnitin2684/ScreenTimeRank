package com.thakurnitin2684.screentimerank.ui.view

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.thakurnitin2684.screentimerank.R
import com.thakurnitin2684.screentimerank.databinding.ActivityWelcomeBinding
import com.thakurnitin2684.screentimerank.utils.PrefRepository
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity() {
    private var viewPager: ViewPager? = null
    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var dotsLayout: LinearLayout? = null
    private lateinit var dots: Array<TextView?>
    private lateinit var layouts: IntArray
    private var btnSkip: Button? = null
    private var btnNext: Button? = null

    private lateinit var activityWelcomeBinding: ActivityWelcomeBinding
    private val prefRepository by lazy { PrefRepository(applicationContext) }

    private var activityBool: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityWelcomeBinding = ActivityWelcomeBinding.inflate(layoutInflater)


        val extras = intent.extras
        if (extras != null) {
            val value = extras.getString("KEY")
            if (value == "yes") {
                activityBool = true
            }
        }
        // Checking for first time launch - before calling setContentView()

        if (!prefRepository.getIsFirstTime() && !activityBool) {
            launchHomeScreen()
            finish()
        }



        setContentView(activityWelcomeBinding.root)



        viewPager = activityWelcomeBinding.viewPager
        dotsLayout = activityWelcomeBinding.layoutDots
        btnSkip = activityWelcomeBinding.btnSkip
        btnNext = activityWelcomeBinding.btnNext

        layouts = if (activityBool) {
            intArrayOf(
                R.layout.welcome1,
                R.layout.welcome2,
                R.layout.welcome3
            )
        } else {
            intArrayOf(
                R.layout.welcome1,
                R.layout.welcome2,
                R.layout.welcome3,
                R.layout.welcome4
            )
        }


        // adding bottom dots
        addBottomDots(0)

        // making notification bar transparent
        changeStatusBarColor()
        myViewPagerAdapter = MyViewPagerAdapter()
        viewPager!!.adapter = myViewPagerAdapter
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)
        btnSkip!!.setOnClickListener { launchHomeScreen() }
        btnNext!!.setOnClickListener {
            // checking for last page
            // if last page home screen will be launched
            val current = getItem(+1)
            if (current < layouts.size) {
                // move to next screen
                viewPager!!.currentItem = current
            } else {
                launchHomeScreen()
            }
        }
    }

    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size)
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
        dotsLayout!!.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = Html.fromHtml("&#8226;")
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(colorsInactive[currentPage])
            dotsLayout!!.addView(dots[i])
        }
        if (dots.isNotEmpty()) dots[currentPage]!!.setTextColor(colorsActive[currentPage])
    }

    private fun getItem(i: Int): Int {
        return viewPager!!.currentItem + i
    }

    private fun launchHomeScreen() {
        prefRepository.setIsFirstTime(false)
        if (activityBool) {
            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this@WelcomeActivity, GoogleSignInActivity::class.java))
            finish()
        }
    }

    //  viewpager change listener
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener = object :
        ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.size - 1) {
                btnSkip!!.visibility = View.GONE
            } else {
                // still pages are left
                btnSkip!!.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * Making notification bar transparent
     */
    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    /**
     * View pager adapter
     */
    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater =
                getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view =
                layoutInflater!!.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(
            view: View,
            obj: Any
        ): Boolean {
            return view === obj
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            val view = `object` as View
            container.removeView(view)
        }
    }
}