package moduleTesting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static java.util.stream.Collectors.*;

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
		return elements.stream().mapToDouble(x -> (double) x).average().getAsDouble();
	}

	public static List<Double> getMeanList(List<? extends List<? extends Number>> list) {
		return list.parallelStream().map(e -> getMean(e)).collect(Collectors.toList());
	}

	public static double getVariance(List<? extends Number> elements) {

		double mean = getMean(elements);
		double variance = elements.stream().mapToDouble(x -> (Math.pow((double) x - mean, 2))).sum()
				/ (elements.size() - 1);
		return variance;
	}

	public static double getStdDeviation(Double variance) {
		return Math.sqrt(variance);
	}

	public static double getStdDeviation(List<? extends Number> elements) {
		return getStdDeviation(getVariance(elements));
	}

	public static <T extends Number> double getCovariance(List<T> vec1, List<T> vec2) {

		if (vec1.size() != vec2.size() || vec1.size() < 1)
			throw new IllegalArgumentException();

		double meanVec1 = Metrics.getMean(vec1);
		double meanVec2 = Metrics.getMean(vec2);

		double sum = 0;

		Iterator<T> it1 = vec1.iterator();
		Iterator<T> it2 = vec2.iterator();

		while (it1.hasNext() && it2.hasNext()) {

			sum += (((it1.next().doubleValue() - meanVec1) * (it2.next().doubleValue() - meanVec2)));
		}
		return sum / (vec1.size() - 1);
	}
	
	public static <T extends Number> double getCovariance(List<T> vec1, List<T> vec2, List<? extends List<T>> meanList) {

		if (vec1.size() != vec2.size() || vec1.size() < 1)
			throw new IllegalArgumentException();

		double meanVec1 = Metrics.getMean(vec1);
		double meanVec2 = Metrics.getMean(vec2);

		double sum = 0;

		Iterator<T> it1 = vec1.iterator();
		Iterator<T> it2 = vec2.iterator();

		while (it1.hasNext() && it2.hasNext()) {

			sum += (((it1.next().doubleValue() - meanVec1) * (it2.next().doubleValue() - meanVec2)));
		}
		return sum / (vec1.size() - 1);
	}

	/**
	 * This method returns a co-variance matrix that compares all features against
	 * each other. Runs concurrently.
	 * 
	 * @param          <T> Type of actual data
	 * @param          <L> Type for outer data list
	 * @param features Nested List for data param T
	 * @return Matrix with covariance values
	 */
	public static <T extends Number, L extends List<T>> Matrix<Double> getCovarianceMatrixConcurrently(
			List<L> features) {
		List<Double> meanList = getMeanList(features);
		
		long start = System.currentTimeMillis();
		Matrix<Double> resultSetMatrix = new Matrix<>(features.size());
		resultSetMatrix.setData(features.parallelStream().map(
				featureA -> features.parallelStream().map(
						featureB -> getCovariance(featureA, featureB))
							.collect(toCollection(ArrayList::new)))
								.collect(toCollection(ArrayList::new)));
		
		System.out.println("Stream time taken: " + (System.currentTimeMillis()-start));
		
		
		
		
		
		
		
		
		
		
		
		
		long startExecutorTime = System.currentTimeMillis();
		ExecutorService execService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Matrix<Future<Double>> futuresMatrix = new Matrix<>(features.size());
		List<Future<Double>> futuresList = new ArrayList<>();
		

		
		// 2
		for (int i = 0; i < features.size(); i++) {
			ArrayList<Future<Double>> covRow = new ArrayList<>();
			// For a staggered array, put i+1 here
			for (int j = 0; j < features.size(); j++) {
				final List<T> vec1 = features.get(i);
				final List<T> vec2 = features.get(j);

				Callable<Double> covarianceCallable = new Callable<Double>() {
					@Override
					public Double call() throws Exception {
						return getCovariance(vec1, vec2);
					}
				};
				Future<Double> submittedFuture = execService.submit(covarianceCallable);
				covRow.add(submittedFuture);
				futuresList.add(submittedFuture);
			}
			futuresMatrix.setRow(i, covRow);
		}
		execService.shutdown();
		
		resultSetMatrix.setData(futuresMatrix.getData().parallelStream()
				.map((Function<? super ArrayList<Future<Double>>, ? extends ArrayList<Double>>) row -> {
					return row.parallelStream().map(element -> {
						Double result = null;
						try {
							result = element.get();
						} catch (InterruptedException | ExecutionException e) {
							System.out.println("Thread was interrupted or failed to execute");
							e.printStackTrace();
							System.exit(-1);
						}
						return result;
					}).collect(toCollection(ArrayList::new));
				}).collect(toCollection(ArrayList::new)));
		System.out.println("map matrix stream: " +  (System.currentTimeMillis() - startExecutorTime));

		return resultSetMatrix;

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
