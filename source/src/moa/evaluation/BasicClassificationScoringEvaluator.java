/*
 *    BasicClassificationSocoringEvaluator.java
 *    Copyright (C) 2013 Federal University of Minas Gerais, Belo Horizonte, Brazil
 *    @author Roberto L. Oliveira Junior (robertolojr at dcc dot ufmg dot br)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */
package moa.evaluation;

import moa.AbstractMOAObject;
import moa.core.Measurement;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Classification evaluator that performs basic incremental evaluation.
 * This class extends {@link BasicClassificationPerformanceEvaluator} including the MSE calculus.
 * 
 * @author Roberto L. Oliveira Junior (robertolojr at dcc dot ufmg dot br)
 * @version $Revision: 1 $
 */
public class BasicClassificationScoringEvaluator extends AbstractMOAObject
			implements ClassificationPerformanceEvaluator {

    private static final long serialVersionUID = 1L;
    
    protected double normalizedMse;
    protected double mse;    
    protected int saw;

    protected double weightObserved;
    protected double weightCorrect;

    protected double[] columnKappa;

    protected double[] rowKappa;

    protected int numClasses;

    @Override
    public void reset() {
        reset(this.numClasses);
    }

    public void reset(int numClasses) {
        this.numClasses = numClasses;
        this.rowKappa = new double[numClasses];
        this.columnKappa = new double[numClasses];
        for (int i = 0; i < this.numClasses; i++) {
            this.rowKappa[i] = 0.0;
            this.columnKappa[i] = 0.0;
        }
        this.weightObserved = 0.0;
        this.weightCorrect = 0.0;
    }

    @Override
    public void addResult(Instance inst, double[] classVotes) {
        double weight = inst.weight();
        int trueClass = (int) inst.classValue();
        if (weight > 0.0) {
            if (this.weightObserved == 0) {
                reset(inst.dataset().numClasses());
            }
            this.weightObserved += weight;
            
            //MSE Calculus
            int predictedClass = Utils.maxIndex(classVotes);
            if (predictedClass == trueClass) {
                this.weightCorrect += weight;
            }
            
            double[] normalized = normalize(classVotes);

            double vote = 0;
            if(normalized.length > 0){
            	vote = trueClass < normalized.length ? normalized[trueClass] : 0;
            }

            if(Double.compare(vote, Double.NaN) == 0){
            	int countNaN = 0;
            	for(int i = 0; i < classVotes.length; ++i){
            		if(Double.compare(normalized[i], Double.NaN) == 0){
            			countNaN++;
            		}
            	}
            	vote = 1;
            	if(countNaN > 1 && classVotes.length > 1){
            		vote = 1.0/countNaN;
            	}
            	
            }
            this.mse += 1-vote;

            this.saw++;
            
            this.rowKappa[predictedClass] += weight;
            this.columnKappa[trueClass] += weight;
        }
    }

    @Override
    public Measurement[] getPerformanceMeasurements() {
    	return new Measurement[]{
                new Measurement("classified instances",
                getTotalWeightObserved()),
                new Measurement("classifications correct (percent)",
                getFractionCorrectlyClassified() * 100.0),
                new Measurement("Kappa Statistic (percent)",
                getKappaStatistic() * 100.0),
                new Measurement("MSE",getMSE())};
    }
    
    public double getTotalWeightObserved() {
        return this.weightObserved;
    }

    public double getFractionCorrectlyClassified() {
        return this.weightObserved > 0.0 ? this.weightCorrect
                / this.weightObserved : 0.0;
    }

    public double getFractionIncorrectlyClassified() {
        return 1.0 - getFractionCorrectlyClassified();
    }

    public double getKappaStatistic() {
        if (this.weightObserved > 0.0) {
            double p0 = getFractionCorrectlyClassified();
            double pc = 0.0;
            for (int i = 0; i < this.numClasses; i++) {
                pc += (this.rowKappa[i] / this.weightObserved)
                        * (this.columnKappa[i] / this.weightObserved);
            }
            double k = (p0 - pc) / (1.0 - pc);
            if(Double.compare(k, Double.NaN) == 0){
            	return 0;
            }
            return k;
        } else {
            return 0;
        }
    }
    
    public double getMSE(){
		return ((double)this.mse/this.saw);
    }
    
    public double getNormalizedMSE(){
    	return (this.normalizedMse/(double)this.saw);
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        Measurement.getMeasurementsDescription(getPerformanceMeasurements(),
                sb, indent);
    }
    
    public double[] normalize(double[] v){
    	double[] normalized = new double[v.length];
    	double n = 0;
    	for(double vote : v){
    		n += vote;
    	}
    	if(Double.compare(n, 0) != 0){
    		for(int i = 0; i < v.length; i++){
    			normalized[i] = v[i] / n;
    		}
    	}
    	
    	return normalized;
    }
}