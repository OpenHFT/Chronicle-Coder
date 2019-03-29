package net.openhft.chronicle.coder;

import sun.misc.Unsafe;

import java.util.Objects;

import static net.openhft.chronicle.coder.impl.UnsafeUtil.UNSAFE;

public interface Coder {
    default long parseLong(CharSequence cs) {
        return parseLong(cs, 0, cs.length());
    }

    long parseLong(CharSequence cs, int offset, int length);

    default int parseInt(CharSequence cs) {
        return parseInt(cs, 0, cs.length());
    }

    default int parseInt(CharSequence cs, int offset, int length) {
        return Math.toIntExact(parseLong(cs, offset, length));
    }

    byte[] parseBytes(CharSequence cs);

    default long parseNative(CharSequence cs, long address, int maxLength) {
        byte[] bytes = parseBytes(cs);
        if (bytes.length > maxLength)
            throw new IllegalStateException("Input too long");
        UNSAFE.copyMemory(bytes, Unsafe.ARRAY_BYTE_BASE_OFFSET, null, address, bytes.length);
        return address;
    }

    default LatLon parseLatLon(CharSequence cs) {
        return parseLatLon(cs, 0, cs.length());
    }

    default LatLon parseLatLon(CharSequence cs, int offset, int length) {
        LatLon latLon = parseNormalisedLatLon(cs, offset, length);
        return new LatLon(latLon.latitude * Math.nextUp(90),
                latLon.longitude * Math.nextUp(180),
                latLon.precision * 180);
    }

    LatLon parseNormalisedLatLon(CharSequence cs, int offset, int length);

    default void appendInt(StringBuilder sb, int value) {
        appendLong(sb, signed() ? (long) value : (value & 0xFFFFFFFFL));
    }

    void appendLong(StringBuilder sb, long value);

    void appendBytes(StringBuilder sb, byte[] bytes);

    default String asString(long value) {
        StringBuilder sb = new StringBuilder();
        appendLong(sb, value);
        return sb.toString();
    }

    default void appendNative(StringBuilder sb, long address, int length) {
        byte[] bytes = new byte[length];
        UNSAFE.copyMemory(null, address, bytes, Unsafe.ARRAY_BYTE_BASE_OFFSET, length);
        appendBytes(sb, bytes);
    }

    /**
     * Append a two dimensional code for latitude and longitude to a precision
     *
     * @param latitude  to use
     * @param longitude to use
     * @param precision when to stop writing.
     */
    default void appendLatLon(StringBuilder sb, double latitude, double longitude, double precision) {
        appendNormalisedLatLon(sb, latitude / Math.nextUp(90), longitude / Math.nextUp(180), precision / 180);
    }

    /**
     * Append a two dimensional code for (x, y) where x is (-1, +1) and y is (-1, +1) to a precision
     *
     * @param latitude  to use
     * @param longitude to use
     * @param precision when to stop writing.
     */
    void appendNormalisedLatLon(StringBuilder sb, double latitude, double longitude, double precision);

    default boolean signed() {
        return false;
    }

    class LatLon {
        public final double latitude;
        public final double longitude;
        public final double precision;

        public LatLon(double latitude, double longitude, double precision) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.precision = precision;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LatLon latLon = (LatLon) o;
            return Double.compare(latLon.latitude, latitude) == 0 &&
                    Double.compare(latLon.longitude, longitude) == 0 &&
                    Double.compare(latLon.precision, precision) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(latitude, longitude, precision);
        }

        @Override
        public String toString() {
            return "LatLon{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", precision=" + precision +
                    '}';
        }
    }
}
