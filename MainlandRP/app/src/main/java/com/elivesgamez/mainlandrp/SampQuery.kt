package com.elivesgamez.mainlandrp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Minimal client for the public SA-MP server query protocol (documented at
 * open-mp / SA-MP wiki). Sends an "i" (info) query and parses hostname,
 * gamemode, player count and max players. No game connection is made here.
 */
object SampQuery {

    data class ServerInfo(
        val online: Boolean,
        val hostname: String = "",
        val gamemode: String = "",
        val players: Int = 0,
        val maxPlayers: Int = 0,
        val passworded: Boolean = false
    )

    suspend fun fetchInfo(host: String, port: Int, timeoutMs: Int = 2500): ServerInfo =
        withContext(Dispatchers.IO) {
            try {
                val address = InetAddress.getByName(host)
                val socket = DatagramSocket().apply { soTimeout = timeoutMs }

                val packetOut = buildPacket(address, port, 'i')
                socket.send(DatagramPacket(packetOut, packetOut.size, address, port))

                val buffer = ByteArray(2048)
                val response = DatagramPacket(buffer, buffer.size)
                socket.receive(response)
                socket.close()

                parseInfo(response.data, response.length)
            } catch (e: Exception) {
                ServerInfo(online = false)
            }
        }

    private fun buildPacket(address: InetAddress, port: Int, opcode: Char): ByteArray {
        val out = ByteArrayOutputStream()
        out.write("SAMP".toByteArray())
        out.write(address.address) // 4 bytes IPv4
        out.write(port and 0xFF)
        out.write((port shr 8) and 0xFF)
        out.write(opcode.code)
        return out.toByteArray()
    }

    private fun parseInfo(data: ByteArray, length: Int): ServerInfo {
        // Header: "SAMP" + 4 byte ip + 2 byte port + 1 byte opcode = 11 bytes
        val buf = ByteBuffer.wrap(data, 0, length).order(ByteOrder.LITTLE_ENDIAN)
        buf.position(11)

        val passworded = buf.get().toInt() != 0
        val players = buf.short.toInt() and 0xFFFF
        val maxPlayers = buf.short.toInt() and 0xFFFF

        val hostnameLen = buf.int
        val hostnameBytes = ByteArray(hostnameLen)
        buf.get(hostnameBytes)
        val hostname = String(hostnameBytes, Charsets.UTF_8)

        val gamemodeLen = buf.int
        val gamemodeBytes = ByteArray(gamemodeLen)
        buf.get(gamemodeBytes)
        val gamemode = String(gamemodeBytes, Charsets.UTF_8)

        return ServerInfo(
            online = true,
            hostname = hostname,
            gamemode = gamemode,
            players = players,
            maxPlayers = maxPlayers,
            passworded = passworded
        )
    }
}
