package com.mook.addressselector

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


/**
 * Created by xujianliu.
 * Date: 2022/2/13.
 */
class AsViewPagerAdapter(val viewPager: ViewPager) : PagerAdapter() {

    private var mDataList: MutableList<RecyclerView> = ArrayList()

    fun setDatas(views: MutableList<RecyclerView>) {
        this.mDataList = views
        notifyDataSetChanged()
    }

    fun addData(view: RecyclerView) {
        mDataList.add(view)
        notifyDataSetChanged()
    }

    override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE

    fun removeData(position: Int) {
        val lists = arrayListOf<RecyclerView>()
        for (i in position + 1 until mDataList.size) {
            lists.add(mDataList[i])
            viewPager.removeView(mDataList[i])
        }
        mDataList.removeAll(lists)
        notifyDataSetChanged()
    }

    fun getDatas() = mDataList

    override fun getCount() = mDataList.size

    override fun isViewFromObject(view: View, `object`: Any) = view === `object`

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(mDataList[position])
        return mDataList[position]
    }

}