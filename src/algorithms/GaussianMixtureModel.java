package algorithms;

import java.util.Random;

import util.Helper;

import maths.functions.Gaussian;
/**
* 
 * @author gjergji.kasneci
*
*/
public class GaussianMixtureModel {
     
     private final static double epsilon = 0.0000000001;
     private final static int max_iterations = 9;
     private Gaussian[] gaussians;
     private double[] priors;
     private double[][] responsibilities;
     
     
     /**
     * Sets all Gaussians
     * @param numberComponents
     */
     public GaussianMixtureModel(int numberComponents){
           gaussians = new Gaussian[numberComponents];
           priors = new double[numberComponents];
           for(int i = 0; i < gaussians.length; i++){
                gaussians[i] = new Gaussian();
           }
           init();
     }

     public static enum IterationLimit{
           MAX_ITERATIONS,
           CONVERGENCE
     }
     
     public int getNumberOfComponents(){
           return priors.length;
     }
     
     public Gaussian[] getGaussians() {
           return gaussians;
     }

     public void setGaussians(Gaussian[] gaussians) {
           this.gaussians = gaussians;
     }

     public double[] getPriors() {
           return priors;
     }

     public void setPriors(double[] priors) {
           this.priors = priors;
     }
     
     /**
     * initializes all Gaussians with random means and variances and tries to break symmetry
     */
     private void init(){
           Random r = new Random();
           for(int i=0; i < gaussians.length; i++){
                double m = r.nextDouble();
                double v = 1.0 + r.nextDouble();
                gaussians[i].setMu(m);
                gaussians[i].setVar(v);
                priors[i] = 1.0/priors.length;
                
           }
     }
     
     /**
     * computes target function on the given data points
     * @param points
     * @return the value of the target function
     */
     private double getTargetFunctionValue(double[] points){
           double target = 0.0;
           for(int j = 0; j < points.length; j++){
                double sum = 0.0;
                for(int i = 0; i < gaussians.length; i++){
                     sum += gaussians[i].eval(points[j])*priors[i];
                }
                target += Math.log(sum);
           }
           return target;
     }
     
     /**
     * Expectation Maximization on the data points
     * @param dataPoints
     */
     public int[] expectationMaximization(double[] dataPoints, IterationLimit il){
           
           responsibilities = new double[gaussians.length][dataPoints.length];
           
           //set the number of iterations or convergence
           int count = 0;
           switch(il){
                case MAX_ITERATIONS: count = max_iterations; break;
                case CONVERGENCE: count = Integer.MAX_VALUE; break;
           }
           
           //run EM
           Helper.print("At iteration: " + count);
           while(count > 0){
                double val = getTargetFunctionValue(dataPoints);
                expectation(dataPoints);
                maximization(dataPoints);
                double newVal = getTargetFunctionValue(dataPoints);
                if(Math.abs(newVal-val)<epsilon) 
                     break;
                count--;
           }
           
           //find for each point the most likely Gaussian
           int[] fromComponent = new int[dataPoints.length];
           for(int j = 0; j < dataPoints.length; j++){
                double maxVal = 0.0;
                for(int i = 0; i < gaussians.length; i++){
                     double newVal = gaussians[i].eval(dataPoints[j]);
                     if(newVal>maxVal){
                           maxVal=newVal;
                           fromComponent[j]=i;
                     }    
                }
           }
           return fromComponent;
     }
     
     
     /**
     * Expectation step in the EM algorithm
     * @param points
     */
     private void expectation(double[] points){
           //compute p(xj)
           double[] weightNormalizers = new double[points.length];
           for(int j = 0; j < points.length; j++){
                double weightNormalizer = 0.0;
                for(int i = 0; i < gaussians.length; i++){
                     weightNormalizer += gaussians[i].eval(points[j])*priors[i];
                }
                weightNormalizers[j] = weightNormalizer;
           }
           
           //compute responsibilities: p(Gi|xj)
           for(int i = 0; i < gaussians.length; i++){
                for(int j = 0; j < points.length; j++){
                     responsibilities[i][j] = priors[i]*gaussians[i].eval(points[j])/weightNormalizers[j];
                }
           }
     }
     
     /**
     * Maximization step in the EM algorithm
     * @param points
     */
     private void maximization(double[] points){
           
           for(int i = 0; i < gaussians.length; i++){
                double mci = 0.0;
                for(int j = 0; j < points.length; j++){
                     mci += responsibilities[i][j];
                }
                priors[i] = mci/points.length;
                double sum = 0.0;
                for(int j = 0; j < points.length; j++){
                     sum += responsibilities[i][j]*points[j];
                }
                double m = sum/mci;
                sum = 0.0;
                for(int j = 0; j < points.length; j++){
                     sum += responsibilities[i][j]*Math.pow(points[j] - m, 2);
                }
                double v = sum/mci;
                gaussians[i].setMu(m);
                gaussians[i].setVar(v);
           }
     }    
     
     public static void main(String[] args){
           double [] points = new double[] {0.11, 0.09, 0.13, 0.08, 0.10, 0.12, 0.09, 0.124, 0.11, 0.98, 0.78, 0.83, 0.87, 0.91, 0.89, 0.79, 0.93, 1.98, 1.78, 1.83, 1.87, 1.91, 1.89, 1.79, 1.93};
           //double [] points = new double[] {0.11, 0.09, 0.13, 0.08, 0.10, 0.12, 0.09, 0.124, 0.11, 1.98, 1.78, 1.83, 1.87, 1.91, 1.89, 1.79, 1.93};
           //double [] points = new double[] {0.11, 0.09, 0.13, 0.08, 0.10, 0.12, 0.09, 0.124, 0.11, 0.98, 0.78, 0.83, 0.87, 0.91, 0.89, 0.79, 0.93};
           GaussianMixtureModel model = new GaussianMixtureModel(3);
           int [] result = model.expectationMaximization(points, IterationLimit.CONVERGENCE);
           for(int component: result)
                System.out.print(component+"\t");
     }
}

