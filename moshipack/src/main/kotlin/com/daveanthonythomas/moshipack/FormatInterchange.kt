package com.daveanthonythomas.moshipack

import com.squareup.moshi.*
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource

class FormatInterchange(val formatIn: Format, val formatOut: Format) {

    private fun transform(reader: JsonReader, writer: JsonWriter) {
        doValue(reader, writer)
        writer.close()
        reader.close()
    }

    private fun doValue(reader: JsonReader, writer: JsonWriter) {
        when(reader.peek()) {
            JsonReader.Token.BEGIN_ARRAY -> doArray(reader, writer)
            JsonReader.Token.BEGIN_OBJECT -> doObject(reader, writer)
            JsonReader.Token.STRING -> writer.value(reader.nextString())
            JsonReader.Token.NUMBER -> writer.value(reader.nextDouble())
            JsonReader.Token.BOOLEAN -> writer.value(reader.nextBoolean())
            JsonReader.Token.NULL -> writer.nullValue().also { reader.nextNull<Any>() }
            else -> throw IllegalStateException("MessagePack shouldn't need this token")
        }
    }

    private fun doObject(reader: JsonReader, writer: JsonWriter) {
        reader.beginObject()
        writer.beginObject()
        while (reader.hasNext()) {
            writer.name(reader.nextName())
            doValue(reader, writer)
        }
        reader.endObject()
        writer.endObject()
    }

    private fun doArray(reader: JsonReader, writer: JsonWriter) {
        reader.beginArray()
        writer.beginArray()
        while (reader.hasNext()) {
            doValue(reader, writer)
        }
    }

    private fun endArray(reader: JsonReader, writer: JsonWriter) {
        reader.endArray()
        writer.endArray()
    }

    fun transform(source: BufferedSource) = Buffer().also {
        transform(formatIn.reader(source), formatOut.writer(it))
    }
}

sealed class Format {
    class Json: Format() {
        override fun reader(source: BufferedSource) = JsonReader.of(source).apply { isLenient = true }
        override fun writer(sink: BufferedSink) = JsonWriter.of(sink)
    }
    class Msgpack(val writerOptions: MsgpackWriterOptions = MsgpackWriterOptions()): Format() {
        override fun reader(source: BufferedSource) = MsgpackReader(source)
        override fun writer(sink: BufferedSink) = MsgpackWriter(sink).apply { writerOptions }
    }

    abstract fun reader(source: BufferedSource): JsonReader
    abstract fun writer(sink: BufferedSink): JsonWriter
}
