package com.quantumcommune.verblr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.KeyEvent
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask


class DateTimeUtils {
    private constructor()

    companion object {

        fun nowUTC() : String
        {
            val format: SimpleDateFormat = SimpleDateFormat(StringUtils.UTC_Format)
            format.timeZone = TimeZone.getTimeZone("UTC")

            return format.format(Date())
        }

        /**
         * @return -1 if `utc1` is before `utc2`, 0 if they are equal, and 1 if `utc1` is after `utc2`
         */
        fun compareUTC(utc1 : String, utc2 : String) : Int
        {
            // [dho] adapted from : https://stackoverflow.com/a/37172623 - 18/05/20
            return clamp(utc1.compareTo(utc2), -1, 1)
        }

        fun getYear(utc : String) : Int?
        {
            return try {
                val inputFormat = SimpleDateFormat(StringUtils.UTC_Format)
                val date: Date = inputFormat.parse(utc)

                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone("UTC")
                calendar.time = date

                calendar.get(Calendar.YEAR)
            } catch(err : Exception) {
                null
            }
        }

        // [dho] adapted from : https://stackoverflow.com/questions/23323792/android-days-between-two-dates - 21/06/20
        fun getDaysSince(utc1 : String) : Int?
        {
            return try {
                val inputFormat = SimpleDateFormat(StringUtils.UTC_Format)
                val date: Date = inputFormat.parse(utc1)

                getDaysSince(date)
            } catch(err : Exception) {
                null
            }
        }

        // [dho] adapted from : https://stackoverflow.com/questions/23323792/android-days-between-two-dates - 21/06/20
        fun getDaysSince(date : Date) : Int?
        {
            return try {
                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone("UTC")
                calendar.time = date

                val nowMS = Calendar.getInstance().timeInMillis
                val otherMS = calendar.timeInMillis

                val diff = nowMS - otherMS

                (diff / (24 * 60 * 60 * 1000)).toInt();
            } catch(err : Exception) {
                null
            }
        }
    }
}

class ThreadUtils
{
    private constructor()
    {

    }

    companion object {

        fun ensureMainThreadExec(work : () -> Unit)
        {
            if(isMainThread())
            {
                work()
            }
            else
            {
                mainThreadExec(work)
            }
        }

        fun ensureBGThreadExec(work : () -> Unit)
        {
            if(isMainThread())
            {
                bgThreadExec(work)
            }
            else
            {
                work()
            }
        }

        fun isMainThread() : Boolean
        {
            return Looper.myLooper() == Looper.getMainLooper();
        }

        fun mainThreadExec(work : () -> Unit)
        {
            Handler(Looper.getMainLooper()).post(
                Runnable {
                    work()
                }
            )
        }

        fun bgThreadExec(work : () -> Unit)
        {
            AsyncTask.execute {
                work()
            }
        }
    }
}


class TimeoutTask (val cancel : () -> Unit)

fun setTimeout(work : () -> Unit, delay : Long) : TimeoutTask
{
    val timer = Timer()

    timer.schedule(timerTask {
        // [dho] "When a timer is no longer needed, users should call cancel(),
        // which releases the timer's thread and other resources. Timers not
        // explicitly cancelled may hold resources indefinitely" - 20/05/20
        timer.cancel()

        work()
    }, delay)

    return TimeoutTask(cancel = { timer.cancel() })
}


fun openURL(context : Context, url : String)
{
    // [dho] uses chrome by default if installed - 24/05/20
    // [dho] adapted from : https://stackoverflow.com/a/40835977/300037 - 24/05/20
    val uri = Uri.parse("googlechrome://navigate?url=$url")
    val i = Intent(Intent.ACTION_VIEW, uri)
    if (i.resolveActivity(context.packageManager) == null) {
        i.data = Uri.parse(url)
    }

    context.startActivity(i)
//    val i = Intent(Intent.ACTION_VIEW)
//    i.data = Uri.parse(url)
//    activity.startActivity(i)
}

fun openMailTo(activity : Activity, email : String, subject : String, body : String)
{
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "message/rfc822"// "text/plain"
    intent.putExtra(Intent.EXTRA_EMAIL, email)
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)

    activity.startActivity(Intent.createChooser(intent, "Send Email"))
}


class ImageUtils {
    private constructor()

    companion object {
        fun squareCrop(url : String, dim : Int)
            // [dho] adapted from : https://stackoverflow.com/a/45372633 - 28/06/20
            = Picasso.get().load(url).resize(dim, dim).centerInside().get()

        fun writeBitmapToPNG(filename : String, bmp : Bitmap, overwrite : Boolean = true) : File
        {
            // [dho] adapted from : https://stackoverflow.com/a/6485850 - 28/06/20
            val outputFile = File(filename)

            if(outputFile.exists() && !overwrite)
            {
                return outputFile
            }
            // [dho] ensure exists. Adapted from : https://stackoverflow.com/a/9620718 - 28/06/20
            outputFile.parentFile?.mkdirs()
            outputFile.createNewFile()


            // [dho] adapted from : https://stackoverflow.com/a/3013625 - 28/06/20
            var outStream = FileOutputStream(outputFile)

            bmp.compress(
                Bitmap.CompressFormat.PNG,
                100, // [dho] quality hint, ignored for PNG apparently.. more for JPG - 28/06/20
                outStream
            )

            outStream.flush() // Not really required
            outStream.close() // do not forget to close the stream

            return outputFile
        }
    }
}

class StringUtils {
    private constructor()

    companion object {
        fun ellipsize(input : String, maxLength : Int) : String
        {
            if(input.length > maxLength)
            {
                return "${input.substring(0, maxLength)}..."
            }

            return input
        }

        fun urlEncoded(input : String): String? {
            try {
                return URLEncoder.encode(input, "utf-8")
            }
            catch(err : Exception)
            {
                return null
            }
        }

        // [dho] adapted from : https://stackoverflow.com/a/9225678/300037 - 24/05/20
        fun isValidEmail(email: CharSequence?): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        // [dho] adapted from : https://stackoverflow.com/a/5930532/300037 - 11/06/20
        fun isValidURL(url: CharSequence?): Boolean
        {
            return Patterns.WEB_URL.matcher(url).matches()
        }

        // [dho] adapted from : https://stackoverflow.com/a/28864305/300037 - 17/06/20
        fun toTitleCase(input: String) : String
        {
            var space = true
            val builder = StringBuilder(input)
            val len: Int = builder.length

            for (i in 0 until len)
            {
                val c: Char = builder.get(i)
                if (space) {
                    if (!Character.isWhitespace(c)) {
                        // Convert to title case and switch out of whitespace mode.
                        builder.setCharAt(i, Character.toTitleCase(c))
                        space = false
                    }
                } else if (Character.isWhitespace(c)) {
                    space = true
                } else {
                    builder.setCharAt(i, Character.toLowerCase(c))
                }
            }

            return builder.toString()
        }


        val UTC_Format = "yyyy-MM-dd'T'HH:mm:ss'Z'"//"yyyy-MM-dd'T'HH:mm:ss.SSSZ"

        fun toDateLabel(utc : String, relative : Boolean = false) : String?
        {
            try {
                val inputFormat = SimpleDateFormat(UTC_Format)
                val date: Date = inputFormat.parse(utc)

                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone("UTC")
                calendar.time = date

                val time = calendar.time

                if(relative)
                {
                    val daysSince = DateTimeUtils.getDaysSince(date)

                    if(daysSince != null && daysSince < 7)
                    {
                        // [dho] NOTE forcing `Locale.US` to get FULL day name
                        // otherwise we only get an abbreviated day name eg 'Sun' - 20/06/20
                        return SimpleDateFormat("EEEE", Locale.US).format(time)
                    }
                }

                return SimpleDateFormat("dd MMM yyyy").format(time)
            }
            catch(err : Exception)
            {
                return null
            }
        }

        fun toKCountLabel(input : Int) : String
        {
            return if(input > 1000) {
                val k = input / 1000f;

                "${String.format(Locale.ROOT, "%.1f", k)}k";
            }
            else if(input == 1000)
            {
                "1k";
            }
            else {
                "$input";
            }
        }

        fun toDurationLabel(totalSecs : Float) : String
        {
            val hours = (totalSecs / 3600).toInt();
            val minutes = ((totalSecs % 3600) / 60).toInt();
            val seconds = (totalSecs % 60).toInt();

            return if(hours == 0)
            {
                String.format("%02d:%02d", minutes, seconds);
            }
            else
            {
                String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        }

    }
}




// [dho] adapted from : https://github.com/youlovejohnny/mediaplayer2test/blob/a741363afd236a436a0b53b2cd079d9c5fa02071/app/src/main/java/com/megastar/firebasetest/KeyEventHelper.kt - 12/06/20
object KeyEventHelper {
    fun isEvent(intent: Intent, keyEvent: Int): Boolean {
        return intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)?.keyCode == KeyEvent(
            KeyEvent.ACTION_DOWN,
            keyEvent
        ).keyCode
    }
}

fun uuid() = UUID.randomUUID().toString().toLowerCase(Locale.ROOT)


fun clamp(input : Int, min : Int, max : Int) = if(input < min) min else if(input > max) max else input
fun clamp(input : Float, min : Float, max : Float) = if(input < min) min else if(input > max) max else input