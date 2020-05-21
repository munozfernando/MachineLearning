package moduleTesting;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Metrics {

	private int truePositives;
	private int trueNegatives;
	private int falsePositives;
	private int falseNegatives;

	public int getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}

	public int getTrueNegatives() {
		return trueNegatives;
	}

	public void setTrueNegatives(int trueNegatives) {
		this.trueNegatives = trueNegatives;
	}

	public int getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}

	public int getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}

	public static double getMean(List<? extends Number> elements) {

		double temp = elements.stream().mapToDouble(x -> (double)x).average().getAsDouble();
		return temp;
	}

	public static double getVariance(List<? extends Number> values) {

		double mean = getMean(values);
		double variance = values.stream().mapToDouble(x -> (Math.pow((double)x - mean, 2))).sum() / (values.size() - 1);
		return variance;
	}

	public static double getStdDeviation(Double variance) {
		return Math.sqrt(variance);
	}

	public static double getStdDeviation(List<? extends Number> elements) {
		return getStdDeviation(getVariance(elements));
	}

	public double getCovariance(List<Double> vec1, List<Double> vec2) {

		if (vec1.size() != vec2.size() || vec1.size() < 1)
			throw new IllegalArgumentException();

		double mean1 = this.getMean(vec1);
		double mean2 = this.getMean(vec2);

		double sum = 0;

		Iterator<Double> it1 = vec1.iterator();
		Iterator<Double> it2 = vec2.iterator();

		while (it1.hasNext() && it2.hasNext()) {

			sum += (((it1.next() - mean1) * (it2.next() - mean2)));
		}
		return sum / (vec1.size() - 1);
	}

	
	// Consider fork-join??!?!??
	public List<List<Double>> getCovarianceMatrix(List<List<Double>> samples) {

		ExecutorService pool = Executors.newCachedThreadPool();
		

		for (int i = 0; i < samples.size(); i++) {
			for (int j = i + 1; j < samples.size(); j++) {
				final List<Double> vec1 = samples.get(i);
				final List<Double> vec2 = samples.get(j);
				
				Callable<Double> r1 = new Callable<Double>() {
					@Override
					public Double call() throws Exception {
						return getCovariance(vec1, vec2);
					}
				};
				Future<Double> result = pool.submit(r1);
			}
		}
		pool.shutdown();
//		try {
//			pool.awaitTermination(5, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	public double getAccuracy() {
		return ((double) (this.truePositives + this.trueNegatives))
				/ (this.truePositives + this.falsePositives + this.trueNegatives + this.falseNegatives);
	}

	public double getSensitivity() {
		return ((double) this.truePositives) / (this.truePositives + this.falsePositives);
	}

	public double getSpecificity() {
		return ((double) this.trueNegatives) / (this.trueNegatives + this.falsePositives);
	}

	public double getPrecision() {
		return ((double) this.truePositives) / (this.truePositives + this.falsePositives);
	}

	public double getRecall() {
		return getSensitivity();
	}

	public double getFOne() {
		return ((double) this.truePositives)
				/ (this.truePositives + ((this.falseNegatives + this.falsePositives) / 2.0));
	}
}
