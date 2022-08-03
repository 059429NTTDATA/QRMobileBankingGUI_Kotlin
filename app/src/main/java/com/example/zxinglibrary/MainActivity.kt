package com.example.zxinglibrary

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.zxing.*
import com.google.zxing.client.android.Intents
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import de.taimos.totp.TOTP
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Hex
import org.json.JSONObject
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private val EXTRA_MESSAGE = "com.example.zxinglibrary.MESSAGE"

    private val TAG = "MyFirebaseMsgService"

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var secretKey: String

    private lateinit var fcmToken: String

    // Register the launcher and result handler
    private val barcodeLauncher: ActivityResultLauncher<ScanOptions?> = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {
                Log.d("MainActivity", "Cancelled scan")
                Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_LONG).show()
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Log.d(
                    "MainActivity",
                    "Cancelled scan due to missing camera permission"
                )
                Toast.makeText(
                    this@MainActivity,
                    "Cancelled due to missing camera permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d("MainActivity", "Scanned")
            println("qr text : " + result.contents)
            Toast.makeText(
                this@MainActivity,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private var openGalleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                findViewById<ImageView>(R.id.iv_display_gallery).setImageURI(imageUri)
                val resultContent = imageUri?.let { scanQRImage(it) }
                Toast.makeText(
                    this@MainActivity,
                    "Scanned: $resultContent",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        secretKey = getString(R.string.secretKey)

        setupFingerprint()

        firebaseMessagingIns()

        findViewById<Button>(R.id.button_scanqr).setOnClickListener {
            onButtonScanQrClick()
        }

        findViewById<Button>(R.id.button_gen_qr).setOnClickListener {
            val barcodeUrl = genUrlCode()
            generateQR(barcodeUrl)
        }

        findViewById<Button>(R.id.button_open_gallery).setOnClickListener {
            openGallery()
        }

        findViewById<Button>(R.id.button_pokemon).setOnClickListener {
            gotoPokemon()
        }

        findViewById<Button>(R.id.button_fingerprint).setOnClickListener {
            gotoFingerprint()
        }

        findViewById<Button>(R.id.btn_check_auth_code).setOnClickListener {
            checkAuthCode()
        }

        findViewById<Button>(R.id.button_user_management).setOnClickListener {
            sendUserManagement()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    private fun scanQRImage(uri: Uri): String? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return scanQRImage(bitmap)
    }

    private fun scanQRImage(bMap: Bitmap): String? {
        var contents: String? = null
        val intArray = IntArray(bMap.width * bMap.height)
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source: LuminanceSource =
            RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader: Reader = MultiFormatReader()
        try {
            val result = reader.decode(bitmap)
            contents = result.text
        } catch (ex: Exception) {
            Log.e("scanQRImage", "Error decoding qr code", ex)
        }
        return contents
    }

    private fun firebaseMessagingIns() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            fcmToken = getString(R.string.msg_token_fmt, token)
        })
    }

    private fun setupFingerprint() {
        val intentFingerprint = Intent(this, Fingerprint::class.java).apply {
            putExtra(EXTRA_MESSAGE, "test gotoFingerprint")
        }
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errString != "Cancel") {
                        Toast.makeText(
                            applicationContext,
                            "Authentication error: $errString", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    startActivity(intentFingerprint)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun gotoFingerprint() {
        biometricPrompt.authenticate(promptInfo)
    }

    private fun gotoPokemon() {
        val dialogBuilder = AlertDialog.Builder(this@MainActivity)
        dialogBuilder.setTitle("Androidly Alert")
            .setMessage("Are you sure you want to go to pokemon?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, PokemonGenVIII::class.java).apply {
                    putExtra(EXTRA_MESSAGE, "test gotoPokemon")
                }
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//        startActivityForResult(gallery, pickImage)
        openGalleryResultLauncher.launch(gallery)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK && requestCode == pickImage) {
//            imageUri = data?.data
//            findViewById<ImageView>(R.id.iv_display_gallery).setImageURI(imageUri)
//        }
//    }

    // Launch
    private fun onButtonScanQrClick() {
        barcodeLauncher.launch(getScanOptions())
    }

    private fun getScanOptions(): ScanOptions {
        val options = ScanOptions()
        options.setPrompt("Scan a qr code")
        return options
    }

    private fun generateSecretKey(): String? {
        val random = SecureRandom()
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        val base32 = Base32()
        return base32.encodeToString(bytes)
    }

    private fun checkAuthCode() {
        this.currentFocus?.let { closeKeyBoard(it) }
        val intAuthcode: String = findViewById<EditText>(R.id.int_authcode).text.toString()
        val currentAuthCode: String? = getCurrentAuthCode()
        if (currentAuthCode != null && currentAuthCode.equals(intAuthcode)) {
            Toast.makeText(this@MainActivity, "auth code is correct!!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@MainActivity, "failed!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateQR(content: String?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400)
            val imageViewQrCode: ImageView = findViewById<View>(R.id.iv_qrcode_display) as ImageView
            imageViewQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
        }
    }

    private fun genUrlCode(): String? {
        val email = getString(R.string.myemail)
        val companyName = getString(R.string.devsecops)
//        val secretKey = generateSecretKey()
        return getGoogleAuthenticatorBarCode(secretKey, email, companyName)
    }

    private fun getGoogleAuthenticatorBarCode(
        secretKey: String?,
        account: String,
        issuer: String
    ): String? {
        return try {
            ("otpauth://totp/"
                    + URLEncoder.encode("$issuer:$account", "UTF-8").replace("+", "%20")
                    ) + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                .toString() + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20")
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException(e)
        }
    }

    private fun getCurrentAuthCode(): String? {
        return getTOTPCode(secretKey)
    }

    private fun getCurrentAuthCode(secretKey: String): String? {
        return getTOTPCode(secretKey)
    }

    private fun listenAuthCode() {
        var lastCode: String? = null
        while (true) {
            val code = getTOTPCode(secretKey)
            if (code != lastCode) {
                println(code)
            }
            lastCode = code
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
            }
        }
    }

    private fun getTOTPCode(secretKey: String?): String? {
        val base32 = Base32()
        val bytes = base32.decode(secretKey)
        val hexKey = Hex.encodeHexString(bytes)
        return TOTP.getOTP(hexKey)
    }

    public fun closeKeyBoard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun sendUserManagement() {
        println("fcmToken : $fcmToken")
        val queue = Volley.newRequestQueue(this@MainActivity)
//        Toast.makeText(this@MainActivity, msgToken, Toast.LENGTH_LONG).show()
        val localhost = ConstantData.LOCALHOST_IP
        val url = "http://$localhost:8080/qrmobilebankingapi/userManagement"
        val params = HashMap<String, Any>()
        params["userId"] = "21019"
        params["mobile"] = "0925580677"
        params["email"] = "dreamdayxiii@gmail.com"
        params["notificationToken"] = fcmToken
        val reqBody = JSONObject(params as Map<*, *>?)
        val stringRequest = JsonObjectRequest(Request.Method.POST, url, reqBody, {
            response ->
                println("response : $response")
        }, {
            error ->
                println("error : $error")
        })
        queue.add(stringRequest)
    }

    var mNfcAdapter: NfcAdapter? = null

    private val TARGET_AID = byteArrayOf(
        0x01.toByte(),
        0x02.toByte(),
        0x03.toByte(),
        0x04.toByte(),
        0x05.toByte(),
        0x06.toByte(),
        0x07.toByte(),
        0x08.toByte()
    )

    private val SELECT_CMD = byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte())

    override fun onResume() {
        super.onResume()
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mNfcAdapter != null) {
            mNfcAdapter!!.enableReaderMode(
                this, this,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null
            )
        }
    }

    override fun onPause() {
        super.onPause()
        NfcAdapter.getDefaultAdapter(this).disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val cmdLength: Int = SELECT_CMD.size
        val aidLength: Int = TARGET_AID.size
        val selectCmd = ByteArray(cmdLength + aidLength + 1)
        var responseCmd: ByteArray
        var responseString = ""

        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            try {
                isoDep.connect();
                printResponseBytes(
                    selectCmd,
                    cmdLength,
                    aidLength,
                    isoDep
                )
                printHisotry(isoDep)
                printMaxTransceiveLength(isoDep)
                printId(isoDep)
                printTimeOut(isoDep)
            } catch (ex: Exception) {
                ex.printStackTrace();
            } finally {
                isoDep.close();
            }
        }
    }

    private fun printResponseBytes(
        selectCmd: ByteArray,
        cmdLength: Int,
        aidLength: Int,
        isoDep: IsoDep
    ) {
        var responseString: String = ""
        System.arraycopy(SELECT_CMD, 0, selectCmd, 0, cmdLength);
        selectCmd[cmdLength] = aidLength.toByte()
        System.arraycopy(TARGET_AID, 0, selectCmd, cmdLength + 1, aidLength)
        var responseCmd: ByteArray = isoDep.transceive(selectCmd)

        for (element in responseCmd) {
    //                    responseString += java.lang.String.format("0x%02x ", element)
            responseString += "$element "
        }
        println("responseString: $responseString")
    }

    private fun printHisotry(isoDep: IsoDep) {
        var str = "Historical bytes: "
        for (b in isoDep.historicalBytes) {
            str += String.format("0x%X%n", b)
        }
        println("$str")
    }

    private fun printMaxTransceiveLength(isoDep: IsoDep) {
        var str = "Max transceive length: "
        str += String.format("%S", isoDep.maxTransceiveLength)
        println("$str")
    }

    private fun printId(isoDep: IsoDep) {
        val tag = isoDep.tag
        var str = "Tag ID: "
        for (b in tag.id) {
            str += String.format("%02X:", b)
        }
        println("""
            ${str.substring(0, str.length - 1)}
            
            """.trimIndent())
    }

    private fun printTimeOut(isoDep: IsoDep) {
        var str = "Time out: "
        str += String.format("%S MS\n", isoDep.timeout)
        println("$str")
    }
}