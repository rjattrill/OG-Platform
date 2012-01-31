/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.financial.model.volatility.smile.fitting.sabr.MoneynessPiecewiseSABRSurfaceFitter;
import com.opengamma.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class FittedVolatilitySurfaceBuilderTest extends AnalyticsTestBase {
  private static final StandardSmileSurfaceDataBundle STANDARD_DATA = new StandardSmileSurfaceDataBundle(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7},
      new double[][] {new double[] {1, 2, 3, 4}, new double[] {1, 2, 3, 4}, new double[] {1, 2, 3, 4}, new double[] {1, 2, 3, 4}},
      new double[][] {new double[] {0.1, 0.1, 0.1, 0.1}, new double[] {0.1, 0.1, 0.1, 0.1}, new double[] {0.1, 0.1, 0.1, 0.1}, new double[] {0.1, 0.1, 0.1, 0.1}},
      true,
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR));
  private static final MoneynessPiecewiseSABRSurfaceFitter MONEYNESS_SURFACE_FITTER = new MoneynessPiecewiseSABRSurfaceFitter(true, false, 100);

  @Test
  public void testStandardData() {
    final StandardSmileSurfaceDataBundle data = cycleObject(StandardSmileSurfaceDataBundle.class, STANDARD_DATA);
    assertArrayEquals(STANDARD_DATA.getExpiries(), data.getExpiries(), 0);
    assertTrue(Arrays.deepEquals(STANDARD_DATA.getStrikes(), data.getStrikes()));
    assertTrue(Arrays.deepEquals(STANDARD_DATA.getVolatilities(), data.getVolatilities()));
    assertEquals(STANDARD_DATA.isCallData(), data.isCallData());
    assertCurveEquals(STANDARD_DATA.getForwardCurve().getForwardCurve(), data.getForwardCurve().getForwardCurve());
    assertCurveEquals(STANDARD_DATA.getForwardCurve().getDriftCurve(), data.getForwardCurve().getDriftCurve());
    assertEquals(STANDARD_DATA.getForwardCurve().getSpot(), data.getForwardCurve().getSpot(), 1e-12);
  }

  @Test
  public void testMoneynessSurfaceFitter() {
    final MoneynessPiecewiseSABRSurfaceFitter fitter = cycleObject(MoneynessPiecewiseSABRSurfaceFitter.class, MONEYNESS_SURFACE_FITTER);
    assertEquals(MONEYNESS_SURFACE_FITTER, fitter);
    final BlackVolatilitySurfaceMoneyness surface1 = MONEYNESS_SURFACE_FITTER.getVolatilitySurface(STANDARD_DATA);
    final BlackVolatilitySurfaceMoneyness surface2 = cycleObject(BlackVolatilitySurfaceMoneyness.class, surface1);
    assertEquals(surface1.getForwardCurve().getSpot(), surface2.getForwardCurve().getSpot());
    assertCurveEquals(surface1.getForwardCurve().getForwardCurve(), surface2.getForwardCurve().getForwardCurve());
    assertSurfaceEquals(surface1.getSurface(), surface2.getSurface());
  }

  private void assertCurveEquals(final Curve<Double, Double> c1, final Curve<Double, Double> c2) {
    if (c1 != c2) {
      for (double x = 0.1; x < 100.0; x += 5.00000001) {
        assertEquals(c1.getYValue(x), c2.getYValue(x));
      }
    }
  }

  private void assertSurfaceEquals(final Surface<Double, Double, Double> s1, final Surface<Double, Double, Double> s2) {
    if (s1 != s2) {
      for (double x = 0.1; x < 100.0; x += 5.00000001) {
        for (double y = 0.1; y < 100.0; y += 5.00000001) {
          assertEquals(s1.getZValue(x, y), s2.getZValue(x, y));
        }
      }
    }
  }
}
