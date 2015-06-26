//
//  Color.java
//  palette-gwt
//
//  Created by William Shakour (billy1380) on 26 Jun 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.palette.shared;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author William Shakour (billy1380)
 *
 */
public class ColorTests {

  @Test
  public void toHtmlColorWithAlphaTest() {
    Assert.assertEquals("#aeaeae", Color.toHtmlColor(0xffaeaeae));
  }

  @Test
  public void toHtmlColorWithOutAlphaTest() {
    Assert.assertEquals("#aeaeae", Color.toHtmlColor(0xaeaeae));
  }

  @Test
  public void toHtmlColorSmallIntegerTest() {
    Assert.assertEquals("#000001", Color.toHtmlColor(0x1));
  }

}
