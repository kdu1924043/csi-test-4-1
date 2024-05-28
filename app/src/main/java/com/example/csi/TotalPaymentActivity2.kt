package com.example.csi

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import kr.co.bootpay.android.Bootpay
import kr.co.bootpay.android.constants.BootpayBuildConfig
import kr.co.bootpay.android.events.BootpayEventListener
import kr.co.bootpay.android.models.BootExtra
import kr.co.bootpay.android.models.BootItem
import kr.co.bootpay.android.models.BootUser
import kr.co.bootpay.android.models.Payload
import java.io.IOException
import java.util.*


class TotalPaymentActivity2 : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var storageReference: StorageReference
    private var applicationId = "663a3fae19b42d44e97685ba"
    private val REQUEST_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_payment)
        if (BootpayBuildConfig.DEBUG) {
            applicationId = "663a3fae19b42d44e97685ba"
        }
        // Firebase Storage의 레퍼런스 생성
        storageReference = FirebaseStorage.getInstance().reference.child("images/gifticon1.png")
    }

    fun PaymentTest(v: View?) {
        val extra = BootExtra()
            .setCardQuota("0,2,3") // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)
        val items: MutableList<BootItem> = ArrayList()
        val item1 = BootItem().setName("마우's 스").setId("ITEM_CODE_MOUSE").setQty(1).setPrice(500.0)
        val item2 = BootItem().setName("키보드").setId("ITEM_KEYBOARD_MOUSE").setQty(1).setPrice(500.0)
        items.add(item1)
        items.add(item2)
        val payload = Payload()

        payload.setApplicationId(applicationId)
            .setOrderName("부트페이 결제테스트")
            .setOrderId("1234")
            .setPrice(3000.0)
            .setUser(getBootUser())
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
                    Bootpay.removePaymentWindow()
                }

                override fun onIssued(data: String) {
                    Log.d("bootpay", "issued: $data")
                }

                override fun onConfirm(data: String): Boolean {
                    Log.d("bootpay", "confirm: $data")
                    return true
                }

                override fun onDone(data: String) {
                    Log.d("done", data)
                    // 결제가 완료되면 Firebase Storage에서 이미지 URL 가져오기
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        // 이미지 URL을 클라이언트로 전송하는 로직 추가
                        Log.d("TotalPaymentActivity2", "Gifticon image URL: $imageUrl")

                        // 이미지를 표시할 ImageView 찾기
                        val imageViewGifticon: ImageView = findViewById(R.id.imageView_gifticon)
                        // 바코드를 표시할 TextView 찾기
                        val imageViewBarcode: ImageView = findViewById(R.id.imageView_barcode)
                        val textViewNumber: TextView = findViewById(R.id.textView_number)

                        // 이미지를 표시할 ImageView에 이미지 설정
                        Glide.with(this@TotalPaymentActivity2)
                            .load(imageUrl)
                            .into(imageViewGifticon)

                        // 이미지를 보이도록 visibility 변경
                        imageViewGifticon.visibility = View.VISIBLE

                        // 바코드와 숫자 표시
                        imageViewBarcode.visibility = View.VISIBLE
                        textViewNumber.visibility = View.VISIBLE

                        // 여기서 바코드와 숫자를 설정하세요 (예: 랜덤한 숫자 생성)
                        val randomBarcode = "123456789"
                        val randomNumber = "987654321"

                        // 바코드를 이미지로 생성하여 표시
                        val barcodeBitmap = generateBarcode(randomBarcode)
                        imageViewBarcode.setImageBitmap(barcodeBitmap)

                        textViewNumber.text = "Number: $randomNumber"

                        // 결제가 완료되면 이미지 저장
                        saveImageToGallery(imageViewGifticon, "gifticon_", ".jpg")
                    }.addOnFailureListener { e ->
                        Log.e("TotalPaymentActivity2", "Error getting gifticon image URL: ${e.message}")
                    }
                }
            }).requestPayment()
    }

    fun getBootUser(): BootUser? {
        val currentUserEmail = getCurrentUserEmail()
        val user = BootUser()
        user.email = currentUserEmail
        // Add other user details as needed
        return user
    }

    private fun getCurrentUserEmail(): String? {
        val currentUser = auth.currentUser
        return currentUser?.email
    }

    // 바코드를 이미지로 생성하는 함수
    // 바코드를 이미지로 생성하는 함수
    private fun generateBarcode(barcodeData: String): Bitmap? {
        try {
            // 바코드 포맷 및 크기 설정
            val format = BarcodeFormat.CODE_128
            val width = 600
            val height = 200

            // 바코드 생성
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(barcodeData, format, width, height, hints)

            // 비트맵 변환
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            return bmp
        } catch (e: WriterException) {
            Log.e("TotalPaymentActivity2", "Error generating barcode: ${e.message}")
        }
        return null
    }


    // 이미지를 갤러리에 저장
    private fun saveImageToGallery(view: ImageView, prefix: String, suffix: String) {
        // 저장소 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE
            )
            return
        }
        // 이미지뷰의 이미지를 비트맵으로 변환
        val bitmap = view.drawable.toBitmap()
        // 내부 저장소에 이미지 저장
        saveImage(bitmap, prefix, suffix)
    }

    // 비트맵을 내부 저장소에 저장
    private fun saveImage(bitmap: Bitmap, prefix: String, suffix: String) {
        // 이미지를 저장하기 위한 파일명 생성
        val fileName = prefix + System.currentTimeMillis() + suffix

        // 저장할 이미지 파일의 URI 생성
        val contentResolver = contentResolver
        val imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val uri = contentResolver.insert(imageUri, imageDetails)

        // 이미지 파일을 저장
        try {
            contentResolver.openOutputStream(uri!!)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Toast.makeText(this, "이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e("TotalPaymentActivity2", "Error saving image: ${e.message}")
            Toast.makeText(this, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 저장소 권한이 허용된 경우 이미지 저장 처리 진행
                // 이미지를 가져오는 방법에 따라 처리할 내용 추가
                // 예를 들어, 이미지뷰의 이미지를 가져오는 경우:
                val imageViewGifticon: ImageView = findViewById(R.id.imageView_gifticon)
                val drawable = imageViewGifticon.drawable
                val bitmap = drawable.toBitmap()
                saveImage(bitmap, "gifticon_", ".jpg")
            } else {
                Toast.makeText(this, "저장소 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
