/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wentch.redkale.convert.json;

import com.wentch.redkale.util.Utility;
import com.wentch.redkale.util.ObjectPool;
import com.wentch.redkale.convert.Convert;
import java.lang.reflect.Type;

/**
 *
 * @author zhangjx
 */
@SuppressWarnings("unchecked")
public final class JsonConvert extends Convert<JsonReader, JsonWriter> {

    private static final ObjectPool<JsonReader> readerPool = new ObjectPool<>(Integer.getInteger("convert.json.pool.size", 16), JsonReader.class);

    private static final ObjectPool<JsonWriter> writerPool = new ObjectPool<>(Integer.getInteger("convert.json.pool.size", 16), JsonWriter.class);

    protected JsonConvert(JsonFactory factory) {
        super(factory);
    }

    @Override
    public JsonFactory getFactory() {
        return (JsonFactory) factory;
    }

    public <T> T convertFrom(final Type type, final String text) {
        if (text == null) return null;
        return convertFrom(type, Utility.charArray(text));
    }

    public <T> T convertFrom(final Type type, final char[] text) {
        if (text == null) return null;
        return convertFrom(type, text, 0, text.length);
    }

    public <T> T convertFrom(final Type type, final char[] text, int start, int len) {
        if (text == null || type == null) return null;
        final JsonReader in = readerPool.poll();
        in.setText(text, start, len);
        T rs = (T) factory.loadDecoder(type).convertFrom(in);
        readerPool.offer(in);
        return rs;
    }

    public String convertTo(final Type type, Object value) {
        if (type == null) return null;
        if (value == null) return "null";
        final JsonWriter out = writerPool.poll();
        factory.loadEncoder(type).convertTo(out, value);
        String result = out.toString();
        writerPool.offer(out);
        return result;
    }

    public String convertTo(Object value) {
        if (value == null) return "null";
        return convertTo(value.getClass(), value);
    }

    public byte[] convertToUTF8Bytes(Object value) {
        if (value == null) return new byte[]{110, 117, 108, 108};
        return convertToUTF8Bytes(value.getClass(), value);
    }

    public byte[] convertToUTF8Bytes(final Type type, Object value) {
        if (type == null) return null;
        if (value == null) return new byte[]{110, 117, 108, 108};
        final JsonWriter out = writerPool.poll();
        factory.loadEncoder(type).convertTo(out, value);
        byte[] result = out.toUTF8Bytes();
        writerPool.offer(out);
        return result;
    }
}