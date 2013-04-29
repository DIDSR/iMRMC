/*
 * ErrorFunction.java
 * 
 * 
 * @Author Robert Sedgewick and Kevin Wayne
 * 
 * This software and documentation (the "Software") were developed at the Food and Drug Administration (FDA) 
 * by employees of the Federal Government in the course of their official duties. Pursuant to Title 17, Section 
 * 105 of the United States Code, this work is not subject to copyright protection and is in the public domain. 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of the Software, to deal in the 
 * Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, or sell copies of the Software or derivatives, and to permit persons to whom the 
 * Software is furnished to do so. FDA assumes no responsibility whatsoever for use by other parties of the 
 * Software, its source code, documentation or compiled executables, and makes no guarantees, expressed or 
 * implied, about its quality, reliability, or any other characteristic.   Further, use of this code in no way 
 * implies endorsement by the FDA or confers any advantage in regulatory decisions.  Although this software 
 * can be redistributed and/or modified freely, we ask that any derivative works bear some notice that they 
 * are derived from it, and any modified versions bear some notice that they have been modified.
 *     
 *     Function to compute the Gaussian pdf (probability density function) and the Gaussian cdf (cumulative 
 *     density function). The approximation is accurate to absolute error less than 8 * 10^(-16). 
 *     Reference: Evaluating the Normal Distribution by George Marsaglia. http://www.jstatsoft.org/v11/a04/paper
 *     Taken from http://introcs.cs.princeton.edu/java/22library/Gaussian.java.html
 *     
 *     Implements the Gauss error function.
 *
 *              erf(z) = 2 / sqrt(pi) * integral(exp(-t*t), t = 0..z) 
 */

package mrmc.core;

public class ErrorFunction {

	// fractional error in math formula less than 1.2 * 10 ^ -7.
	// although subject to catastrophic cancellation when z in very close to 0
	// from Chebyshev fitting formula for erf(z) from Numerical Recipes, 6.2
	public static double erf(double z) {
		double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

		// use Horner's method
		double ans = 1
				- t
				* Math.exp(-z
						* z
						- 1.26551223
						+ t
						* (1.00002368 + t
								* (0.37409196 + t
										* (0.09678418 + t
												* (-0.18628806 + t
														* (0.27886807 + t
																* (-1.13520398 + t
																		* (1.48851587 + t
																				* (-0.82215223 + t * (0.17087277))))))))));
		if (z >= 0)
			return ans;
		else
			return -ans;
	}

	// fractional error less than x.xx * 10 ^ -4.
	// Algorithm 26.2.17 in Abromowitz and Stegun, Handbook of Mathematical.
	public static double erf2(double z) {
		double t = 1.0 / (1.0 + 0.47047 * Math.abs(z));
		double poly = t * (0.3480242 + t * (-0.0958798 + t * (0.7478556)));
		double ans = 1.0 - poly * Math.exp(-z * z);
		if (z >= 0)
			return ans;
		else
			return -ans;
	}

	// cumulative normal distribution
	// See Gaussia.java for a better way to compute Phi(z)
	public static double Phi(double z) {
		return 0.5 * (1.0 + erf(z / (Math.sqrt(2.0))));
	}
}