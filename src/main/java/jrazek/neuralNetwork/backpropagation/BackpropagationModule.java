package jrazek.neuralNetwork.backpropagation;

import jrazek.neuralNetwork.abstracts.classes.layers.DerivedLayer;
import jrazek.neuralNetwork.abstracts.classes.layers.Layer;
import jrazek.neuralNetwork.abstracts.classes.neurons.DerivedNeuron;
import jrazek.neuralNetwork.abstracts.classes.neurons.Neuron;
import jrazek.neuralNetwork.netStructure.Bias;
import jrazek.neuralNetwork.netStructure.Connection;
import jrazek.neuralNetwork.netStructure.Net;
import jrazek.neuralNetwork.netStructure.inputLayer.InputNeuron;
import jrazek.neuralNetwork.netStructure.outputLayer.OutputNeuron;

import javax.management.RuntimeErrorException;
import java.util.*;

import static jrazek.neuralNetwork.utils.Rules.gradientDescentRate;
import static jrazek.neuralNetwork.utils.Rules.batchSize;

public class BackpropagationModule {
    final private Net net;
    final private List<DerivedLayer<? extends DerivedNeuron>> derivedLayers;
    private Map<Connection, List<Double>> gradientWeights = new HashMap<>();
    private Map<Bias, List<Double>> gradientBiases = new HashMap<>();
    private double errorT;
    private double [] expected;
    private int iteration;
    public BackpropagationModule(Net net) {
        this.net = net;
        this.derivedLayers = new ArrayList<>();
        this.iteration = 0;
        List<Layer<? extends Neuron>> layers = net.getLayers().subList(1, net.getLayers().size());
        for(Layer<? extends Neuron> layer : layers){
            if(layer instanceof DerivedLayer){
                derivedLayers.add((DerivedLayer<?extends DerivedNeuron>)layer);
            }
        }
        for(Connection c : net.getConnections()){
            gradientWeights.put(c, new LinkedList<>());
        }
        for(Bias b : net.getBiases()){
            gradientBiases.put(b, new LinkedList<>());
        }
    }
    public void backPropagate(double [] exp) throws RuntimeErrorException {
        this.expected = exp;
        this.errorT = getErrorT(expected);
        if(expected.length != net.getOutputLayer().getNeurons().size())
            throw new RuntimeErrorException(new Error("3123 ERROR"));

        for (Connection conn : net.getConnections()){
            double delta = -gradientDescentRate * differentiateWeight(conn);
            gradientWeights.get(conn).add(delta);
        }
        for (Bias bias : net.getBiases()){
            double delta = -gradientDescentRate * differentiateBias(bias);
            gradientBiases.get(bias).add(delta);
        }
        if((iteration + 1) % batchSize == 0){
            calculateAverageGradient();
        }
        iteration ++;
    }
    private void calculateAverageGradient(){
        for(Map.Entry<Connection, List<Double>> entry : gradientWeights.entrySet()){
            double sum = 0;
            for(Double d : entry.getValue()){
                sum += d;
            }
            sum /= entry.getValue().size();
            entry.getKey().updateWeight(sum);
            entry.getValue().clear();
            //weights should be updated after calculating all of the derivatives and biases
        }
        for(Map.Entry<Bias, List<Double>> entry : gradientBiases.entrySet()){
            double sum = 0;
            for(Double d : entry.getValue()){
                sum += d;
            }
            sum /= entry.getValue().size();
            entry.getKey().updateBias(sum);
            entry.getValue().clear();
            //weights should be updated after calculating all of the derivatives and biases
        }
    }
    private double differentiateWeight(Connection c){

        DerivedNeuron start = (DerivedNeuron)c.getOutputNeuron();
        double result = 1;
        if(c.getInputNeuron() instanceof DerivedNeuron)
            result = ((DerivedNeuron) c.getInputNeuron()).getActivationValue();
        else if(c.getInputNeuron() instanceof InputNeuron)
            result = ((InputNeuron) c.getInputNeuron()).getOutput();


        return result * getChain(start);
    }
    private double differentiateBias(Bias b){
        return getChain((DerivedNeuron)b.getNeuron());
    }
    private double getChain(DerivedNeuron start){
        if(start.getCurrentChain() != 0)
            return start.getCurrentChain();

        double chain = start.getActivationValue()*(1-start.getActivationValue());

        if(start instanceof OutputNeuron)
            chain *= -2*(expected[start.getIndexInLayer()] - start.getActivationValue());
        else{
            double tmp = 0;
            for(Connection c : start.getOutPutConnections()){
                tmp += c.getWeight()*getChain((DerivedNeuron)c.getOutputNeuron());
            }
            chain *= tmp;
        }
        start.setCurrentChain(chain);
        return chain;
    }
    private double getErrorT(double [] expected){
        double sum = 0;
        int neuronNum = 0;
        for(OutputNeuron neuron : net.getOutputLayer().getNeurons()){
            sum += Math.pow(neuron.getActivationValue() - expected[neuronNum], 2);
            neuronNum++;
        }
        return sum;
    }
    public double showError(){
        return errorT;
    }
    public double test(){
        Connection c = null;
        for(Connection co : net.getConnections()){
            if(co.getId() == 4)//static value.
                c = co;
        }
        if(c == null)
            return 0;
        DerivedNeuron in;
        DerivedNeuron out;
        if(c.getInputNeuron() == null || c.getOutputNeuron() == null)
            return 0;
        if(!(c.getInputNeuron() instanceof DerivedNeuron && c.getOutputNeuron() instanceof DerivedNeuron))
            return 0;
        in = ((DerivedNeuron)c.getInputNeuron());
        out = ((DerivedNeuron)c.getOutputNeuron());
        return 2*in.getActivationValue()*out.getActivationValue()*(1-out.getActivationValue())*(0.99-out.getActivationValue());
    }
}
