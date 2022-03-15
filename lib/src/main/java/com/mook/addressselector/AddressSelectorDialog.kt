package com.mook.addressselector

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mook.addressselector.adapter.DataAdapter
import com.mook.addressselector.databinding.DialogAddressSelectorBinding
import com.mook.addressselector.databinding.ItemRecyclerviewBinding
import com.mook.addressselector.model.AddressBean
import com.mook.addressselector.model.City
import com.mook.addressselector.model.District
import com.mook.addressselector.model.Province
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.xml.parsers.SAXParserFactory

/**
 * Created by xujianliu.
 * Date: 2022/2/13.
 */
class AddressSelectorDialog(
    val onSelected: (address: String) -> Unit,
    val onSelectedDetails: (province: String, city: String, district: String) -> Unit
) : BottomSheetDialogFragment(), DialogInterface.OnDismissListener {
    private val TAG = "AddressSelectorDialog"
    private lateinit var mBinding: DialogAddressSelectorBinding

    /** ViewPager适配器  */
    private val pagerAdapter: AsViewPagerAdapter by lazy {
        AsViewPagerAdapter(mBinding.vpContainer)
    }

    private val initContentData: String? = null

    internal fun show(manager: FragmentManager) {
        super.show(manager, "AsDialog")
    }

    override fun show(manager: FragmentManager, tag: String?) {

    }


    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        return -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setCancelable(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogAddressSelectorBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        mBinding.vpContainer.adapter = pagerAdapter
        mBinding.vpContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            /*因为BottomSheetBehavior.findScrollingChild()方法中只取第一个可滑动的view让它支持滑动，
            *所以需要手动设置，viewpager当前显示的页面未可滑动的(即isNestedScrollingEnabled==true)。
            * 其它，view的isNestedScrollingEnabled都为false。
            * 且在滑动完成以后，需要刷新viewpager,此处设置才会生效。
            */
            override fun onPageSelected(position: Int) {
                val currentItem = mBinding.vpContainer.currentItem
                Log.i(TAG, "onPageSelected: position=$position\t$currentItem")
                for ((index, recyclerView) in pagerAdapter.getDatas().withIndex()) {
                    recyclerView.isNestedScrollingEnabled = index == position
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE)
                    pagerAdapter.notifyDataSetChanged()

                Log.i(TAG, "onPageScrollStateChanged: $state")
            }
        })
        mBinding.asIndicators.setupWithViewPager(mBinding.vpContainer)
        mBinding.tvConfirm.setOnClickListener {
            onSelected(mBinding.asIndicators.getContent())
            val items = mutableListOf<String>()
            items.addAll(mBinding.asIndicators.getItems())
            if (items.size < 3) {
                for (i in 0..(3 - items.size)) {
                    items.add("")
                }
            }
            onSelectedDetails(items[0], items[1], items[2])
            dismiss()
        }
        mBinding.close.setOnClickListener { dismiss() }

        loadData()
    }

    private fun showConfirmBtn() {
        mBinding.tvConfirm.visibility = View.VISIBLE
    }

    private fun hideConfirmBtn() {
        mBinding.tvConfirm.visibility = View.GONE
    }

    private fun createViewPagerItem(type: Int): RecyclerView {
        val adapter = when (type) {
            0 -> DataAdapter<Province> {
                removeViewPagerItem()
                loadCityData(it)
            }
            1 -> DataAdapter<City> {
                removeViewPagerItem()
                loadDistrict(it)
            }
            else -> DataAdapter<District> {
                val currentItem = mBinding.vpContainer.currentItem
                showConfirmBtn()
                mBinding.asIndicators.changeItem(
                    currentItem,
                    it.name,
                    false
                )
            }
        }
        val context = this.requireContext()
        val recyclerView = ItemRecyclerviewBinding.inflate(LayoutInflater.from(context)).root
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
        return recyclerView
    }

    private fun removeViewPagerItem() {
        val currentItem = mBinding.vpContainer.currentItem
        if (mBinding.asIndicators.isSelectedItem(currentItem)) {
            pagerAdapter.removeData(currentItem)
        }
    }

    private fun loadProvinceData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val list = parseXml()
                withContext(Dispatchers.Main) {
                    setRecyclerViewData(0, list)
                }
            }
        }
    }

    private fun <T : AddressBean> setRecyclerViewData(type: Int, data: List<T>) {
        val recyclerView = createViewPagerItem(type)
        recyclerView.isNestedScrollingEnabled = true
        (recyclerView.adapter as DataAdapter<T>).setData(data)
        pagerAdapter.addData(recyclerView)
    }

    private fun loadCityData(province: Province) {
        if (province.hasChild()) {
            hideConfirmBtn()
            val name = province.name
            if (name == "北京市" || name == "天津市" || name == "上海市" || name == "重庆市") {
                loadDistrict(province.cityList[0])
                return
            } else {
                setRecyclerViewData(1, province.cityList)
            }
        } else if (TextUtils.isEmpty(mBinding.asIndicators.getContent())) {
            showConfirmBtn()
        }

        mBinding.asIndicators.changeItem(
            mBinding.vpContainer.currentItem,
            province.name,
            province.hasChild()
        )
    }

    /**
     * 加载区数据
     */
    private fun loadDistrict(city: City) {
        if (city.hasChild()) {
            hideConfirmBtn()
            setRecyclerViewData(2, city.districtList)
        } else {
            showConfirmBtn()
        }

        mBinding.asIndicators.changeItem(
            mBinding.vpContainer.currentItem,
            city.name,
            city.hasChild()
        )
    }

    /**
     * 解析xml数据
     * @return
     */
    private fun parseXml(): List<Province> {
        val asset = context?.assets
        try {
            val input = asset?.open("province_data.xml")
            val spf = SAXParserFactory.newInstance()
            val parser = spf.newSAXParser()
            val handler = XmlParserHandler()
            parser.parse(input, handler)
            input?.close()
            return handler.getDataList()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return arrayListOf()
    }

    private fun loadData() {
        loadProvinceData()
        if (TextUtils.isEmpty(initContentData)) {
            mBinding.asIndicators.changeItem(0, "", true)
        } else {
            val items = initContentData?.split(",")
            for (i in 0..2) {
                if (i == 0) {

                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        dialog.behavior.run {
            isHideable = false
            peekHeight = 3000
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
        }
        return dialog
    }

    companion object {
        fun show(
            fragment: Fragment,
            onSelected: (address: String) -> Unit,
            onSelectedDetails: (province: String, city: String, district: String) -> Unit
        ): AddressSelectorDialog {
            return show(fragment.parentFragmentManager, onSelected, onSelectedDetails)
        }

        fun show(
            activity: FragmentActivity,
            onSelected: (address: String) -> Unit,
            onSelectedDetails: (province: String, city: String, district: String) -> Unit
        ): AddressSelectorDialog {
            return show(activity.supportFragmentManager, onSelected, onSelectedDetails)
        }

        private fun show(
            fragmentManager: FragmentManager,
            onSelected: (address: String) -> Unit,
            onSelectedDetails: (province: String, city: String, district: String) -> Unit
        ): AddressSelectorDialog {
            val asDialog = AddressSelectorDialog(onSelected, onSelectedDetails)
            asDialog.show(fragmentManager)
            return asDialog
        }
    }

}