package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.RepairJob
import com.example.data.model.ShopSettings
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateReceiptPdf(
        context: Context,
        job: RepairJob,
        settings: ShopSettings
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()
        val headerPaint = Paint()

        // Colors
        val primaryColor = Color.rgb(13, 27, 42) // #0D1B2A
        val accentColor = Color.rgb(0, 119, 182)  // #0077B6
        val darkText = Color.rgb(30, 30, 30)
        val lightGray = Color.rgb(240, 243, 246)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        // Header Background Banner
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 110f, paint)

        // Shop Name Header
        titlePaint.color = Color.WHITE
        titlePaint.textSize = 26f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(settings.shopName, 30f, 45f, titlePaint)

        // Tagline / Subtitle
        headerPaint.color = Color.rgb(200, 225, 250)
        headerPaint.textSize = 12f
        canvas.drawText("PROFESSIONAL MOBILE REPAIR & DIAGNOSTICS", 30f, 65f, headerPaint)
        canvas.drawText("${settings.address} | Ph: ${settings.phone}", 30f, 85f, headerPaint)

        // Invoice / Receipt Badge
        titlePaint.color = accentColor
        titlePaint.textSize = 18f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("REPAIR RECEIPT", 420f, 45f, titlePaint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        canvas.drawText("Job #: ${job.jobTicketNumber}", 420f, 68f, paint)
        canvas.drawText("Date: ${dateFormat.format(Date(job.receivedDate))}", 420f, 85f, paint)

        var y = 135f

        // Customer & Device Box
        paint.color = lightGray
        canvas.drawRoundRect(25f, y, 570f, y + 140f, 8f, 8f, paint)

        val textPaint = Paint()
        textPaint.color = darkText
        textPaint.textSize = 12f

        val boldTextPaint = Paint()
        boldTextPaint.color = darkText
        boldTextPaint.textSize = 12f
        boldTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        // Left Column: Customer Details
        canvas.drawText("CUSTOMER DETAILS:", 40f, y + 25f, boldTextPaint)
        canvas.drawText("Name: ${job.customerName}", 40f, y + 50f, textPaint)
        canvas.drawText("Phone: ${job.customerPhone}", 40f, y + 70f, textPaint)
        if (settings.gstOrRegNumber.isNotBlank()) {
            canvas.drawText("GST/Reg: ${settings.gstOrRegNumber}", 40f, y + 90f, textPaint)
        }

        // Right Column: Device & IMEI
        canvas.drawText("DEVICE INFORMATION:", 310f, y + 25f, boldTextPaint)
        canvas.drawText("Brand/Model: ${job.brand} ${job.deviceModel}", 310f, y + 50f, textPaint)
        canvas.drawText("IMEI / Serial: ${job.imeiNumber.ifEmpty { "N/A" }}", 310f, y + 70f, textPaint)
        canvas.drawText("Status: ${job.jobStatus.replace("_", " ")}", 310f, y + 90f, boldTextPaint)

        y += 165f

        // Problem Description
        canvas.drawText("REPORTED PROBLEM / REPAIR SUMMARY:", 30f, y, boldTextPaint)
        y += 20f

        paint.color = Color.rgb(250, 250, 250)
        canvas.drawRect(25f, y, 570f, y + 50f, paint)
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        canvas.drawRect(25f, y, 570f, y + 50f, paint)
        paint.style = Paint.Style.FILL

        canvas.drawText(job.problemDescription, 35f, y + 30f, textPaint)

        y += 75f

        // Financial Table Header
        paint.color = primaryColor
        canvas.drawRect(25f, y, 570f, y + 30f, paint)

        val whiteBold = Paint()
        whiteBold.color = Color.WHITE
        whiteBold.textSize = 12f
        whiteBold.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("DESCRIPTION", 40f, y + 20f, whiteBold)
        canvas.drawText("AMOUNT (${settings.currencySymbol})", 450f, y + 20f, whiteBold)

        y += 30f

        // Financial Table Rows
        fun drawTableRow(desc: String, amount: String, isBold: Boolean = false) {
            paint.color = Color.rgb(248, 249, 250)
            canvas.drawRect(25f, y, 570f, y + 28f, paint)
            val p = if (isBold) boldTextPaint else textPaint
            canvas.drawText(desc, 40f, y + 18f, p)
            canvas.drawText(amount, 450f, y + 18f, p)
            y += 28f
        }

        drawTableRow("Estimated Repair Cost", "${settings.currencySymbol}${job.estimatedCost}")
        if (job.finalAmount != job.estimatedCost) {
            drawTableRow("Final Agreed Amount", "${settings.currencySymbol}${job.finalAmount}", true)
        }
        drawTableRow("Advance Amount Paid", "${settings.currencySymbol}${job.advancePaid}")
        
        val balance = job.pendingBalance
        val balanceStr = if (balance <= 0) "PAID IN FULL" else "${settings.currencySymbol}$balance"
        drawTableRow("REMAINING BALANCE DUE", balanceStr, true)

        y += 25f

        // Terms & Conditions
        canvas.drawText("TERMS & WARRANTY CONDITIONS:", 30f, y, boldTextPaint)
        y += 18f

        textPaint.textSize = 10f
        val termsLines = settings.termsAndConditions.split("\n")
        for (line in termsLines) {
            if (line.isNotBlank()) {
                canvas.drawText(line, 30f, y, textPaint)
                y += 15f
            }
        }

        y += 30f

        // Signatures
        canvas.drawLine(40f, y + 30f, 200f, y + 30f, textPaint)
        canvas.drawText("Customer Signature", 60f, y + 45f, textPaint)

        canvas.drawLine(380f, y + 30f, 540f, y + 30f, textPaint)
        canvas.drawText("Authorized Signature (${settings.shopName})", 380f, y + 45f, textPaint)

        document.finishPage(page)

        return try {
            val docsDir = context.getExternalFilesDir("Documents")
            if (docsDir?.exists() == false) docsDir.mkdirs()
            val file = File(docsDir, "Receipt_${job.jobTicketNumber}.pdf")
            val os = FileOutputStream(file)
            document.writeTo(os)
            os.close()
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Receipt PDF via"))
    }
}
