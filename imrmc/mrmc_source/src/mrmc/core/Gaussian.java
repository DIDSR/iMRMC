/*
 * Gaussian.java
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
 */

package mrmc.core;

public class Gaussian {
	// return phi(x) = standard Gaussian pdf
	public static double phi(double x) {
		return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
	}

	// return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
	public static double phi(double x, double mu, double sigma) {
		return phi((x - mu) / sigma) / sigma;
	}

	// return Phi(z) = standard Gaussian cdf using Taylor approximation
	public static double Phi(double z) {
		if (z < -8.0)
			return 0.0;
		if (z > 8.0)
			return 1.0;
		double sum = 0.0, term = z;
		for (int i = 3; sum + term != sum; i += 2) {
			sum = sum + term;
			term = term * z * z / i;
		}
		return 0.5 + sum * phi(z);
	}

	// return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
	public static double Phi(double z, double mu, double sigma) {
		return Phi((z - mu) / sigma);
	}

	// Compute z such that Phi(z) = y via bisection search
	public static double PhiInverse(double y) {
		return PhiInverse(y, .00000001, -8, 8);
	}

	// bisection search
	private static double PhiInverse(double y, double delta, double lo,
			double hi) {
		double mid = lo + (hi - lo) / 2;
		if (hi - lo < delta)
			return mid;
		if (Phi(mid) > y)
			return PhiInverse(y, delta, lo, mid);
		else
			return PhiInverse(y, delta, mid, hi);
	}
}
