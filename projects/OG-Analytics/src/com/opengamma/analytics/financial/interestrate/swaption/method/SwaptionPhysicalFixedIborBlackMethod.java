/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with SABR model.
 */
public final class SwaptionPhysicalFixedIborBlackMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod INSTANCE = new SwaptionPhysicalFixedIborBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionPhysicalFixedIborBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedIborBlackMethod() {
  }

  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();
  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  /**
   * The swap method.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    Validate.notNull(swaption, "Swaption");
    Validate.notNull(curveBlack, "Curves with Black volatility");
    Validate.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency() == swaption.getCurrency(), "Black data currency should be equal to swaption currency");
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), curveBlack);
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), curveBlack.getBlackParameters().getGeneratorSwap().getFixedLegDayCount(), curveBlack);
    final double forward = PRC.visit(swaption.getUnderlyingSwap(), curveBlack);
    final double forwardModified = forward * pvbp / pvbpModified;
    final double strikeModified = SwapFixedCouponDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, curveBlack);
    final double tenor = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    Validate.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Bundle should contain Black Swaption data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (YieldCurveWithBlackSwaptionBundle) curves);
  }

  /**
   * Computes the implied Black volatility of the vanilla swaption.
   * @param swaption The swaption.
   * @param curves The yield curve bundle.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Yield curve bundle should contain Black swaption data");
    final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
    Validate.notNull(swaption, "Forex option");
    final double tenor = swaption.getMaturityTime();
    final double volatility = curvesBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    return volatility;
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    Validate.notNull(swaption, "Swaption");
    Validate.notNull(curveBlack, "Curves with Black volatility");
    Validate.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency() == swaption.getCurrency(), "Black data currency should be equal to swaption currency");
    final double forward = PRC.visit(swaption.getUnderlyingSwap(), curveBlack);
    // Derivative of the forward with respect to the rates.
    final InterestRateCurveSensitivity forwardDr = new InterestRateCurveSensitivity(PRSC.visit(swaption.getUnderlyingSwap(), curveBlack));
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), curveBlack);
    // Derivative of the PVBP with respect to the rates.
    final InterestRateCurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), curveBlack);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final double strike = SwapFixedCouponDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, curveBlack);
    final double tenor = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    InterestRateCurveSensitivity result = pvbpDr.multiply(bsAdjoint[0]);
    result = result.plus(forwardDr.multiply(pvbp * bsAdjoint[1]));
    if (!swaption.isLong()) {
      result = result.multiply(-1);
    }
    return result;
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a physical delivery European swaption in the Black swaption model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueBlackSwaptionSensitivity presentValueBlackSensitivity(final SwaptionPhysicalFixedIbor swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    Validate.notNull(swaption, "Swaption");
    Validate.notNull(curveBlack, "Curves with Black volatility");
    Validate.isTrue(curveBlack.getBlackParameters().getGeneratorSwap().getCurrency() == swaption.getCurrency(), "Black data currency should be equal to swaption currency");
    final double forward = PRC.visit(swaption.getUnderlyingSwap(), curveBlack);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), curveBlack);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final double strike = SwapFixedCouponDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, curveBlack);
    final double tenor = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    DoublesPair point = new DoublesPair(swaption.getTimeToExpiry(), tenor);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = curveBlack.getBlackParameters().getVolatility(point);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    Map<DoublesPair, Double> sensitivity = new HashMap<DoublesPair, Double>();
    sensitivity.put(point, bsAdjoint[2] * pvbp * (swaption.isLong() ? 1.0 : -1.0));
    return new PresentValueBlackSwaptionSensitivity(sensitivity, curveBlack.getBlackParameters().getGeneratorSwap());
  }

}
