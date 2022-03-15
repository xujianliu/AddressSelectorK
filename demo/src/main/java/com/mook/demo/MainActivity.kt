package com.mook.demo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.mook.addressselector.AddressSelectorDialog
import com.mook.demo.databinding.ActivityMainBinding
/**
 * Created by xujianliu.
 * Date: 2022/2/13.
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val mBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)


        mBinding.btnShow.setOnClickListener {
            AddressSelectorDialog.show(this, { address ->
                mBinding.tvAddress.text = address
                Log.e(TAG, "selected： $address")
            }, { province, city, district ->
                Log.e(TAG, "selected： province=$province; city=$city; district=$district")
            })
        }

    }
}