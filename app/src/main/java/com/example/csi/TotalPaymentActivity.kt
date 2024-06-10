package com.example.csi

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

class TotalPaymentActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var storageReference: StorageReference
    private val firestore = FirebaseFirestore.getInstance()
    private var applicationId = "663a3fae19b42d44e97685ba"
    private val REQUEST_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_payment)
        if (BootpayBuildConfig.DEBUG) {
            applicationId = "663a3fae19b42d44e97685ba"
        }
        // Firebase Storage의 레퍼런스 생성
        storageReference = FirebaseStorage.getInstance().reference.child("images/gifticon.png")
    }

    fun PaymentTest(v: View?) {
        val extra = BootExtra()
            .setCardQuota("0,2,3") // 일시불, 2개월, 3개월 할부 허용
        val items: MutableList<BootItem> = ArrayList()
        val item1 = BootItem().setName("마우스").setId("ITEM_CODE_MOUSE").setQty(1).setPrice(500.0)
        val item2 = BootItem().setName("키보드").setId("ITEM_KEYBOARD_MOUSE").setQty(1).setPrice(500.0)
        items.add(item1)
        items.add(item2)
        val payload = Payload()

        payload.setApplicationId(applicationId)
            .setOrderName("부트페이 결제테스트")
            .setOrderId("1234")
            .setPrice(1000.0)
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
                        Log.d("TotalPaymentActivity", "Gifticon image URL: $imageUrl")

                        // 이미지를 표시할 ImageView 찾기
                        val imageViewGifticon: ImageView = findViewById(R.id.imageView_gifticon)
                        val imageViewBarcode: ImageView = findViewById(R.id.imageView_barcode)
                        val textViewNumber: TextView = findViewById(R.id.textView_number)

                        // 이미지를 표시할 ImageView에 이미지 설정
                        Glide.with(this@TotalPaymentActivity)
                            .asBitmap()
                            .load(imageUrl)
                            .into(object : com.bumptech.glide.request.target.SimpleTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                    // 기프티콘 이미지 로드 완료
                                    imageViewGifticon.setImageBitmap(resource)

                                    // 바코드와 숫자 표시
                                    val randomBarcode = "123456789"
                                    val randomNumber = "987654321"

                                    // 바코드를 이미지로 생성하여 표시
                                    val barcodeBitmap = generateBarcode(randomBarcode)
                                    barcodeBitmap?.let {
                                        imageViewBarcode.setImageBitmap(it)
                                        textViewNumber.text = "Number: $randomNumber"

                                        // 기프티콘, 바코드, 넘버 이미지를 하나로 결합
                                        val combinedBitmap = combineImages(resource, it, randomNumber)

                                        // 결제가 완료되면 바코드 이미지 저장 알림
                                        showSaveConfirmationDialog(combinedBitmap, randomBarcode)
                                    } ?: run {
                                        Toast.makeText(this@TotalPaymentActivity, "바코드 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                    }.addOnFailureListener { e ->
                        Log.e("TotalPaymentActivity", "Error getting gifticon image URL: ${e.message}")
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
            Log.e("TotalPaymentActivity", "Error generating barcode: ${e.message}")
        }
        return null
    }

    // 기프티콘 이미지, 바코드 이미지, 숫자를 하나의 이미지로 합치는 함수
    private fun combineImages(gifticon: Bitmap, barcode: Bitmap, number: String): Bitmap {
        val width = maxOf(gifticon.width, barcode.width)
        val height = gifticon.height + barcode.height + 50 // 숫자를 위한 추가 공간

        val combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combinedBitmap)
        canvas.drawColor(Color.WHITE) // 배경색 설정

        // 기프티콘 이미지 그리기
        canvas.drawBitmap(gifticon, 0f, 0f, null)

        // 바코드 이미지 그리기
        canvas.drawBitmap(barcode, 0f, gifticon.height.toFloat(), null)

        // 숫자 그리기
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER

        val xPos = (canvas.width / 2).toFloat()
        val yPos = (gifticon.height + barcode.height + 40).toFloat()
        canvas.drawText(number, xPos, yPos, paint)

        return combinedBitmap
    }

    // 바코드 이미지 저장 확인 알림을 표시하는 함수
    private fun showSaveConfirmationDialog(bitmap: Bitmap, barcode: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("바코드 저장")
            .setMessage("바코드를 저장하시겠습니까?")
            .setPositiveButton("확인") { dialog, which ->
                val savedUri = saveImageToGallery(bitmap, "barcode_", ".jpg")
                savedUri?.let {
                    saveBarcodeToFirestore(barcode, it)
                    Toast.makeText(this, "바코드가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, "바코드 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소") { dialog, which ->
                Toast.makeText(this, "바코드 저장이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // 바코드와 사용자 정보를 Firestore에 저장
    private fun saveBarcodeToFirestore(barcode: String, uri: Uri) {
        val user = auth.currentUser
        user?.let {
            val userEmail = it.email
            val barcodeData = hashMapOf(
                "barcode" to barcode,
                "uri" to uri.toString(),
                "user" to userEmail
            )
            firestore.collection("barcodes").document(userEmail!!)
                .set(barcodeData)
                .addOnSuccessListener {
                    Log.d("TotalPaymentActivity", "Barcode data saved to Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("TotalPaymentActivity", "Error saving barcode data to Firestore: ${e.message}")
                }
        }
    }

    // 비트맵을 갤러리에 저장
    private fun saveImageToGallery(bitmap: Bitmap, prefix: String, suffix: String): Uri? {
        val fileName = prefix + System.currentTimeMillis() + suffix
        val contentResolver = contentResolver
        val imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val uri = contentResolver.insert(imageUri, imageDetails)
        try {
            contentResolver.openOutputStream(uri!!)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            return uri
        } catch (e: IOException) {
            Log.e("TotalPaymentActivity", "Error saving image: ${e.message}")
        }
        return null
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
                val imageViewBarcode: ImageView = findViewById(R.id.imageView_barcode)
                val drawable = imageViewBarcode.drawable
                val bitmap = drawable.toBitmap()
                val savedUri = saveImageToGallery(bitmap, "barcode_", ".jpg")
                savedUri?.let {
                    saveBarcodeToFirestore("sample_barcode", it)
                    Toast.makeText(this, "바코드가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, "바코드 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "저장소 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
