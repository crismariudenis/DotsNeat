package com.example;

import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static com.example.Main.*;
import static com.example.Main.p;

public class NeuralNetwork {
    Matrix[] weights; // Matrix[i] should be from i to i+1
    ArrayList<Float>[] bias;// bias[i] should be the biases from level i
    float minWeight = -5;
    float maxWeight = 5;
    float minBias = -5;
    float maxBias = 5;
    int step = 0;
    int maxNrStep = 1000;

    NeuralNetwork(int[] v) {
        bias = (ArrayList<Float>[]) new ArrayList[v.length];
        weights = new Matrix[v.length - 1];
        for (int i = 0; i < v.length; i++) {
            if (i != v.length - 1)//last layer
                weights[i] = new Matrix(v[i], v[i + 1], minWeight, maxWeight);
            bias[i] = new ArrayList<Float>();
            initialize(bias[i], v[i]);
        }
    }
    //returns the output after the matrices multiplications

    ArrayList<Float> process(PVector pos, PVector vel) {
        ArrayList<Float> input = new ArrayList<>(Arrays.asList(pos.x + vel.x - (goal.x + goalVel.x), pos.y + vel.y - (goal.y + goalVel.y), pos.x - goal.x, pos.y - goal.y));
        //Todo: Maybe change the pos of the walls with dif of velocity
//  THIS IS GOOD: ArrayList<Float> input = new ArrayList<>(Arrays.asList(pos.x-goal.x, pos.y-goal.y,vel.x,vel.y, goalVel.x, goalVel.y));
        if (input.size() != Main.nnShape[0]) {
            System.out.println("Number of inputs=" + input.size() + "doesn't match the NN shape" + Main.nnShape[0]);
            System.exit(0);
        } else {
            //loop through all the layers except the last
            for (int i = 0; i < Main.nnShape.length - 1; i++) {
                //Multiply the weight matrix by the input
                input = weights[i].calc(sigmoid(input), bias[i + 1]);
            }
        }
        //lock the output between [-1,1]
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i) <= -1)
                input.set(i, -1f);
            else if (input.get(i) >= 1)
                input.set(i, 1f);
        }
        return input;
    }

    //initialize with random values
    private void initialize(ArrayList<Float> v, int size) {
        for (int i = 0; i < size; i++) {
            Random R = new Random();
            v.add(minBias + R.nextFloat() * (maxBias - minBias));
        }
    }

    private ArrayList<Float> sigmoid(ArrayList<Float> v) {
        ArrayList<Float> ans = new ArrayList<Float>();
        for (Float x : v) ans.add(1 / (1 + exp(-x)));
        return ans;
    }

    //returns a clone of a NeuralNetwork
    public NeuralNetwork clone() {
        NeuralNetwork clone = new NeuralNetwork(Main.nnShape);
        //clone the bias by value
        clone.bias = (ArrayList<Float>[]) new ArrayList[this.bias.length];
        for (int i = 0; i < this.bias.length; i++) {
            clone.bias[i] = new ArrayList<Float>();
            for (int j = 0; j < this.bias[i].size(); j++)
                clone.bias[i].add(this.bias[i].get(j));

        }

        clone.weights = new Matrix[this.weights.length];
        for (int i = 0; i < this.weights.length + 1; i++)
            if (i != this.weights.length)//last layer
                clone.weights[i] = weights[i].copy();

        return clone;
    }

   //merge 2 NeuralNetwork
    public NeuralNetwork merge(NeuralNetwork p2) {
        NeuralNetwork merger = p2.clone();

        for (int i = 0; i < this.weights.length + 1; i++) {
            float r = p.random(1);
            if (i != this.weights.length && r < 0.5)
                merger.weights[i] = this.weights[i].merge(p2.weights[i]);
        }
        for (int i = 0; i < this.bias.length; i++)
            for (int j = 0; j < this.bias[i].size(); j++) {
                float r = p.random(1);
                if (r < 0.5) {
                    merger.bias[i].set(j, this.bias[i].get(j));
                }
            }
        return merger;
    }

    public void mutate() {
        float mutationRate = 0.01f;
        //mutate bias
        for (ArrayList<Float> bia : bias)
            for (int j = 0; j < bia.size(); j++) {
                float rand = p.random(1);
                if (rand < mutationRate) {
                    float editBias = p.random(-1, 1);
                    bia.set(j, editBias);
                }
            }
        //mutate weights
        for (Matrix x : weights)
            x.mutate();

    }

}