package jrazek.neuralNetwork.backpropagation;

import jrazek.neuralNetwork.abstracts.classes.layers.DerivedLayer;
import jrazek.neuralNetwork.abstracts.classes.layers.Layer;
import jrazek.neuralNetwork.abstracts.classes.neurons.DerivedNeuron;
import jrazek.neuralNetwork.abstracts.classes.neurons.Neuron;
import jrazek.neuralNetwork.netStructure.Connection;
import jrazek.neuralNetwork.netStructure.Net;
import jrazek.neuralNetwork.netStructure.hiddenLayer.HiddenLayer;
import jrazek.neuralNetwork.netStructure.inputLayer.InputNeuron;
import jrazek.neuralNetwork.netStructure.outputLayer.OutputLayer;
import jrazek.neuralNetwork.netStructure.outputLayer.OutputNeuron;

import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jrazek.neuralNetwork.utils.Rules.gradientDescentRate;
import static jrazek.neuralNetwork.utils.Utils.round;
import static jrazek.neuralNetwork.utils.Utils.sigmoid;

public class BackpropagationModule {
    final private Net net;
    final private List<DerivedLayer<? extends DerivedNeuron>> derivedLayers;
    double errorT;
    double [] expected;
    public BackpropagationModule(Net net) {
        this.net = net;
        this.derivedLayers = new ArrayList<>();
        List<Layer<? extends Neuron>> layers = net.getLayers().subList(1, net.getLayers().size());
        for(Layer<? extends Neuron> layer : layers){
            if(layer instanceof DerivedLayer){
                derivedLayers.add((DerivedLayer<?extends DerivedNeuron>)layer);
            }
        }
    }
    public void backPropagate(double [] exp) throws RuntimeErrorException {
        this.expected = exp;
        this.errorT = getErrorT(expected);
        if(expected.length != net.getOutputLayer().getNeurons().size())
            throw new RuntimeErrorException(new Error("3123 ERROR"));
        Map<Connection, Double> gradient = new HashMap<>(net.getConnections().size());
        for (Connection conn : net.getConnections()){
            double delta = -gradientDescentRate*derivative(conn);
            gradient.put(conn, delta);
        }
        for(Map.Entry<Connection, Double> entry : gradient.entrySet()){
            ///System.out.println("old: " + entry.getKey().getWeight() + " new: " + entry.getValue());
            entry.getKey().updateWeight(entry.getValue());
            //weights should be updated after calculating all of the derivatives
        }
    }
    private double derivative(Connection c){
        DerivedNeuron startingNeuron = ((DerivedNeuron)c.getOutputNeuron());
        double result = 1;

        if(c.getInputNeuron() instanceof InputNeuron)
            result = ((InputNeuron) c.getInputNeuron()).getOutput();//1
        else if(c.getInputNeuron() instanceof DerivedNeuron)
            result = ((DerivedNeuron) c.getInputNeuron()).getActivationValue();//1

        result *= startingNeuron.getActivationValue()*(1-startingNeuron.getActivationValue());
        result *= getChain(startingNeuron);
        if(c.getId() == 4){
            System.out.println("chain value = " + result);
        }
        return result;
    }
    private double getChain(DerivedNeuron start){
        double chain = 1;
        if(start.getLayer() instanceof OutputLayer){
            chain *= 2*start.getActivationValue()*(start.getActivationValue() - expected[start.getIndexInLayer()]);
        }
        else{
            List<Connection> connections = start.getOutPutConnections();
            for(Connection c : connections){
                chain *= c.getWeight() * getChain((DerivedNeuron)c.getOutputNeuron());
            }
            /// TODO: 25.08.2020 split
        }
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
}
