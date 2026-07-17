package com.litelauncher.app.managers

import android.content.Context
import com.litelauncher.app.bridge.AccountInfo
import com.litelauncher.app.bridge.AccountType
import com.litelauncher.app.bridge.AuthProgress
import java.util.UUID
import java.security.MessageDigest

/**
 * 账户管理器 — MS OAuth + 离线登录
 */
class AccountManager(private val context: Context) {
    private val accounts = mutableListOf<AccountInfo>()
    private var current: AccountInfo? = null

    suspend fun startMSLogin(): AuthProgress {
        // TODO: 实现设备码流程
        // POST https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode
        return AuthProgress(
            userCode = "",
            verificationUri = "https://microsoft.com/link",
            status = "waiting",
            error = null
        )
    }

    suspend fun loginOffline(username: String): AccountInfo {
        val uuid = generateOfflineUUID(username)
        val acc = AccountInfo(
            id = uuid,
            username = username,
            uuid = uuid,
            type = AccountType.OFFLINE,
            skinUrl = null,
            authServer = null
        )
        accounts.add(acc)
        current = acc
        return acc
    }

    suspend fun logout() { current = null }

    suspend fun getCurrent(): AccountInfo = current ?: AccountInfo(
        id = "", username = "", uuid = "", type = AccountType.OFFLINE, skinUrl = null, authServer = null
    )

    suspend fun listAll(): List<AccountInfo> = accounts.toList()

    companion object {
        // 离线 UUID 生成: MD5("OfflinePlayer:" + username) → UUID v3
        fun generateOfflineUUID(username: String): String {
            val input = "OfflinePlayer:$username"
            val md5 = MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
            // Set version to 3
            md5[6] = ((md5[6].toInt() and 0x0F) or 0x30).toByte()
            // Set variant to RFC 4122
            md5[8] = ((md5[8].toInt() and 0x3F) or 0x80).toByte()

            val sb = StringBuilder(36)
            for (i in 0 until 16) {
                if (i == 4 || i == 6 || i == 8 || i == 10) sb.append('-')
                sb.append(String.format("%02x", md5[i]))
            }
            return sb.toString()
        }
    }
}
