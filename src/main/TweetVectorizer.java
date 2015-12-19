package main;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;

public class TweetVectorizer {
	static StanfordCoreNLP tokenizer;
	static StanfordCoreNLP pipeline;
	

	public static void initialize() {
		Properties pipelineProps = new Properties();
		pipelineProps.setProperty("annotators", "parse, sentiment");
		pipelineProps.setProperty("enforceRequirements", "false");
		Properties tokenizerProps = new Properties();
		tokenizerProps.setProperty("annotators", "tokenize, ssplit");
		tokenizer = new StanfordCoreNLP(tokenizerProps);
		pipeline = new StanfordCoreNLP(pipelineProps);
	}

	public static Double[] vectorize(String tweet) {
		Double[] vector = new Double[25];
		for (int i = 0; i < 25; i++) {
			vector[i] = 0.0;
		}
		Annotation annotation = new Annotation(tweet);
		tokenizer.annotate(annotation);
		List<Annotation> annotations = Generics.newArrayList();
		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			Annotation nextAnnotation = new Annotation(sentence.get(CoreAnnotations.TextAnnotation.class));
			nextAnnotation.set(CoreAnnotations.SentencesAnnotation.class, Collections.singletonList(sentence));
			annotations.add(nextAnnotation);
		}
		for (Annotation ann : annotations) {
			pipeline.annotate(ann);
			for (CoreMap sentence : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
				SimpleMatrix nodeVector = RNNCoreAnnotations.getNodeVector(tree);
				for (int i = 0; i < 25; i++) {
					vector[i] += nodeVector.get(i);
				}
				assert nodeVector.getNumElements() == 25;
			}
		}
		return vector;

	}

	public static void main (String[] args) {
	String tweet = args[0];
	TweetVectorizer.initialize();
	Double[] vector = TweetVectorizer.vectorize(tweet);
		
	}
}
