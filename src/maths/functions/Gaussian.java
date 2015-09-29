package maths.functions;

import java.util.HashMap;
import java.util.Map;
/**
* 
 * @author gjergji.kasneci
*
*/
public class Gaussian {
     
     //mean
     private double mu = 0.0;
     //variance
     private double var = 1.0;
     
     /**
     * 
      * 0-1 Gaussian
     */
     public Gaussian(){}
     
     
     /**
     * Constructs a new Gaussian
     * @param m mean
     * @param v variance
     */
     public Gaussian(double m, double v){
           mu = m;
           var = v;
     }
     
     /**
     * 
      * @param x
     * @return the Gaussian value for x
     */
     public Double eval(Double x){
           return 1/Math.sqrt(2.0*Math.PI*var)*Math.pow(Math.E, -Math.pow((x-mu),2)/(2*var));
     }
     
     /**
     * 
      * @return the Gaussian values from 3 standard deviations on the left and the right of the Gaussian
     */
     public Map<Double, Double> map(){
           Map<Double, Double> map = new HashMap<Double, Double>();
           for(double x=-3.0*Math.sqrt(var); x<=3.0*Math.sqrt(var); x += Math.sqrt(var)/2.0){
                map.put(x, eval(x));
           }
           return map;
     }
     
     /**
     * prints this Gaussian
     */
     public void printMap(){
           for(double x : map().keySet())
                System.out.println(x +"\t"+ eval(x));
     }
     
     
     public double getMu() {
           return mu;
     }
     public void setMu(double mu) {
           this.mu = mu;
     }
     public double getVar() {
           return var;
     }
     public void setVar(double var) {
           this.var = var;
     }
     
     public static void main(String[] args){
           Gaussian g = new Gaussian();
           g.printMap();
           
     }

}


