/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.willshex.palette.shared;

/**
 * A set of color-related utility methods, building upon those available in {@code Color}.
 */
public class ColorUtils {

    private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
    private static final int MIN_ALPHA_SEARCH_PRECISION = 10;

    private ColorUtils() {}

    /**
     * Composite two potentially translucent colors over each other and returns the result.
     */
    public static int compositeColors(int foreground, int background) {
        final float alpha1 = Color.alpha(foreground) / 255f;
        final float alpha2 = Color.alpha(background) / 255f;

        float a = (alpha1 + alpha2) * (1f - alpha1);
        float r = (Color.red(foreground) * alpha1)
                + (Color.red(background) * alpha2 * (1f - alpha1));
        float g = (Color.green(foreground) * alpha1)
                + (Color.green(background) * alpha2 * (1f - alpha1));
        float b = (Color.blue(foreground) * alpha1)
                + (Color.blue(background) * alpha2 * (1f - alpha1));

        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    /**
     * Returns the luminance of a color.
     *
     * Formula defined here: http://www.w3.org/TR/2008/REC-WCAG20-20081211/#relativeluminancedef
     */
    public static double calculateLuminance(int color) {
        double red = Color.red(color) / 255d;
        red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);

        double green = Color.green(color) / 255d;
        green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);

        double blue = Color.blue(color) / 255d;
        blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);

        return (0.2126 * red) + (0.7152 * green) + (0.0722 * blue);
    }

    /**
     * Returns the contrast ratio between {@code foreground} and {@code background}.
     * {@code background} must be opaque.
     * <p>
     * Formula defined
     * <a href="http://www.w3.org/TR/2008/REC-WCAG20-20081211/#contrast-ratiodef">here</a>.
     */
    public static double calculateContrast(int foreground, int background) {
        if (Color.alpha(background) != 255) {
            throw new IllegalArgumentException("background can not be translucent");
        }
        if (Color.alpha(foreground) < 255) {
            // If the foreground is translucent, composite the foreground over the background
            foreground = compositeColors(foreground, background);
        }

        final double luminance1 = calculateLuminance(foreground) + 0.05;
        final double luminance2 = calculateLuminance(background) + 0.05;

        // Now return the lighter luminance divided by the darker luminance
        return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
    }

    /**
     * Calculates the minimum alpha value which can be applied to {@code foreground} so that would
     * have a contrast value of at least {@code minContrastRatio} when compared to
     * {@code background}.
     *
     * @param foreground       the foreground color.
     * @param background       the background color. Should be opaque.
     * @param minContrastRatio the minimum contrast ratio.
     * @return the alpha value in the range 0-255, or -1 if no value could be calculated.
     */
    public static int calculateMinimumAlpha(int foreground, int background,
            float minContrastRatio) {
        if (Color.alpha(background) != 255) {
            throw new IllegalArgumentException("background can not be translucent");
        }

        // First lets check that a fully opaque foreground has sufficient contrast
        int testForeground = setAlphaComponent(foreground, 255);
        double testRatio = calculateContrast(testForeground, background);
        if (testRatio < minContrastRatio) {
            // Fully opaque foreground does not have sufficient contrast, return error
            return -1;
        }

        // Binary search to find a value with the minimum value which provides sufficient contrast
        int numIterations = 0;
        int minAlpha = 0;
        int maxAlpha = 255;

        while (numIterations <= MIN_ALPHA_SEARCH_MAX_ITERATIONS &&
                (maxAlpha - minAlpha) > MIN_ALPHA_SEARCH_PRECISION) {
            final int testAlpha = (minAlpha + maxAlpha) / 2;

            testForeground = setAlphaComponent(foreground, testAlpha);
            testRatio = calculateContrast(testForeground, background);

            if (testRatio < minContrastRatio) {
                minAlpha = testAlpha;
            } else {
                maxAlpha = testAlpha;
            }

            numIterations++;
        }

        // Conservatively return the max of the range of possible alphas, which is known to pass.
        return maxAlpha;
    }

    /**
     * Convert RGB components to HSL (hue-saturation-lightness).
     * <ul>
     * <li>hsl[0] is Hue [0 .. 360)</li>
     * <li>hsl[1] is Saturation [0...1]</li>
     * <li>hsl[2] is Lightness [0...1]</li>
     * </ul>
     *
     * @param r   red component value [0..255]
     * @param g   green component value [0..255]
     * @param b   blue component value [0..255]
     * @param hsl 3 element array which holds the resulting HSL components.
     */
    public static void RGBToHSL(int r, int g, int b, float[] hsl) {
        final float rf = r / 255f;
        final float gf = g / 255f;
        final float bf = b / 255f;

        final float max = Math.max(rf, Math.max(gf, bf));
        final float min = Math.min(rf, Math.min(gf, bf));
        final float deltaMaxMin = max - min;

        float h, s;
        float l = (max + min) / 2f;

        if (max == min) {
            // Monochromatic
            h = s = 0f;
        } else {
            if (max == rf) {
                h = ((gf - bf) / deltaMaxMin) % 6f;
            } else if (max == gf) {
                h = ((bf - rf) / deltaMaxMin) + 2f;
            } else {
                h = ((rf - gf) / deltaMaxMin) + 4f;
            }

            s = deltaMaxMin / (1f - Math.abs(2f * l - 1f));
        }

        hsl[0] = (h * 60f) % 360f;
        hsl[1] = s;
        hsl[2] = l;
    }

    /**
     * Convert the ARGB color to its HSL (hue-saturation-lightness) components.
     * <ul>
     * <li>hsl[0] is Hue [0 .. 360)</li>
     * <li>hsl[1] is Saturation [0...1]</li>
     * <li>hsl[2] is Lightness [0...1]</li>
     * </ul>
     *
     * @param color the ARGB color to convert. The alpha component is ignored.
     * @param hsl 3 element array which holds the resulting HSL components.
     */
    public static void colorToHSL(int color, float[] hsl) {
        RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), hsl);
    }

    /**
     * Convert HSL (hue-saturation-lightness) components to a RGB color.
     * <ul>
     * <li>hsl[0] is Hue [0 .. 360)</li>
     * <li>hsl[1] is Saturation [0...1]</li>
     * <li>hsl[2] is Lightness [0...1]</li>
     * </ul>
     * If hsv values are out of range, they are pinned.
     *
     * @param hsl 3 element array which holds the input HSL components.
     * @return the resulting RGB color
     */
    public static int HSLToColor(float[] hsl) {
        final float h = hsl[0];
        final float s = hsl[1];
        final float l = hsl[2];

        final float c = (1f - Math.abs(2 * l - 1f)) * s;
        final float m = l - 0.5f * c;
        final float x = c * (1f - Math.abs((h / 60f % 2f) - 1f));

        final int hueSegment = (int) h / 60;

        int r = 0, g = 0, b = 0;

        switch (hueSegment) {
            case 0:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * (x + m));
                b = Math.round(255 * m);
                break;
            case 1:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * (c + m));
                b = Math.round(255 * m);
                break;
            case 2:
                r = Math.round(255 * m);
                g = Math.round(255 * (c + m));
                b = Math.round(255 * (x + m));
                break;
            case 3:
                r = Math.round(255 * m);
                g = Math.round(255 * (x + m));
                b = Math.round(255 * (c + m));
                break;
            case 4:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (c + m));
                break;
            case 5:
            case 6:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (x + m));
                break;
        }

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return Color.rgb(r, g, b);
    }

    /**
     * Set the alpha component of {@code color} to be {@code alpha}.
     */
    public static int setAlphaComponent(int color, int alpha) {
        if (alpha < 0 || alpha > 255) {
            throw new IllegalArgumentException("alpha must be between 0 and 255.");
        }
        return (color & 0x00ffffff) | (alpha << 24);
    }

    /**
     * Convert RGB components to HSV (hue-saturation-value).
     * <ul>
     * <li>hsl[0] is Hue [0 .. 360)</li>
     * <li>hsl[1] is Saturation [0...1]</li>
     * <li>hsl[2] is Value [0...1]</li>
     * </ul>
     *
     * @param r   red component value [0..255]
     * @param g   green component value [0..255]
     * @param b   blue component value [0..255]
     * @param hsv 3 element array which holds the resulting HSV components.
     */
    public static void RGBToHSV(int red, int green, int blue, float[] hsv) {
      float min, max, delta;

      min = Math.min(red, Math.min(green, blue));
      max = Math.max(red, Math.max(green, blue));
      hsv[2] = max; // v

      delta = max - min;

      if (max != 0) hsv[1] = delta / max; // s
      else {
        // r = g = b = 0 // s = 0, v is undefined
        hsv[1] = 0;
        hsv[0] = -1;
        return;
      }

      if (red == max) hsv[0] = (green - blue) / delta; // between yellow & magenta
      else if (green == max) hsv[0] = 2 + (blue - red) / delta; // between cyan & yellow
      else hsv[0] = 4 + (red - green) / delta; // between magenta & cyan

      hsv[0] *= 60; // degrees
      if (hsv[0] < 0) hsv[0] += 360;
    }

    /**
     * Convert HSV (hue-saturation-value) components to a RGB color.
     * <ul>
     * <li>hsv[0] is Hue [0 .. 360)</li>
     * <li>hsv[1] is Saturation [0...1]</li>
     * <li>hsv[2] is Value [0...1]</li>
     * </ul>
     * If hsv values are out of range, they are pinned.
     *
     * @param hsv 3 element array which holds the input HSV components.
     * @return the resulting RGB color
     */
    public static int HSVToColor(float[] hsv) {
      hsv[0] = MathUtils.constrain(hsv[0], 0.0f, 1.0f);
      hsv[1] = MathUtils.constrain(hsv[1], 0.0f, 1.0f);
      hsv[2] = MathUtils.constrain(hsv[2], 0.0f, 1.0f);
      
      float red = 0.0f;
      float green = 0.0f;
      float blue = 0.0f;
      
      final float hf = (hsv[0] - (int) hsv[0]) * 6.0f;
      final int ihf = (int) hf;
      final float f = hf - ihf;
      final float pv = hsv[2] * (1.0f - hsv[1]);
      final float qv = hsv[2] * (1.0f - hsv[1] * f);
      final float tv = hsv[2] * (1.0f - hsv[1] * (1.0f - f));

      switch (ihf) {
          case 0:         // Red is the dominant color
              red = hsv[2];
              green = tv;
              blue = pv;
              break;
          case 1:         // Green is the dominant color
              red = qv;
              green = hsv[2];
              blue = pv;
              break;
          case 2:
              red = pv;
              green = hsv[2];
              blue = tv;
              break;
          case 3:         // Blue is the dominant color
              red = pv;
              green = qv;
              blue = hsv[2];
              break;
          case 4:
              red = tv;
              green = pv;
              blue = hsv[2];
              break;
          case 5:         // Red is the dominant color
              red = hsv[2];
              green = pv;
              blue = qv;
              break;
      }

      return 0xFF000000 | (((int) (red * 255.0f)) << 16) |
              (((int) (green * 255.0f)) << 8) | ((int) (blue * 255.0f));
    }

}
