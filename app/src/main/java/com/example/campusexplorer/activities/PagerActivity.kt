package com.example.campusexplorer.activities

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup

import com.example.campusexplorer.R
import kotlinx.android.synthetic.main.activity_pager.*
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.campusexplorer.SharedPrefmanager


class PagerActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mNextBtn: ImageButton? = null
    private var mFinishBtn: Button? = null
    private var mSkipBtn: Button? = null

    private var mIndicator0: ImageView? = null
    private var mIndicator1: ImageView? = null
    private var mIndicator2: ImageView? = null

    private var indicators: MutableList<ImageView> = ArrayList()

    private var page:Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mNextBtn = findViewById(R.id.intro_btn_next)
        mFinishBtn = findViewById(R.id.intro_btn_finish)
        mSkipBtn = findViewById(R.id.intro_btn_skip)

        mIndicator0 = findViewById(R.id.intro_indicator_0)
        mIndicator1 = findViewById(R.id.intro_indicator_1)
        mIndicator2 = findViewById(R.id.intro_indicator_2)

        indicators.add(mIndicator0!!)
        indicators.add(mIndicator1!!)
        indicators.add(mIndicator2!!)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        //val color1 = Color.CYAN
        //val color2 = Color.RED
        //val color3 = Color.GREEN

        val color1 = getColor(R.color.eventVorlesung)
        val color2 = getColor(R.color.eventSeminar)
        val color3 = getColor(R.color.eventUebung)

        val colorList = intArrayOf(color1, color2, color3)


        container.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{

            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                val evaluator = ArgbEvaluator()
                val colorUpdate = evaluator.evaluate(p1,colorList[p0], colorList[if(p0 == 2)p0 else p0 + 1]).toString().toInt()

                container.setBackgroundColor(colorUpdate)

            }

            override fun onPageSelected(p0: Int) {
                updateIndicators(p0)
                page = p0
                Log.d("Page",page.toString())


                when(p0){
                    0 -> container.setBackgroundColor(color1)
                    1 -> container.setBackgroundColor(color2)
                    2 -> container.setBackgroundColor(color3)
            }
                mNextBtn!!.visibility = (if (p0==2)View.GONE else View.VISIBLE)
                mFinishBtn!!.visibility = (if (p0==2)View.VISIBLE else View.GONE)


        }
    })
        SharedPrefmanager.init(this)

        mFinishBtn!!.setOnClickListener {this.finish()
            SharedPrefmanager.saveIntroBool(true)
        }

        mSkipBtn?.setOnClickListener{this.finish()}

        mNextBtn!!.setOnClickListener { this.container.currentItem++ }
    }




    fun updateIndicators(position: Int){
        indicators.forEachIndexed{ index, element-> element.setBackgroundResource(if(index==position)R.drawable.indicator_selected else R.drawable.indicator_unselected) }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_pager, menu)
        return true
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {


        private var mImage: ImageView? = null
        private var mHeader: TextView? = null
        private var mDesc: TextView? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_pager, container, false)

            mImage = rootView.findViewById(R.id.placeholder_image)
            mHeader =rootView.findViewById(R.id.placeholder_header)
            mDesc = rootView.findViewById(R.id.placeholder_description)
            mImage?.setBackgroundResource(0)

            when(arguments?.getInt(ARG_SECTION_NUMBER)){
                0 -> {
                    mImage?.setBackgroundResource(R.drawable.ic_explore_white_200dp)
                    mDesc?.setText(R.string.tut_page_0_description)
                    mHeader?.setText(R.string.tut_page_0_header)
                }
                1 -> {
                    mImage?.setBackgroundResource(R.drawable.ic_crop_free_white_200dp)
                    mDesc?.setText(R.string.tut_page_1_description)
                    mHeader?.setText(R.string.tut_page_1_header)
                }
                2 -> {
                    mImage?.setBackgroundResource(R.drawable.ic_filter_list_white_200dp)
                    mDesc?.setText(R.string.tut_page_2_description)
                    mHeader?.setText(R.string.tut_page_2_header)
                }

            }

            return rootView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
