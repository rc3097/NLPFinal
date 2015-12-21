package nlp.langmodel;

import java.util.HashMap;
import java.util.List;

import nlp.util.Pair;

/**
 * Language models assign probabilities to sentences and generate sentences.
 */
public abstract class SentimentQuantifier {
	public abstract void train(HashMap<Pair<String, String>, List< List<String>>> traindata);
	public abstract double predictProbability(HashMap<Pair<String, String>, List< List<String>>> testdata);
	
	/*
	 * First parameter is actual p(+), second parameter is q(+) = [predicted p(+)]
	 */
	public double lossfunction(double p, double q){
		double p1 = p;
		double q1 =q;
		double p2 = 1-p;
		double q2 = 1-q;
		return p1 * Math.log(p1/q1) + p2 *Math.log(p2/q2);
	}
}
