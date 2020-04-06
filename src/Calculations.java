
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ksiders
 */
public abstract class Calculations {
    
    public static double sumAmount(ArrayList<Ingredient> list){
        double sum = 0;
        for(int i = 0; i < list.size(); i++){
            sum = sum + list.get(i).getAmount();
        }
        return sum;
    }
    public static double sumCalories(ArrayList<Ingredient> list){
        double sum = 0;
        for(int i = 0; i < list.size(); i++){
            sum = sum + list.get(i).getCalories();
        }
        return sum;
    }
    
    public double coefficient(int oldNumber, int newNumber){
        return newNumber / oldNumber;
    }
    
    public ArrayList<Double> newEach(ArrayList<Double> each, double coefficient){
        for(int i = 0; i < each.size(); i++){
            //each[i] = (int)(each[i] * coefficient);
        }
        return each;
    }
    
    public double eachCalories(double perHundred, double amount){
        return perHundred * amount / 100;
    }
}
