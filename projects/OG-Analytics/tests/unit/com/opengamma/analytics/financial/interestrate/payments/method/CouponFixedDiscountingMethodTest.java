/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueParallelCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.surface.StringValue;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the methods related to fixed coupons.
 */
public class CouponFixedDiscountingMethodTest {

  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("TARGET");

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final GeneratorDeposit DEPOSIT_EUR = new EURDeposit(EUR_CALENDAR);

  private static final Period START_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, START_PERIOD, DEPOSIT_EUR.getBusinessDayConvention(), EUR_CALENDAR, DEPOSIT_EUR.isEndOfMonth());
  private static final Period CPN_TENOR = Period.ofMonths(12);
  private static final double NOTIONAL = 100000000;
  private static final double FIXED_RATE = 0.0250;
  private static final CouponFixedDefinition CPN_DEFINITION = CouponFixedDefinition.from(START_DATE, CPN_TENOR, DEPOSIT_EUR, NOTIONAL, FIXED_RATE);

  private static final YieldCurveBundle YC_BUNDLE = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVES_NAME = TestsDataSetsSABR.curves2Names();

  private static final CouponFixed CPN = CPN_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final CouponFixedDiscountingMethod METHOD = CouponFixedDiscountingMethod.getInstance();
  private static final PresentValueParallelCurveSensitivityCalculator PVPCSC = PresentValueParallelCurveSensitivityCalculator.getInstance();

  @Test
  /**
   * Tests the present value of fixed coupons.
   */
  public void presentValue() {
    CurrencyAmount pvComputed = METHOD.presentValue(CPN, YC_BUNDLE);
    double pvExpected = CPN.getAmount() * YC_BUNDLE.getCurve(CURVES_NAME[0]).getDiscountFactor(CPN.getPaymentTime());
    assertEquals("CouponFixed: Present value by discounting", pvExpected, pvComputed.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed coupons.
   */
  public void presentValueParallelCurveSensitivity() {
    StringValue pvpcsComputed = METHOD.presentValueParallelCurveSensitivity(CPN, YC_BUNDLE);
    double pvpcsExpected = -CPN.getPaymentTime() * CPN.getAmount() * YC_BUNDLE.getCurve(CURVES_NAME[0]).getDiscountFactor(CPN.getPaymentTime());
    assertEquals("CouponFixed: Present value parallel curve sensitivity by discounting", 1, pvpcsComputed.getMap().size());
    assertEquals("CouponFixed: Present value parallel curve sensitivity by discounting", pvpcsExpected, pvpcsComputed.getMap().get(CURVES_NAME[0]), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed coupons.
   */
  public void presentValueParallelCurveSensitivityMethodVsCalculator() {
    StringValue pvpcsMethod = METHOD.presentValueParallelCurveSensitivity(CPN, YC_BUNDLE);
    StringValue pvpcsCalculator = PVPCSC.visit(CPN, YC_BUNDLE);
    assertTrue("CouponFixed: Present value parallel curve sensitivity by discounting", StringValue.compare(pvpcsMethod, pvpcsCalculator, 1.0E-5));
  }

}
