package com.ssomar.score.screen.internal.display;

/**
 * Pure maths of the FLOATING and PINNED screens (no Bukkit, unit tested).
 * <p>
 * Bukkit conventions: yaw 0 looks along +Z and grows to the right, pitch +90 looks down.
 * Panel coordinates are centre-origin, x to the right and y up, in panel units: one unit is one block seen from one
 * block away. Layout coordinates ({@link com.ssomar.score.screen.ScreenLayout}) are top-left origin, v down.
 */
public final class ScreenGeometry {

    private ScreenGeometry() {
    }

    /** unit look vector */
    public static double[] dir(double yawDeg, double pitchDeg) {
        double y = Math.toRadians(yawDeg), p = Math.toRadians(pitchDeg);
        return new double[]{-Math.sin(y) * Math.cos(p), -Math.sin(p), Math.cos(y) * Math.cos(p)};
    }

    /** unit vector to the right of the view (horizontal) */
    public static double[] right(double yawDeg, double pitchDeg) {
        double[] r = cross(dir(yawDeg, pitchDeg), new double[]{0, 1, 0});
        double l = len(r);
        if (l < 1e-9) {
            double y = Math.toRadians(yawDeg);
            return new double[]{-Math.cos(y), 0, -Math.sin(y)};
        }
        return scale(r, 1 / l);
    }

    /** unit vector up in the view */
    public static double[] up(double yawDeg, double pitchDeg) {
        return cross(right(yawDeg, pitchDeg), dir(yawDeg, pitchDeg));
    }

    /**
     * FLOATING: where the crosshair of a player looking at (yaw, pitch) meets a panel facing the anchor direction.
     *
     * @param unit size of one panel unit on a plane at distance 1 (1 = same angle as PINNED)
     * @return {x, y} in panel units, or null when the player looks away from the panel
     */
    public static double[] floatingPoint(double anchorYaw, double anchorPitch, double yaw, double pitch, double unit) {
        double[] n = dir(anchorYaw, anchorPitch);
        double[] r = dir(yaw, pitch);
        double facing = dot(r, n);
        if (facing <= 1e-6) return null;
        double[] hit = scale(r, 1 / facing);
        return new double[]{dot(hit, right(anchorYaw, anchorPitch)) / unit, dot(hit, up(anchorYaw, anchorPitch)) / unit};
    }

    /**
     * PINNED: panel point under the cursor. The cursor is a world point one block away in the anchor direction that the
     * shader mirrors through the view axis, so it moves with the head like a mouse pointer. Same result whatever the
     * client FOV or aspect.
     *
     * @return {x, y} in panel units, or null when the anchor is behind the player
     */
    public static double[] pinnedPoint(double anchorYaw, double anchorPitch, double yaw, double pitch) {
        double[] a = dir(anchorYaw, anchorPitch);
        double depth = dot(a, dir(yaw, pitch));
        if (depth <= 0.01) return null;
        double cx = dot(a, right(yaw, pitch)), cy = dot(a, up(yaw, pitch));
        return new double[]{-cx / depth, -cy / depth};
    }

    /** centre-origin panel point to layout coordinates (top-left origin, v down) */
    public static double[] toLayout(double x, double y, double width, double height) {
        return new double[]{x + width / 2, height / 2 - y};
    }

    /** centre of a layout rectangle in centre-origin panel coordinates */
    public static double[] centre(double u, double v, double w, double h, double width, double height) {
        return new double[]{u + w / 2 - width / 2, height / 2 - (v + h / 2)};
    }

    /** direction (unit) towards a centre-origin panel point seen at distance 1, for a panel facing the anchor */
    public static double[] towards(double anchorYaw, double anchorPitch, double x, double y) {
        double[] n = dir(anchorYaw, anchorPitch), r = right(anchorYaw, anchorPitch), u = up(anchorYaw, anchorPitch);
        double[] d = new double[]{n[0] + x * r[0] + y * u[0], n[1] + x * r[1] + y * u[1], n[2] + x * r[2] + y * u[2]};
        return scale(d, 1 / len(d));
    }

    /* ------------------------------------------------------------ map header (images in PINNED) */

    /** signature of an image tile, in the first 16 pixels of row 0 */
    public static final int MAP_SIGNATURE = 0xB5A3;
    /** map colour bytes: SNOW at full shade (255, 255, 255) = 1, COLOR_BLACK at full shade (25, 25, 25) = 0 */
    public static final byte BIT_ONE = 34, BIT_ZERO = 118;

    /**
     * Row 0 of an image tile: signature, tile centre x and y, half width and half height (panel units, centre-origin),
     * 16 bits each, most significant bit first. Range [-8, 8) for the centre and [0, 16) for the half sizes,
     * resolution 1/4096.
     */
    public static byte[] mapHeader(double cx, double cy, double halfW, double halfH) {
        int[] words = {MAP_SIGNATURE, enc(cx + 8), enc(cy + 8), enc(halfW), enc(halfH)};
        byte[] row = new byte[128];
        for (int i = 0; i < 128; i++) row[i] = BIT_ZERO;
        for (int w = 0; w < words.length; w++)
            for (int b = 0; b < 16; b++) row[w * 16 + b] = ((words[w] >> (15 - b)) & 1) == 1 ? BIT_ONE : BIT_ZERO;
        return row;
    }

    /** inverse of {@link #mapHeader}, for the tests: {cx, cy, halfW, halfH} or null */
    public static double[] readMapHeader(byte[] row) {
        int[] words = new int[5];
        for (int w = 0; w < 5; w++) {
            int v = 0;
            for (int b = 0; b < 16; b++) {
                byte px = row[w * 16 + b];
                if (px != BIT_ONE && px != BIT_ZERO) return null;
                v = (v << 1) | (px == BIT_ONE ? 1 : 0);
            }
            words[w] = v;
        }
        if (words[0] != MAP_SIGNATURE) return null;
        return new double[]{words[1] / 4096.0 - 8, words[2] / 4096.0 - 8, words[3] / 4096.0, words[4] / 4096.0};
    }

    private static int enc(double v) {
        return (int) Math.max(0, Math.min(65535, Math.round(v * 4096)));
    }

    /* ------------------------------------------------------------ vectors */

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    static double[] cross(double[] a, double[] b) {
        return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double[] scale(double[] a, double s) {
        return new double[]{a[0] * s, a[1] * s, a[2] * s};
    }

    static double len(double[] a) {
        return Math.sqrt(dot(a, a));
    }
}
