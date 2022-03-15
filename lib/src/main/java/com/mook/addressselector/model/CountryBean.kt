package com.mook.addressselector.model

/**
 * Created by xujianliu.
 * Date: 2022/2/13.
 */
abstract class AddressBean(open val name: String) {
    abstract fun hasChild(): Boolean
}

/**
 * 国家
 */
data class Country(
    val code: Int,
    override val name: String,
    val province: List<Province> = listOf()
) : AddressBean(name) {
    override fun hasChild() = province.isNotEmpty()
}

/**
 *省
 */
data class Province(
    override val name: String,
    val cityList: MutableList<City> = mutableListOf<City>()
) : AddressBean(name) {
    override fun hasChild() = cityList.isNotEmpty()
}

/**
 * 市
 */
data class City(
    override val name: String,
    val districtList: MutableList<District> = mutableListOf<District>()
) : AddressBean(name) {
    override fun hasChild() = districtList.isNotEmpty()
}

/**
 * 区 或 县
 */
data class District(override val name: String) : AddressBean(name) {
    override fun hasChild() = false
}
