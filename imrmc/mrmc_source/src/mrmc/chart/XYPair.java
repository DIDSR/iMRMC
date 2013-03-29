package mrmc.chart;

public class XYPair implements Comparable<XYPair> {
	public double x;
	public double y;

	public XYPair(double theX, double theY) {
		x = theX;
		y = theY;
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (other.getClass() != getClass()) {
			return false;
		}

		XYPair compared = (XYPair) other;
		return ((Double.compare(this.x, compared.x) == 0) && (Double.compare(
				this.y, compared.y) == 0));
	}

	@Override
	public int compareTo(XYPair other) {
		if (other == null) {
			NullPointerException e = new NullPointerException();
			throw e;
		}
		if (other == this) {
			return 0;
		}

		if (this.equals(other)) {
			return 0;
		}

		if (Double.compare(this.x, other.x) < 0) {
			return -1;
		} else if (Double.compare(this.x, other.x) == 0) {
			if (Double.compare(this.y, other.y) < 0) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
