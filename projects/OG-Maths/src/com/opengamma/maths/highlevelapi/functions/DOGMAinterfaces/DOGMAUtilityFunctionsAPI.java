/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAinterfaces;

import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArraySuper;

/**
 * DOGMA Utility functions
 */
public interface DOGMAUtilityFunctionsAPI {

  /**
   * Computes the dot product of vectors
   * @param array1 the first array
   * @param array2 the second array
   * @return the dot product array1 and array2, if the inputs are arrays the dot products are calculated along the first dimension greater than 1
   */
  OGArraySuper<Number> dot(OGArraySuper<Number> array1, OGArraySuper<Number> array2);
}