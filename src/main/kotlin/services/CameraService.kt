package services

import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean

class CameraService {
    private var webcam: Webcam? = null
    private val isOpen = AtomicBoolean(false)
    private val initializationLock = Any()

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isOpen.get()) return@withContext true
            
            synchronized(initializationLock) {
                if (isOpen.get()) return@withContext true
                
                webcam = Webcam.getDefault()
                if (webcam != null) {
                    // カメラ解像度を設定（Java 21互換）
                    webcam?.setViewSize(Dimension(640, 480))
                    webcam?.open()
                    isOpen.set(true)
                    println("カメラ初期化成功: ${webcam?.name}")
                    true
                } else {
                    println("カメラが見つかりませんでした")
                    false
                }
            }
        } catch (e: Exception) {
            println("カメラの初期化に失敗: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun captureFrame(): BufferedImage? = withContext(Dispatchers.IO) {
        try {
            if (!isOpen.get()) {
                val initialized = initialize()
                if (!initialized) return@withContext null
            }
            webcam?.image
        } catch (e: Exception) {
            println("フレームキャプチャエラー: ${e.message}")
            null
        }
    }

    fun close() {
        try {
            if (isOpen.compareAndSet(true, false)) {
                webcam?.close()
                webcam = null
                println("カメラをクローズしました")
            }
        } catch (e: Exception) {
            println("カメラクローズエラー: ${e.message}")
            e.printStackTrace()
        }
    }

    fun isAvailable(): Boolean = try {
        Webcam.getDefault() != null
    } catch (e: Exception) {
        false
    }

    fun getWebcamList(): List<String> {
        return try {
            Webcam.getWebcams().map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
