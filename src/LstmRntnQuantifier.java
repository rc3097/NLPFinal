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
	public void train(HashMap<Pair<String,String>,List<List<String>>> traindata) {}
	
	public void train(HashMap<String,TweetSet> tweetSets, int numEpochs) {
		System.out.println("Training network.");
		WordVectorIterator iter = new WordVectorIterator(tweetSets);
		for (int epoch = 0; epoch < numEpochs; ++epoch) {
			System.out.println("Beginning epoch "+epoch+":");
			net.fit(iter);
			System.out.println("Completed epoch "+epoch+".\n");
		}
		System.out.println("Training complete.\n");
	}

	@Override
	public double predictProbability(HashMap<Pair<String, String>, List<List<String>>> testdata) {
		return 0;
	}
	
	public double predictProbability(TweetSet tweetSet) {
		
	}
	
	
}
