package com.tjbaobao.gitee.googlebillingutil

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.tjbaobao.gitee.billing.GoogleBillingUtil
import com.tjbaobao.gitee.billing.OnGoogleBillingListener
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var googleBillingUtil: GoogleBillingUtil

    private val logBuffer = StringBuffer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GoogleBillingUtil.isDebug(true)
        GoogleBillingUtil.setSkus(arrayOf("tips_level1","tips_level2","tips_level3"), arrayOf("weekly","monthly","yearly"))
//        GoogleBillingUtil.setSkus(arrayOf("tips_level1","tips_level2","tips_level3"), null)//如果没有订阅
        GoogleBillingUtil.setIsAutoConsumeAsync(false)//在你不完成熟悉自动消耗的逻辑是，建议关闭自动消耗功能，或者改用122-21版本
        googleBillingUtil = GoogleBillingUtil.getInstance()
            .addOnGoogleBillingListener(this, OnMyGoogleBillingListener())
            .build(this)

        //region============================设置按钮点击监听============================

        btNext.setOnClickListener {
            NextActivity.toActivity(this)
        }
        tvLogClean.setOnClickListener {
            tvLog.text = ""
        }
        ivHelp.setOnClickListener {
            val intent = Intent()
            val uri = Uri.parse("https://github.com/TJHello/GoogleBilling")
            intent.action = Intent.ACTION_VIEW
            intent.data = uri
            startActivity(intent)
        }

        //endregion

    }

    private inner class OnMyGoogleBillingListener : OnGoogleBillingListener() {
        //内购服务初始化成功
        override fun onSetupSuccess(isSelf: Boolean) {
            log("内购服务初始化完成")
            checkSubs()
        }

        override fun onQuerySuccess(skuType: String, list: MutableList<SkuDetails>,isSelf: Boolean) {
            if(!isSelf) return
            val tempBuffer = StringBuffer()
            tempBuffer.append("查询商品信息成功($skuType):\n")
            for((i, skuDetails) in list.withIndex()){
                val details = String.format(Locale.getDefault(),"%s , %s",
                    skuDetails.sku,skuDetails.price
                    )
                tempBuffer.append(details)
                if(i!=list.size-1){
                    tempBuffer.append("\n")
                }
            }
            log(tempBuffer.toString())
        }

        override fun onPurchaseSuccess(purchase: Purchase,isSelf: Boolean) {
            val tempBuffer = StringBuffer()
            val details = String.format(Locale.getDefault(),"购买商品成功:%s", purchase.sku)
            tempBuffer.append(details)
            log(tempBuffer.toString())
        }

        override fun onConsumeSuccess(purchaseToken: String,isSelf: Boolean) {
            log("消耗商品成功:$purchaseToken")
        }

        override fun onFail(tag: GoogleBillingUtil.GoogleBillingListenerTag, responseCode: Int,isSelf: Boolean) {
            log("操作失败:tag=${tag.name},responseCode=$responseCode")
        }

        override fun onError(tag: GoogleBillingUtil.GoogleBillingListenerTag,isSelf: Boolean) {
            log("发生错误:tag=${tag.name}")
        }
    }

    /**
     * 检查是否有有效订阅
     */
    private fun checkSubs(){
        when(val size = googleBillingUtil.getPurchasesSizeSubs(this)){
            0->{
                //不具备有效订阅
                log("有效订阅数:0(无有效订阅)")
            }
            -1->{
                //查询失败
                log("有效订阅数:-1(查询失败)")
            }
            else->{
                //具有有效订阅
                log("有效订阅数:$size(具备有效订阅)")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        googleBillingUtil.onDestroy(this)
        //退出程序的时候可以调用(实验性)
        GoogleBillingUtil.endConnection()
    }

    private fun log(msg:String){
        val log = String.format(Locale.getDefault(),"%s\n%s\n\n",getTime(),msg)
        logBuffer.append(log)
        tvLog.text = logBuffer.toString()
    }

    private fun getTime():String{
        val dt = Date()
        val sdf = DateFormat.getInstance() as SimpleDateFormat
        sdf.applyPattern("HH:mm:ss.SSS")
        return sdf.format(dt)
    }
}
