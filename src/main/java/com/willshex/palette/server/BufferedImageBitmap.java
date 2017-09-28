//
//  BufferedImageBitmap.java
//  palette-gwt
//
//  Created by William Shakour (billy1380) on 28 Sep 2017.
//  Copyright © 2017 WillShex Limited. All rights reserved.
//
package com.willshex.palette.server;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import com.willshex.palette.shared.Bitmap;
import com.willshex.palette.shared.Color;

/**
 * @author William Shakour (billy1380)
 *
 */
public class BufferedImageBitmap implements Bitmap {

  public BufferedImage image;

  public BufferedImageBitmap(BufferedImage image) {
    this.image = image;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.palette.shared.Bitmap#getWidth()
   */
  @Override
  public int getWidth() {
    return image.getWidth();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.palette.shared.Bitmap#getHeight()
   */
  @Override
  public int getHeight() {
    return image.getHeight();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.palette.shared.Bitmap#getPixels(int[], int, int, int,
   * int, int, int)
   */
  @Override
  public void getPixels(int[] pixels, int offset, int stride, int x, int y,
      int width, int height) {
    Raster r = image.getData(new Rectangle(x, y, width, height));
    int[] data = r.getPixels(x, y, width, height, (int[]) null);
    for (int i = 0; i < data.length; i += 4) {
      pixels[i / 4] = Color.argb(data[i + 3], data[i], data[i + 1],
          data[i + 2]);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.palette.shared.Bitmap#copyScaled(int, int)
   */
  @Override
  public Bitmap copyScaled(int width, int height) {
    return new BufferedImageBitmap(toBufferedImage(
        image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.palette.shared.Bitmap#recycle()
   */
  @Override
  public void recycle() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.willshex.palette.shared.Bitmap#isRecycled()
   */
  @Override
  public boolean isRecycled() {
    return false;
  }

  /**
   * Converts a given Image into a BufferedImage
   *
   * @param img
   *          The Image to be converted
   * @return The converted BufferedImage
   */
  public static BufferedImage toBufferedImage(Image img) {
    if (img instanceof BufferedImage)
      return (BufferedImage) img;

    // Create a buffered image with transparency
    BufferedImage bimage = new BufferedImage(img.getWidth(null),
        img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

    // Draw the image on to the buffered image
    Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();

    // Return the buffered image
    return bimage;
  }

}
