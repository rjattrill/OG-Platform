/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import javax.time.InstantProvider;

/**
 * 
 */
public interface CurveCalculationConfigSource {

  MultiCurveCalculationConfig getConfig(final String name);

  MultiCurveCalculationConfig getConfig(final String name, final InstantProvider versionAsOf);
}
