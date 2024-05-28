package com.example.csi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kr.co.bootpay.android.*;
import kr.co.bootpay.android.Bootpay
import kr.co.bootpay.android.BootpayAnalytics
import kr.co.bootpay.android.events.BootpayEventListener
import kr.co.bootpay.android.models.BootExtra
import kr.co.bootpay.android.models.BootItem
import kr.co.bootpay.android.models.BootUser
import kr.co.bootpay.android.models.Payload
import kr.co.bootpay.android.models.statistics.BootStatItem

class Buy2Activity : AppCompatActivity() {
    private val application_id = "663a3fae19b42d44e97685ba" //production

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)
    }
    fun DefaultPayment(v: View?) {
        val intent = Intent(applicationContext, DefaultPaymentActivity::class.java)
        startActivity(intent)
    }

    fun TotalPayment(v: View?) {
        val intent = Intent(applicationContext, TotalPaymentActivity::class.java)
        startActivity(intent)
    }
    fun goRequest(v: View?) {
        val user = BootUser().setPhone("010-1234-5678") // 구매자 정보
        val extra = BootExtra()
            .setCardQuota("0,2,3") // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)

        var price = 3000.0

        val pg: String = "나이스페이"
        val method: String = "카드"

        val items: MutableList<BootItem> = ArrayList()
        val item1 = BootItem().setName("마우's 스").setId("ITEM_CODE_MOUSE").setQty(1).setPrice(500.0)
        val item2 = BootItem().setName("키보드").setId("ITEM_KEYBOARD_MOUSE").setQty(1).setPrice(500.0)
        items.add(item1)
        items.add(item2)

        val payload = Payload()
        payload.setApplicationId(application_id)
            .setOrderName("부트페이 결제테스트")
            .setPg(pg)
            .setOrderId("1234")
            .setMethod(method)
            .setPrice(price)
            .setUser(user)
            .setExtra(extra).items = items

        val map: MutableMap<String, Any> = HashMap()
        map["1"] = "abcdef"
        map["2"] = "abcdef55"
        map["3"] = 1234
        payload.metadata = map

        Bootpay.init(supportFragmentManager, applicationContext)
            .setPayload(payload)
            .setEventListener(object : BootpayEventListener {
                override fun onCancel(data: String) {
                    Log.d("bootpay", "cancel: $data")
                }

                override fun onError(data: String) {
                    Log.d("bootpay", "error: $data")
                }

                override fun onClose() {
                    Log.d("bootpay", "close")
                    Bootpay.removePaymentWindow()  // 결제 창을 닫는 올바른 메소드 호출
                }


                override fun onIssued(data: String) {
                    Log.d("bootpay", "issued: $data")
                }

                override fun onConfirm(data: String): Boolean {
                    Log.d("bootpay", "confirm: $data")
                    return true
                }

                override fun onDone(data: String) {
                    Log.d("bootpay", "done: $data")
                }
            }).requestPayment()

    }
}