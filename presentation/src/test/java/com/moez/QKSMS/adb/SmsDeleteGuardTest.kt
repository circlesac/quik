package dev.octoshrimpy.quik.adb

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SmsDeleteGuardTest {
    @Test
    fun fingerprintMatchesBinderCanonicalJson() {
        val record = SmsFingerprintRecord(
            id = 10,
            address = "발신\"자",
            body = "hello,\n세계\t\u0001",
            date = 1000,
            dateSent = null,
            read = 0,
            status = -1,
            threadId = 20,
            type = 1
        )

        assertEquals(
            "4d7fcb84d3c290b4e9f922a5789f284732a23ed6164040727567da63dc181039",
            SmsDeleteGuard.fingerprint(record)
        )
    }

    @Test
    fun parseFingerprintAcceptsOnlyOneFullSha256() {
        assertEquals(
            "abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd",
            SmsDeleteGuard.parseFingerprint(
                "fingerprint=ABCDEFABCDEFABCDEFABCDEFABCDEFABCDEFABCDEFABCDEFABCDEFABCDEFABCD",
                null
            )
        )

        listOf(
            null,
            "fingerprint=abc",
            "fingerprint='abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd'",
            "id=1",
            "fingerprint=abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd AND id=1"
        ).forEach { selection ->
            assertRejected {
                SmsDeleteGuard.parseFingerprint(selection, null)
            }
        }

        assertRejected {
            SmsDeleteGuard.parseFingerprint(
                "fingerprint=abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd",
                arrayOf("unexpected")
            )
        }
    }

    private fun assertRejected(block: () -> Unit) {
        try {
            block()
            fail("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }
}
