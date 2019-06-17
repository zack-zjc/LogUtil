package com.realcloud.loochadroid.util

import android.util.Log
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue
import java.util.zip.GZIPOutputStream

/**
 * author:zack
 * Date:2019/6/14
 * Description:日志调试
 */
object LogUtil {

  enum class LogLevel{
    VERBOSE,
    INFO,
    WARN,
    DEBUG,
    ERROR,
  }

  //展示log的标签
  private const val SHOW_LOG = false

  //打印日志级别
  private var logLevel = LogLevel.VERBOSE

  //日志中的时间显示格式
  private val logTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

  //日志队列
  private var mLogBlockingQueue = LinkedBlockingQueue<Pair<String,String>>()

  init {
    LogDumper().start()
  }

  /**
   * 设置显示日志等级
   */
  fun setLogLevel(level:LogLevel){
    this.logLevel = level
  }

  /**
   * 连接多个数据
   */
  private fun contactMessage(vararg messages: Any):String{
    val stringBuilder = StringBuilder()
    for (msg in messages){
      stringBuilder.append("${JsonUtil.toString(msg)}\n")
    }
    return stringBuilder.toString()
  }

  /**
   * 获取当前调用文件类名
   */
  private fun getCurrentClassTag():String{
    val stackElement = Thread.currentThread().stackTrace
    stackElement?.let {
      for (element in it) {
        if (element == null || element.isNativeMethod || element.className == Thread::class.java.name ||
            element.className == javaClass.name) {
          continue
        }
        return Class.forName(element.className).simpleName
      }
    }
    return javaClass.name
  }

  /**
   * 获取当前函数的信息
   */
  private fun getLogHeaderInfo(): String {
    val stackElement = Thread.currentThread().stackTrace
    stackElement?.let {
      for (element in it) {
        if (element == null || element.isNativeMethod || element.className == Thread::class.java.name ||
            element.className == LogUtil::class.java.name) {
          continue
        }
        return "[${logTimeFormat.format(Date())}-${element.className}-${element.methodName}-Line:${element.lineNumber}]"
      }
    }
    return "[${logTimeFormat.format(Date())}]"
  }

  /**
   * verbose日志
   */
  fun v(vararg messages: Any) {
    if (SHOW_LOG){
      if (logLevel.ordinal >= LogLevel.DEBUG.ordinal){
        Log.v(getCurrentClassTag(),contactMessage(messages))
      }
    }
  }

  /**
   * warning日志
   */
  fun w(vararg messages: Any) {
    if (SHOW_LOG){
      if (logLevel.ordinal >= LogLevel.DEBUG.ordinal){
        Log.w(getCurrentClassTag(),contactMessage(messages))
      }
    }
  }

  /**
   * debug日志
   */
  fun d(vararg messages: Any) {
    if (SHOW_LOG){
      if (logLevel.ordinal >= LogLevel.DEBUG.ordinal){
        Log.d(getCurrentClassTag(),contactMessage(messages))
      }
    }
  }

  /**
   * error日志
   */
  fun e(vararg messages: Any) {
    if (SHOW_LOG){
      if (logLevel.ordinal >= LogLevel.DEBUG.ordinal){
        Log.e(getCurrentClassTag(),contactMessage(messages))
      }
    }
  }

  /**
   * 打印日志到文件
   */
  fun logToFile(fileName: String,vararg messages: Any) {
    val logStr = "${getLogHeaderInfo()}----->${contactMessage(messages)}"
    e(messages)
    mLogBlockingQueue.add(Pair(fileName,logStr))
  }

  /**
   * 打印日志到文件
   */
  fun logToFile(vararg messages: Any) {
    val logStr = "${getLogHeaderInfo()}----->${contactMessage(messages)}"
    e(messages)
    mLogBlockingQueue.add(Pair("campus_log",logStr))
  }

  /**
   * 日志打印线程
   */
  class LogDumper : Thread() {

    override fun run() {
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
      while (true){
        try {
          val pairInfo = mLogBlockingQueue.take()
          if (pairInfo != null){
            val fileName = pairInfo.first
            val logInfo = pairInfo.second
            val logFile = FileUtil.createFile("sdcard/log/$fileName.txt")
            var gzipOutputStream: GZIPOutputStream? = null
            try {
              gzipOutputStream = GZIPOutputStream(FileOutputStream(logFile, true))
              gzipOutputStream.write(logInfo.toByteArray())
              gzipOutputStream.flush()
            } catch (e: Exception) {
              e.printStackTrace()
            } finally {
              gzipOutputStream?.close()
            }
          }
        }catch (e:Exception){
          e.printStackTrace()
        }
      }
    }

  }

}