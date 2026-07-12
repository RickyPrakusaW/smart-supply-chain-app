package com.agroSystem.app.data.util

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import javax.net.ssl.SSLSocketFactory

object SmtpMailer {
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = 465
    private const val SENDER_EMAIL = "ricky.prakusa@gmail.com"
    private const val APP_PASSWORD = "sspovdxjtbzmhvpex" // Gmail SMTP App Password

    suspend fun sendOtpEmail(recipientEmail: String, otpCode: String): Boolean = withContext(Dispatchers.IO) {
        val cleanPassword = APP_PASSWORD.replace(" ", "")
        try {
            val factory = SSLSocketFactory.getDefault()
            val socket = factory.createSocket(SMTP_HOST, SMTP_PORT)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()))

            fun readResponse(): String {
                val line = reader.readLine()
                Log.d("SmtpMailer", "Server: $line")
                return line ?: ""
            }

            fun sendCommand(cmd: String) {
                Log.d("SmtpMailer", "Client: $cmd")
                writer.print(cmd + "\r\n")
                writer.flush()
            }

            readResponse() // Greeting 220

            sendCommand("EHLO localhost")
            var line = ""
            do {
                line = readResponse()
            } while (line.startsWith("250-"))

            sendCommand("AUTH LOGIN")
            readResponse() // 334 Username prompt

            val base64User = Base64.encodeToString(SENDER_EMAIL.toByteArray(), Base64.NO_WRAP)
            sendCommand(base64User)
            readResponse() // 334 Password prompt

            val base64Pass = Base64.encodeToString(cleanPassword.toByteArray(), Base64.NO_WRAP)
            sendCommand(base64Pass)
            val authResp = readResponse() // 235 Authentication successful
            if (!authResp.startsWith("235")) {
                Log.e("SmtpMailer", "SMTP Auth Failed: $authResp")
                socket.close()
                return@withContext false
            }

            sendCommand("MAIL FROM:<$SENDER_EMAIL>")
            readResponse() // 250 OK

            sendCommand("RCPT TO:<$recipientEmail>")
            readResponse() // 250 OK

            sendCommand("DATA")
            readResponse() // 354 Start input

            val emailData = """
                From: AgriMitra Security <$SENDER_EMAIL>
                To: $recipientEmail
                Subject: Kode OTP Registrasi AgriMitra
                MIME-Version: 1.0
                Content-Type: text/html; charset=UTF-8

                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 12px; padding: 24px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); background-color: #ffffff;">
                    <div style="text-align: center; margin-bottom: 20px;">
                        <h2 style="color: #475B40; margin: 0;">🌾 AgriMitra</h2>
                        <p style="color: #666; font-size: 14px; margin-top: 4px;">Smart Supply Chain Verification</p>
                    </div>
                    <hr style="border: 0; border-top: 1px solid #eeeeee; margin-bottom: 20px;">
                    <p style="font-size: 16px; color: #333333;">Halo,</p>
                    <p style="font-size: 15px; color: #555555; line-height: 1.5;">Terima kasih telah mendaftar di <strong>AgriMitra</strong>. Gunakan kode verifikasi (OTP) berikut untuk menyelesaikan proses pendaftaran Anda:</p>
                    <div style="background-color: #F5F3EE; border-radius: 8px; padding: 16px; text-align: center; margin: 24px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #d05c3f;">$otpCode</span>
                    </div>
                    <p style="font-size: 13px; color: #888888; line-height: 1.4;">Kode verifikasi ini berlaku selama 10 menit. Jangan bagikan kode ini kepada siapa pun demi keamanan akun Anda.</p>
                    <hr style="border: 0; border-top: 1px solid #eeeeee; margin-top: 24px; margin-bottom: 16px;">
                    <p style="font-size: 11px; color: #aaaaaa; text-align: center; margin: 0;">&copy; 2026 AgriMitra. All Rights Reserved.</p>
                </div>
            """.trimIndent()

            sendCommand(emailData)
            sendCommand(".")
            readResponse() // 250 OK

            sendCommand("QUIT")
            readResponse() // 221 Closing connection

            socket.close()
            return@withContext true
        } catch (e: Exception) {
            Log.e("SmtpMailer", "Failed to send SMTP email", e)
            return@withContext false
        }
    }
}
