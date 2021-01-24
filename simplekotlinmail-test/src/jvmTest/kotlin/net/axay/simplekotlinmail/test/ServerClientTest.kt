package net.axay.simplekotlinmail.test

import net.axay.simplekotlinmail.delivery.MailerManager
import net.axay.simplekotlinmail.delivery.sendSync
import net.axay.simplekotlinmail.email.emailBuilder
import net.axay.simplekotlinmail.server.smtpServer
import net.axay.simplekotlinmail.server.start
import net.axay.simplekotlinmail.server.tls.TLSContext
import net.axay.simplekotlinmail.server.tls.setupTLS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ServerClientTest {

    private val plainText = "Hey, how are you?"

    private val email = emailBuilder {
        from("foo@bar.com")
        to("info@example.org")

        withSubject("This is an important message!")
        withPlainText(plainText)
    }

    @Test
    fun testServer() {

        val smtpServer = smtpServer {
            mailListener {
                println("received email: ${it.email.plainText}")
                assertEquals(it.email.plainText?.trim(), plainText)
            }
        }

        smtpServer.start(keepAlive = true)

        email.sendSync()

        MailerManager.shutdownMailers()

        smtpServer.stop()

    }

    @Test
    fun testServerTLS() {

        println(File("./src/jvmTest/resources/keystore").absoluteFile.absolutePath)

        val plainText = "Hey, how are you?"

        val smtpServer = smtpServer {
            setupTLS(
                TLSContext(
                    File("./src/jvmTest/resources/keystore"), "passphrase",
                    File("./src/jvmTest/resources/truststore"), "passphrase"
                ),
                true,
                requireClientAuth = false
            )

            mailListener {
                println("received email: ${it.email.plainText}")
                assertEquals(it.email.plainText?.trim(), plainText)
            }
        }

        smtpServer.start(keepAlive = true)

        email.sendSync()

        MailerManager.shutdownMailers()

        smtpServer.stop()

    }

}
