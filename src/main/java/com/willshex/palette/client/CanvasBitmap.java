/*
 * Copyright 2015 WillShex Limited
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
package com.willshex.palette.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.willshex.palette.shared.Bitmap;
import com.willshex.palette.shared.Color;

public class CanvasBitmap implements Bitmap {

  private Canvas canvas = Canvas.createIfSupported();
  private Image image;

  /**
   * 
   */
  public CanvasBitmap(Image image) {
    this.image = image;
    setupCanvas(image.getWidth(), image.getHeight());
    draw();
  }

  private void setupCanvas(int width, int height) {
    canvas.setSize(Integer.toString(width) + "px", Integer.toString(height)
        + "px");

    canvas.setCoordinateSpaceWidth(width);
    canvas.setCoordinateSpaceHeight(height);
  }

  private void draw() {
    ImageElement imgElem = ImageElement.as(image.getElement());
    canvas.getContext2d().drawImage(imgElem, 0, 0,
        canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
  }

  @Override
  public int getWidth() {
    return canvas.getCoordinateSpaceWidth();
  }

  @Override
  public int getHeight() {
    return canvas.getCoordinateSpaceHeight();
  }

  @Override
  public void getPixels(int[] pixels, int offset, int stride, int x, int y,
      int width, int height) {
    CanvasPixelArray array = canvas.getContext2d()
        .getImageData(0, 0, width, height).getData();
    // copy it to an array
    int dataLength = array.getLength();
    for (int i = 0; i < dataLength; i++) {
      if (i % 4 == 0) {
        pixels[i / 4] = Color.argb(array.get(i + 3), array.get(i),
            array.get(i + 1), array.get(i + 2));
      }
    }
  }

  @Override
  public Bitmap copyScaled(int width, int height) {
    // does not actually copy the image, just scales it and draws on top
    setupCanvas(width, height);
    draw();
    return this;
  }

  @Override
  public void recycle() {
  }

  @Override
  public boolean isRecycled() {
    return false;
  }

}
