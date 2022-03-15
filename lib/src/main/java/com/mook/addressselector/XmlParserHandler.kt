package com.mook.addressselector

import com.mook.addressselector.model.City
import com.mook.addressselector.model.District
import com.mook.addressselector.model.Province
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.Attributes
import org.xml.sax.SAXException


/**
 * Created by xujianliu.
 * Date: 2022/2/13.
 */
class XmlParserHandler : DefaultHandler() {

    private val provinceList = mutableListOf<Province>()

    fun getDataList(): List<Province> {
        return provinceList
    }

    @Throws(SAXException::class)
    override fun startDocument() {
    }

    var provinceModel: Province? = null
    var cityModel: City? = null
    var districtModel: District? = null

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        if (qName == "province") {
            provinceModel = Province(attributes.getValue(0))
        } else if (qName == "city") {
            cityModel = City(attributes.getValue(0))
        } else if (qName == "district") {
            districtModel = District(attributes.getValue(0))
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        if (qName == "district" && districtModel != null) {
            cityModel?.districtList?.add(districtModel!!)
        } else if (qName == "city" && cityModel != null) {
            provinceModel?.cityList?.add(cityModel!!)
        } else if (qName == "province" && provinceModel != null) {
            provinceList.add(provinceModel!!)
        }
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
    }

}