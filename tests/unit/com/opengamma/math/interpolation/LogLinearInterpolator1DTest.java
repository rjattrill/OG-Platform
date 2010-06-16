/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class LogLinearInterpolator1DTest {
  private static final Interpolator1D<Interpolator1DModel, InterpolationResult> LINEAR = new LinearInterpolator1D();
  private static final Interpolator1D<Interpolator1DModel, InterpolationResult> INTERPOLATOR = new LogLinearInterpolator1D();
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 2 * x - 7;
    }
  };
  private static final Interpolator1DModel MODEL;
  private static final Interpolator1DModel TRANSFORMED_MODEL;
  private static final double EPS = 1e-9;

  static {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    final Map<Double, Double> transformedData = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = Double.valueOf(i);
      data.put(x, FUNCTION.evaluate(x));
      transformedData.put(x, Math.log(FUNCTION.evaluate(x)));
    }
    MODEL = Interpolator1DModelFactory.fromMap(data);
    TRANSFORMED_MODEL = Interpolator1DModelFactory.fromMap(transformedData);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullModel() {
    INTERPOLATOR.interpolate(null, 3.4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(MODEL, null);
  }

  @Test(expected = InterpolationException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(MODEL, -2.);
  }

  @Test(expected = InterpolationException.class)
  public void testHgihValue() {
    INTERPOLATOR.interpolate(MODEL, 12.);
  }

  @Test
  public void test() {
    assertEquals(Math.exp(INTERPOLATOR.interpolate(MODEL, 3.4).getResult()), LINEAR.interpolate(TRANSFORMED_MODEL, 3.4).getResult(), EPS);
  }
}
