/*
 * CofVGenRoeMetz.java
 * 
 * v2.0b
 * 
 * @Author Rohan Pathare
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
 *     Calculates product moments from a generalized Roe and Metz model that allows for differerent variances 
 *     for each truth state and modality. Adapted for java from cofv_genroemetz.pro (Brandon D. Gallas, PhD)
 */

package mrmc.core;

public class CofVGenRoeMetz {

	public static double prodMoment1(double[] u, double[] scale, int n) {
		double scale1 = scale[0];
		double scale20 = scale[1];
		double scale21 = scale[2];

		double lx = 10 * Math.sqrt(scale1);
		return lx;
	}
}
