package moduleTesting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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

		double temp = elements.stream().mapToDouble(x -> (double) x).average().getAsDouble();
		return temp;
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

	public <T extends Number> double getCovariance(List<T> vec1, List<T> vec2) {

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
	 * This this is fucked. Its really complex and it might have too much overhead.
	 * I will create another method just like this one, but execute it series and
	 * compare...
	 * 
	 * @param features
	 * @return
	 */
	public <T extends Number, L extends List<T>> Matrix<Double> getCovarianceMatrixConcurrently(List<L> features) {

		int count = 100;
		for (int k = 1; k < count; k++) {

			/*
			 * This is the plan 1. Create an executor 2. Feed Futures into the executor 3.
			 * Check if all futures have been completed 4. Return the covariance matrix
			 */
			long startTimeA = System.currentTimeMillis();
			// 1
			ExecutorService execService = Executors.newFixedThreadPool(k);
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

			// 3
			// Block this thread while we ensure all other threads have finished
			// NOTE: Should be unnecessary, since when calling future.get(), this thread
			// blocks anyway...
//		boolean hasRunningThread = true;
//		while(hasRunningThread) {
//			hasRunningThread = false;
//			for(Iterator<Future<Double>> futureIter = futuresList.iterator(); futureIter.hasNext() && hasRunningThread == false;) {
//				if(!futureIter.next().isDone()) {
//					hasRunningThread = true;
//				}
//			}
//		}

			// 4
			// Map the old matrix into a new one of actual values...
			Iterator<Future<Double>> futListIter = futuresList.iterator();
			Matrix<Double> resultSetMatrix = new Matrix<>(features.size());

			for (int i = 0; i < futuresMatrix.getDimensions(); i++) {
				for (int j = 0; j < futuresMatrix.getDimensions(); j++) {
					try {
						resultSetMatrix.getRow(i).add(futListIter.next().get());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			long elapsedA = System.currentTimeMillis() - startTimeA;
			
			System.out.println("****************************************");
			System.out.println("TESTING: " + k + " THREADS");
			System.out.println("Concurrent time: "+ elapsedA);
			int i = 0;

		}
		// Print the matrix and return
		//System.out.println(resultSetMatrix);
		//return resultSetMatrix;
		return null;
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
