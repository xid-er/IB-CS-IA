
import java.util.Comparator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ksiders
 */
public class Ingredient implements Comparable{
    private String name;
    private double calories;
    private double amountIngredient;
    
    public Ingredient(String name, double calculatedCalories, double amount){
        this.name = name;
        this.calories = calculatedCalories;
        this.amountIngredient = amount;
    }
    
    public String getName(){
        return name;
    }
    
    public double getAmount(){
        return amountIngredient;
    }
    
    public double getCalories(){
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setAmountIngredient(double amountIngredient) {
        this.amountIngredient = amountIngredient;
    }
    
    @Override
    public int compareTo(Object i){
        Ingredient ing = (Ingredient)i;
        return this.getName().compareTo(ing.getName());
    }
    
}
