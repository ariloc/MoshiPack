import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8

operator fun Buffer.plusAssign(string: String) {
    this.write(string.decodeHex())
}

val String.hex: String get() = this.encodeUtf8().hex()
