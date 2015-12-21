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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import nlp.langmodel.SentimentQuantifier;
import nlp.util.Pair;

public class LstmRntnQuantifier extends SentimentQuantifier {
	
	MultiLayerNetwork net;
	boolean verbose;
	
	public LstmRntnQuantifier(int numLayers, int lstmLayerSize, int inputDimension, boolean verbose) throws Exception {
		this.verbose = verbose;
		//Set up network configuration:
		NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
			.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
			.learningRate(0.01)
			.rmsDecay(0.95)
			.seed(12345)
			.regularization(true)
			.l2(0.001)
			.list(numLayers+1);
		if (numLayers < 1) throw new Exception("Must have numLayers >= 1!");
		builder.layer(0, new GravesLSTM.Builder().nIn(inputDimension).nOut(lstmLayerSize)
				.updater(Updater.RMSPROP)
				.activation("sigmoid").weightInit(WeightInit.DISTRIBUTION)
				.dist(new UniformDistribution(-0.01, 0.01)).build());
		for (int i = 1; i < numLayers; ++i) {
			builder.layer(i, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
					.updater(Updater.RMSPROP)
					.activation("sigmoid").weightInit(WeightInit.DISTRIBUTION)
					.dist(new UniformDistribution(-0.08, 0.08)).build());
		}
		builder.layer(numLayers, new RnnOutputLayer.Builder(LossFunction.SQUARED_LOSS).activation("sigmoid")
				.updater(Updater.RMSPROP)
				.nIn(lstmLayerSize).nOut(1).weightInit(WeightInit.DISTRIBUTION)
				.dist(new UniformDistribution(-0.08, 0.08)).build())
				.pretrain(false).backprop(true);
		MultiLayerConfiguration conf = builder.build();
		
		net = new MultiLayerNetwork(conf);
		net.init();
		if (verbose) net.setListeners(new ScoreIterationListener(1));
		
		if (verbose) {
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
	}
	
	@Override
	public void train(HashMap<Pair<String,String>,List<List<String>>> traindata) {}
	
	public void train(HashMap<String,TweetSet> tweetSets, int numEpochs) {
		System.out.println("Training network.");
		WordVectorIterator iter = new WordVectorIterator(tweetSets);
		for (int epoch = 0; epoch < numEpochs; ++epoch) {
			net.rnnClearPreviousState();
			net.fit(iter);
			if (verbose) System.out.println("Completed epoch "+epoch);
		}
		System.out.println("Training complete.\n");
	}

	@Override
	public double predictProbability(HashMap<Pair<String, String>, List<List<String>>> testdata) {
		return 0;
	}
	
	public double predictProbability(TweetSet tweetSet) {
		double[] features = new double[tweetSet.size() * 25];
		for (int j = 0; j <tweetSet.size() ; j++) {
			for (int k = 0 ; k < 25 ; k++) {
				features[25 * j + k] = tweetSet.getTweetSet().get(j).getVector()[k];
			
			}
		}
		INDArray inputs = Nd4j.create(features, new int[] { tweetSet.size(), 25 }); 
		net.rnnClearPreviousState();
		net.feedForward(inputs);
		INDArray output = net.activate();
		return (double)output.getFloat(new int[]{tweetSet.size()-1,0});
	}
	
	public double KLD(HashMap<String,TweetSet> tweetSets, boolean verbose) {
		double totalLoss = 0.0;
		for (String topic : tweetSets.keySet()) {
			
			double currentPrediction = predictProbability(tweetSets.get(topic));
			currentPrediction = currentPrediction > 0.9999999999 ? 0.9999 : currentPrediction;
			currentPrediction = currentPrediction < 0.0000000001 ? 0.0001 : currentPrediction;
			
			double label = tweetSets.get(topic).getpValue();
			label = label > 0.9999999999 ? 0.9999 : label;
			label = label < 0.0000000001 ? 0.0001 : label;
			
			double currentKLD = lossfunction(label, currentPrediction);
			if (verbose)
				System.out.println("For topic \""+topic+"\", prediction: "+currentPrediction+", actual: "+label+", KLD: "+currentKLD);
			totalLoss += currentKLD;
			
		}
		return totalLoss/tweetSets.size();
	}
}
