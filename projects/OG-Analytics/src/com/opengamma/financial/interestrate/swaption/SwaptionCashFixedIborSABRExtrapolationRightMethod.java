/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a cash-settled European swaption with SABR model and extrapolation to the right. 
 *  Implemented only for the SABRHaganVolatilityFunction.
 */
public class SwaptionCashFixedIborSABRExtrapolationRightMethod {

  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;
  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();

  /**
   * Constructor from cut-off strike and tail parameter.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public SwaptionCashFixedIborSABRExtrapolationRightMethod(double cutOffStrike, double mu) {
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Computes the present value of a cash-settled European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public double presentValue(final SwaptionCashFixedIbor swaption, SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    ParRateCalculator prc = ParRateCalculator.getInstance();
    AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    double pvbp = SwapFixedIborMethod.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not required.
    double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    double price;
    if (swaption.getStrike() <= _cutOffStrike) { // No extrapolation
      BlackPriceFunction blackFunction = new BlackPriceFunction();
      double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, swaption.getStrike(), forward);
      double discountFactorSettle = sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
      BlackFunctionData dataBlack = new BlackFunctionData(forward, discountFactorSettle * pvbp, volatility);
      Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(swaption);
      price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    } else { // With extrapolation
      DoublesPair expiryMaturity = new DoublesPair(swaption.getTimeToExpiry(), maturity);
      double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
      double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
      double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
      double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
      SABRFormulaData sabrParam = new SABRFormulaData(forward, alpha, beta, nu, rho);
      SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
      price = pvbp * sabrExtrapolation.price(swaption) * (swaption.isLong() ? 1.0 : -1.0);
    }
    return price;
  }

  /**
   * Computes the present value rate sensitivity to rates of a cash-settled European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public PresentValueSensitivity presentValueSensitivity(final SwaptionCashFixedIbor swaption, SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    ParRateCalculator prc = ParRateCalculator.getInstance();
    AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    // Derivative of the forward with respect to the rates.
    PresentValueSensitivity forwardDr = new PresentValueSensitivity(PRSC.visit(swaption.getUnderlyingSwap(), sabrData));
    double pvbp = SwapFixedIborMethod.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Derivative of the annuity with respect to the forward.
    double pvbpDf = SwapFixedIborMethod.getAnnuityCashDerivative(swaption.getUnderlyingSwap(), forward);
    double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    PresentValueSensitivity result = new PresentValueSensitivity();
    DoublesPair expiryMaturity = new DoublesPair(swaption.getTimeToExpiry(), maturity);
    double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    SABRFormulaData sabrParam = new SABRFormulaData(forward, alpha, beta, nu, rho);
    SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    result = forwardDr.multiply(pvbpDf * sabrExtrapolation.price(swaption) + pvbp * sabrExtrapolation.priceDF(swaption));
    if (!swaption.isLong()) {
      result = result.multiply(-1);
    }
    return result;
  }

}
