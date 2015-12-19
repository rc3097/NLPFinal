import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import nlp.langmodel.SentimentQuantifier;
import nlp.util.Pair;

public class LstmRntnQuantifier extends SentimentQuantifier {
	
	MultiLayerNetwork net;
	
	public LstmRntnQuantifier(int numLayers, int lstmLayerSize, int inputDimension) {
		//Set up network configuration:
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
			.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
			.learningRate(0.1)
			.rmsDecay(0.95)
			.seed(12345)
			.regularization(true)
			.l2(0.001)
			.list(2)
			.layer(0, new GravesLSTM.Builder().nIn(inputDimension).nOut(lstmLayerSize)
					.updater(Updater.RMSPROP)
					.activation("tanh").weightInit(WeightInit.DISTRIBUTION)
					.dist(new UniformDistribution(-0.08, 0.08)).build())
			.layer(1, new RnnOutputLayer.Builder(LossFunction.SQUARED_LOSS).activation("tanh")
					.updater(Updater.RMSPROP)
					.nIn(lstmLayerSize).nOut(1).weightInit(WeightInit.DISTRIBUTION)
					.dist(new UniformDistribution(-0.08, 0.08)).build())
			.pretrain(false).backprop(true)
			.build();
		
		net = new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(1));
		
		System.out.println("Network initialized.");
		//Print the  number of parameters in the network (and for each layer)
		Layer[] layers = net.getLayers();
		int totalNumParams = 0;
		for( int i=0; i<layers.length; i++ ){
			int nParams = layers[i].numParams();
			System.out.println("Number of parameters in layer " + i + ": " + nParams);
			totalNumParams += nParams;
		}
		System.out.println("Total number of network parameters: " + totalNumParams);
	}

	@Override
	public void train(HashMap<Pair<String, String>, List<List<String>>> traindata) {
		// TODO Auto-generated method stub
		//separate data by topic
		HashMap<String,List<Pair<String,String>>> tweetsets = new HashMap<String,List<Pair<String,String>>>();
		for (Pair<String,String> key : traindata.keySet()) {
			String topic = key.getFirst();
			String label = key.getSecond();
			if (!tweetsets.containsKey(topic)) tweetsets.put(topic,new ArrayList<Pair<String,String>>());
			List<Pair<String,String>> tweetset = tweetsets.get(key);
			List<List<String>> sentences = traindata.get(key);
			for (List<String> sentenceList : sentences) {
				StringJoiner joiner = new StringJoiner(" ");
				for (String word : sentenceList)
					joiner.add(word);
				String sentenceString = joiner.toString();
				tweetset.add(new Pair<String,String>(label,sentenceString));
			}
		}
		
		
	}

	@Override
	public double predictProbability(HashMap<Pair<String, String>, List<List<String>>> testdata) {
		// TODO Auto-generated method stub
		
		return 0;
	}
	
	
}
