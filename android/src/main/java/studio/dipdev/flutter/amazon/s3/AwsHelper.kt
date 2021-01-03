package studio.dipdev.flutter.amazon.s3

import android.content.Context
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.*

class AwsHelper(private val context: Context, private val onUploadCompleteListener: OnUploadCompleteListener, private val BUCKET_NAME: String, private val IDENTITY_POOL_ID: String, private val KEY: String, private val CONTENTDISPOSITION: String) {

    private var transferUtility: TransferUtility

    init {
        val credentialsProvider = CognitoCachingCredentialsProvider(context, IDENTITY_POOL_ID, Regions.AP_SOUTH_1)
        val amazonS3Client = AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_SOUTH_1))
        transferUtility = TransferUtility.builder().s3Client(amazonS3Client).context(context).build()
    }

    private val uploadedUrl: String
        get() = getUploadedUrl(KEY)

    private fun getUploadedUrl(key: String?): String {
        return String.format(Locale.getDefault(), URL_TEMPLATE, BUCKET_NAME, key)
    }

    @Throws(UnsupportedEncodingException::class)
    fun uploadImage(image: File): String {
        val credentialsProvider = CognitoCachingCredentialsProvider(context, IDENTITY_POOL_ID, Regions.AP_SOUTH_1)
        val amazonS3Client = AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_SOUTH_1))
        transferUtility = TransferUtility.builder().s3Client(amazonS3Client).context(context).build()

        val metadata = ObjectMetadata()
        metadata.contentDisposition = CONTENTDISPOSITION
        val transferObserver = transferUtility.upload(BUCKET_NAME, KEY, image, metadata)

        transferObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    onUploadCompleteListener.onUploadComplete(getUploadedUrl(KEY))
                }
                if (state == TransferState.FAILED) {
                    onUploadCompleteListener.onFailed()
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}
            override fun onError(id: Int, ex: Exception) {
                Log.e(TAG, "error in upload id [ " + id + " ] : " + ex.message)
            }
        })
        return uploadedUrl
    }

    @Throws(UnsupportedEncodingException::class)
    fun clean(filePath: String): String {
        return filePath.replace("[^A-Za-z0-9 ]".toRegex(), "")
    }

    interface OnUploadCompleteListener {
        fun onUploadComplete(imageUrl: String)
        fun onFailed()
    }

    companion object {
        private val TAG = AwsHelper::class.java.simpleName
        private const val URL_TEMPLATE = "https://s3.amazonaws.com/%s/%s"
    }
}