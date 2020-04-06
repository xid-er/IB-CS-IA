/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.apache.derby.jdbc.EmbeddedDriver;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;
/**
 *
 * @author ksiders
 */
public class IAFile extends javax.swing.JFrame {

    Connection con;
    Statement stmt;
    ResultSet rs;
    boolean works = true;
    boolean finished = false;
    int curRow = 0;
    String[] dataArray;
    DefaultListModel listModel;
    ArrayList<Ingredient> ingredientArray = new ArrayList<>();
    ArrayList<Ingredient> calorieArray = new ArrayList<>();
    /**
     * Creates new form IAFile
     */
    public IAFile() {
        listModel = new DefaultListModel();
        initComponents();
        btnChange.setEnabled(false);
        txtFinalCal.setEnabled(false);
        txtFinalAmount.setEnabled(false);
        txtCal.setEnabled(false);
        btnAddToList.setEnabled(false);
        txtPerson.setEnabled(false);
        hints();
        
        listIngredients.addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                listValueChanged(arg0);
            }
        });
        createDatabase();
        try{
            // Better safe than sorry
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /*System.out.println("Smth wrong with closing con in constructor"); e.printStackTrace();*/}
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) { /*System.out.println("Smth wrong with closing con in constructor"); e.printStackTrace();*/}
            }
            if (con != null) {
                try {
                    con.commit();
                    con.close();
                } catch (SQLException e) { /*System.out.println("Smth wrong with closing con in constructor"); e.printStackTrace();*/}
            }
            
            con = DriverManager.getConnection("jdbc:derby:Ingredients", "username", "password");
            
            // Combo box with available ingredients
            con.setAutoCommit(false);
            stmt = con.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
            String SQL = "SELECT * FROM Ing3 ORDER BY Ingredient_Name";
            rs = stmt.executeQuery(SQL);
            con.commit();
            boxIngredients.addItem("-");
            while(rs.next()){
                boxIngredients.addItem(rs.getString("Ingredient_Name"));
            }
            
            boxIngredients.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    DefaultComboBoxModel d=(DefaultComboBoxModel) boxIngredients.getModel();
                    if(d.getSize() != 0){  
                        displayCalorieStandard();
                        String selectedIng = boxIngredients.getSelectedItem().toString();
                        if(!selectedIng.equals("-") && !finished){
                            btnAddToList.setEnabled(true);
                        }
                        else btnAddToList.setEnabled(false);
                        
                        for(int i = 0; i < ingredientArray.size(); i++){
                            if(ingredientArray.get(i).getName().equals(selectedIng)){
                                btnAddToList.setEnabled(false);
                                break;
                            }
                        }
                    }
                }
            });
        }
        catch (Exception e){
            /*
            System.out.println("Test() constructor error");
            System.out.println(e);
            System.out.println("... in line: " + e.getStackTrace()[0].getLineNumber());
            */
            e.printStackTrace();
        }
        finally{
            try { stmt.close(); } catch (Exception e) { /*System.out.println("stmt in cons"); */}
        }
    }

    public void createDatabase(){
        PreparedStatement pstmt;
      
        String createSQL = "create table Ing3 ("
        + "id integer not null generated always as"
        + " identity (start with 1, increment by 1),   "
        + "Ingredient_Name varchar(40) not null, Cal_Standard decimal(30, 2),"
        + "constraint primary_key primary key (id))";
      
        try {
            // Decide on the db system directory: <userhome>/.addressbook/
            String userHomeDir = System.getProperty("user.home", ".");
            String systemDir = userHomeDir + "/Recipes";

            // Set the db system directory.
            System.setProperty("derby.system.home", systemDir);
            
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            
            Driver derbyEmbeddedDriver = new EmbeddedDriver();
            DriverManager.registerDriver(derbyEmbeddedDriver);

            con = DriverManager.getConnection("jdbc:derby:Ingredients;create=true", "username", "password");
            con.setAutoCommit(false);

            try{
                stmt = con.createStatement();
                stmt.execute(createSQL);
            
                pstmt = con.prepareStatement("insert into Ing3 (Ingredient_Name, Cal_Standard) values(?,?)");
                pstmt.setString(1, "Butter");
                pstmt.setDouble(2, 716.8);
                pstmt.executeUpdate();

                pstmt = con.prepareStatement("insert into Ing3 (Ingredient_Name, Cal_Standard) values(?,?)");
                pstmt.setString(1, "Sugar");
                pstmt.setDouble(2, 386.7);
                pstmt.executeUpdate();
         
                pstmt = con.prepareStatement("insert into Ing3 (Ingredient_Name, Cal_Standard) values(?,?)");
                pstmt.setString(1, "Flour");
                pstmt.setDouble(2, 364);
                pstmt.executeUpdate();

                pstmt = con.prepareStatement("insert into Ing3 (Ingredient_Name, Cal_Standard) values(?,?)");
                pstmt.setString(1, "Eggs");
                pstmt.setDouble(2, 155.1);
                pstmt.executeUpdate();
                con.commit();
            } catch(SQLException e){
                if(e.toString().contains("already exists")){
//                    System.out.println("Database exists!");
//                    System.out.println(e);
                }
                else throw e;
            }

        } catch (SQLException | ClassNotFoundException ex) {
            //System.out.println("in connection " + ex);
        } finally {
            //System.out.println("after close:" + con==null);
        }
    }
    
    public void displayCalorieStandard(){
        try{
            String ingredient = boxIngredients.getSelectedItem().toString();
            stmt = con.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
            String SQL = "SELECT * FROM Ing3 WHERE Ingredient_Name = '"+ingredient+"'";
            ResultSet rs = stmt.executeQuery(SQL);
            if(rs.next()){
                txtCal.setText(rs.getString("Cal_Standard"));
            }
            
        }
        catch(Exception e){
//            System.out.println("displayCalorieStandard error");
//            System.out.println(e);
        }
        
    }
    
    public void listValueChanged(ListSelectionEvent arg0){
        
                if (!arg0.getValueIsAdjusting()) {
                    //Trims String
                    int selectedIndex = listIngredients.getSelectedIndex();
                    if (selectedIndex != -1 && btnFinish.isEnabled()) {
                        String tempString = removeSpaces(
                                listModel.getElementAt(selectedIndex)
                                        .toString());
                        String[] tempArray = tempString.split(" ");
                        
                        String ingName = tempArray[0].trim();
                        int option = JOptionPane.showConfirmDialog(
                            null, 
                            "Do you really want to remove " + ingName + " from the recipe?", 
                            "Remove " + ingName, 
                            JOptionPane.OK_CANCEL_OPTION);
                        if(option == JOptionPane.OK_OPTION && !finished){
                            btnAddToList.setEnabled(true);
                            Collections.sort(ingredientArray);
                            int index = Arrays.binarySearch(ingredientArray.toArray(), new Ingredient(ingName, 0, 0));
                            ingredientArray.remove(index);
                            listModel.remove(selectedIndex);
                            txtFinalAmount.setText(Calculations.sumAmount(ingredientArray)+"");
                            txtFinalCal.setText(Calculations.sumCalories(ingredientArray)+"");
                        }
                        else{
                            listIngredients.clearSelection();
                        }
                    }
                }
            
    }
    
    public void hints(){
        String hint = "If you want to change an ingredient's amount, "
                + "simply remove it and add it with a different amount.";
        listIngredients.setToolTipText(hint);
        
        hint = "If you want to remove all ingredients and start the calculation"
                + " from scratch, click here.";
        btnNewRecipe.setToolTipText(hint);
        
        hint = "If you're sure the recipe is complete and possibly want to"
                + " change the amount, click here.";
        btnFinish.setToolTipText(hint);
        
        hint = "If you want to change the amount of all ingredients in the "
                + "recipe, please click on Finish and then here.";
        btnChange.setToolTipText(hint);
        
        hint = "If you want to add an ingredient to the recipe, please choose "
                + "it from those available in the list, write the necessary "
                + "amount and then click here.";
        btnAddToList.setToolTipText(hint);
        
        hint = "If there isn't a particular ingredient available, you can add "
                + "it by clicking on \"Add ingredient to database\".";
        boxIngredients.setToolTipText(hint);
        
        hint = "If you want to check how many calories each person will consume, "
                + "please enter the number of people and then click here.";
        btnDivide.setToolTipText(hint);
        
        hint = "If you want to add an available ingredient, please click here.";
        btnAddToDatabase.setToolTipText(hint);
    }
    /**
     * This method is called from within the constructor to initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblIngredient = new javax.swing.JLabel();
        boxIngredients = new javax.swing.JComboBox<>();
        lblCal = new javax.swing.JLabel();
        txtCal = new javax.swing.JTextField();
        lblChosen = new javax.swing.JLabel();
        btnAddToList = new javax.swing.JButton();
        lblAmount = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        boxMeasure = new javax.swing.JComboBox<>();
        txtFinalAmount = new javax.swing.JTextField();
        lblFinalAmount = new javax.swing.JLabel();
        btnChange = new javax.swing.JButton();
        lblFinalCal = new javax.swing.JLabel();
        txtFinalCal = new javax.swing.JTextField();
        btnAddToDatabase = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listIngredients = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        btnFinish = new javax.swing.JButton();
        btnNewRecipe = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtPeople = new javax.swing.JTextField();
        btnDivide = new javax.swing.JButton();
        txtPerson = new javax.swing.JTextField();
        lblKcal = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(960, 700));
        setPreferredSize(new java.awt.Dimension(960, 700));
        setSize(new java.awt.Dimension(960, 700));

        lblIngredient.setText("Ingredient:");

        lblCal.setText("Calories (per 100g)");

        txtCal.setText("0");

        lblChosen.setText("Chosen");

        btnAddToList.setText("ADD");
        btnAddToList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddToListActionPerformed(evt);
            }
        });

        lblAmount.setText("Amount in recipe");

        txtAmount.setText("1");

        boxMeasure.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "grams", "cups", "tablespoons", "teaspoons" }));

        txtFinalAmount.setText("0");

        lblFinalAmount.setText("Final amount");

        btnChange.setText("Change amount");
        btnChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeActionPerformed(evt);
            }
        });

        lblFinalCal.setText("FINAL CALORIES");

        txtFinalCal.setText("0");

        btnAddToDatabase.setText("Add ingredient to database");
        btnAddToDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddToDatabaseActionPerformed(evt);
            }
        });

        listIngredients.setModel(listModel);
        listIngredients.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listIngredients.setToolTipText("");
        listIngredients.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                hint(evt);
            }
        });
        jScrollPane1.setViewportView(listIngredients);

        jLabel1.setText("Ingredient name                                               | Calories                     | Amount");

        btnFinish.setText("Finish");
        btnFinish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinishActionPerformed(evt);
            }
        });

        btnNewRecipe.setText("New recipe");
        btnNewRecipe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewRecipeActionPerformed(evt);
            }
        });

        jLabel2.setText("How many people are eating?");

        txtPeople.setText("1");

        btnDivide.setText("Calculate for each person!");
        btnDivide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDivideActionPerformed(evt);
            }
        });

        lblKcal.setText("kcal");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(lblIngredient)
                                            .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(boxIngredients, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGap(203, 203, 203)
                                    .addComponent(btnNewRecipe))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(0, 111, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(lblCal)
                                            .addGap(29, 29, 29))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(txtCal, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(36, 36, 36)
                                            .addComponent(boxMeasure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(lblAmount))
                                    .addGap(26, 26, 26)
                                    .addComponent(btnAddToList)
                                    .addGap(72, 72, 72)))
                            .addComponent(lblChosen)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblFinalAmount)
                                .addGap(136, 136, 136))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(txtFinalAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(58, 58, 58)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFinalCal)
                            .addComponent(txtFinalCal, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnChange)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnFinish)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(btnAddToDatabase)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPeople, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(txtPerson)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(lblKcal))
                                .addComponent(btnDivide, javax.swing.GroupLayout.Alignment.LEADING)))
                        .addGap(285, 285, 285))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIngredient)
                    .addComponent(btnAddToDatabase)
                    .addComponent(btnNewRecipe))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(boxIngredients, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAmount)
                    .addComponent(lblCal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(boxMeasure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddToList))
                .addGap(15, 15, 15)
                .addComponent(lblChosen)
                .addGap(7, 7, 7)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFinish))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFinalCal)
                        .addGap(11, 11, 11)
                        .addComponent(txtFinalCal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnChange)
                        .addGap(139, 139, 139)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPeople, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblFinalAmount)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtFinalAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnDivide)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtPerson, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblKcal))))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public Double tryParse(String text) {
        try {
            works = true;
            return Double.parseDouble(text);
        } 
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, 
                "Please input a valid number", 
                "Invalid number", 
                JOptionPane.ERROR_MESSAGE);
            works = false;
            return null;
        }
}
    
    public String removeSpaces(String str){
        String clean = "";
        boolean letter = true;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i)!=' '){
                letter = true;
                clean += str.charAt(i);
            }
            else if(letter == true){
                clean += " ";
                letter = false;
            }
        }
        return clean;
    }
    
    private void refreshComboBox(){
        try{
            stmt = con.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
            String SQL = "SELECT Ingredient_Name, Cal_Standard FROM Ing3 ORDER BY Ingredient_Name";
            ResultSet rs = stmt.executeQuery(SQL);
            con.commit();

            DefaultComboBoxModel d=(DefaultComboBoxModel) boxIngredients.getModel();
            d.removeAllElements();

            d.addElement("-");
            while(rs.next()){
                d.addElement(rs.getString("Ingredient_Name"));
            }
            boxIngredients.setSelectedItem("-");
        }
        catch(SQLException err){
            JOptionPane.showMessageDialog(IAFile.this, err.getMessage());
            //JOptionPane.showMessageDialog(null,"Failed to Connect to Database","Error Connection", 3);
        }
    }
    
    private void btnAddToDatabaseActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        PreparedStatement pstmt;
        // Creates dialog
        JTextField dataIngredient = new JTextField();
        JTextField dataCalories = new JTextField();
        Object[] message = {
            "Ingredient:", dataIngredient,
            "Calories per 100 grams:", dataCalories
        };
        int option = JOptionPane.showConfirmDialog(
            null, 
            message, 
            "Add ingredient to database", 
            JOptionPane.OK_CANCEL_OPTION);
        
        // Validation
        if(
          dataCalories.getText().length() != 0 &&
          dataIngredient.getText().length() != 0){
            tryParse(dataCalories.getText());
            
            if(dataIngredient.getText().length()>30){
                JOptionPane.showMessageDialog(
                    null, 
                    "Please write a name that is 30 characters or less.", 
                    "Add ingredient to database", 
                    JOptionPane.ERROR_MESSAGE);
                works = false;
            }
            if(works && Double.parseDouble(dataCalories.getText())>100000){
                JOptionPane.showMessageDialog(
                    null, 
                    "Please write a number lower than 100 000.", 
                    "Add ingredient to database", 
                    JOptionPane.ERROR_MESSAGE);
                works = false;
            }
            
            if(works && option == JOptionPane.OK_OPTION){

                // Inserts ingredient into database
                try{
                    pstmt = con.prepareStatement("insert into Ing3 (Ingredient_Name, Cal_Standard) values(?,?)");
                    pstmt.setString(1, dataIngredient.getText());
                    pstmt.setDouble(2, Double.parseDouble(dataCalories.getText()));
                    pstmt.executeUpdate();
                    con.commit();

                    // Thank you
                    JOptionPane.showMessageDialog(
                        null, 
                        "Thank you. " + dataIngredient.getText() + " added.", 
                        "Ingredient added", 
                        JOptionPane.INFORMATION_MESSAGE);
                    refreshComboBox();
                }
                catch(Exception e){
                    if(e instanceof DerbySQLIntegrityConstraintViolationException){
                        JOptionPane.showMessageDialog(
                            null, 
                            "Sorry, there already is an ingredient with that name.", 
                            "Database eror", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        JOptionPane.showMessageDialog(
                            null, 
                            "Something is wrong with the database: " + e, 
                            "Database eror", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        else{
            if(option == JOptionPane.OK_OPTION){
                JOptionPane.showMessageDialog(
                    null, 
                    "One or both fields are empty", 
                    "Empty fields", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        }
        
        
    }
    
    private void btnAddToListActionPerformed(java.awt.event.ActionEvent evt) {                                             
        // Variables
        boolean empty = false;
        double calories = 0;
        double amount = 0;
        double calcAmount = 0;
        String measurement = "";
        String ingredient = boxIngredients.getSelectedItem().toString();
        if(txtCal.getText().length() == 0 ||
            txtAmount.getText().length() == 0) empty = true;
        tryParse(txtAmount.getText());
        if(works) amount = Double.parseDouble(txtAmount.getText());
        if(amount > 100000){
            JOptionPane.showMessageDialog(null, 
                "Please input a number less than 100 000", 
                "Invalid number", 
                JOptionPane.ERROR_MESSAGE);
        }
        else if(!empty && works && !finished){
            btnAddToList.setEnabled(false);
            calories = Double.parseDouble(txtCal.getText());
            calcAmount = Double.parseDouble(txtAmount.getText());
            switch(boxMeasure.getSelectedIndex()){
                case 0: calcAmount *= 0.01;
                        break;
                case 1: calcAmount *= 2.5;
                        measurement = " ("+amount+" cups)";
                        amount *= 250;
                        break;
                case 2: measurement = " ("+amount+" tablespoons)";
                        calcAmount *= 0.15;
                        amount *= 15;
                        break;
                case 3: measurement = " ("+amount+" teaspoons)";
                        calcAmount *= 0.05;
                        amount *= 5;
                        break;
            }
            
            double calculatedCalories = calories * calcAmount;
            
            //Adding object
            Ingredient ing = new Ingredient(ingredient, calculatedCalories, amount);
            ingredientArray.add(ing);
            
            String sumAmount = String.format(Locale.US, "%.2f", Calculations.sumAmount(ingredientArray))+"";
            String sumCalories = String.format(Locale.US, "%.2f", Calculations.sumCalories(ingredientArray))+"";
            txtFinalAmount.setText(sumAmount);
            txtFinalCal.setText(sumCalories);
            
            //Adding to list
            String amountString = ""+ String.format(Locale.US, "%.2f", amount);
            String calcCalString = ""+String.format(Locale.US, "%.2f", calculatedCalories);
            String dataString = String.format("%-69s", ingredient) +
                    String.format("%-35s", calcCalString) +
                    amountString +
                    " " + "grams" + measurement;
            listModel.addElement(dataString);
        }
        else{
            if(empty){
                JOptionPane.showMessageDialog(
                null, 
                "One or both fields are empty", 
                "Empty fields", 
                JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void btnFinishActionPerformed(java.awt.event.ActionEvent evt) {     
        finished = true;
        btnAddToList.setEnabled(false);
        btnFinish.setEnabled(false);
        btnChange.setEnabled(true);
    }
    
    private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {                                          
        //Dialog
        JTextField dataOld = new JTextField();
        JTextField dataNew = new JTextField();
        dataOld.setEnabled(false);
        dataOld.setText(txtFinalAmount.getText());
        Object[] message = {
            "Amount now:", dataOld,
            "Change amount:", dataNew
        };
        int option = JOptionPane.showConfirmDialog(
            null, 
            message, 
            "Change amount of the recipe", 
            JOptionPane.OK_CANCEL_OPTION);
        
        //Validation
        if(option == JOptionPane.OK_OPTION){
            if(dataNew.getText().length() != 0){
                tryParse(dataNew.getText());
                
                if(works){
                    double numOld = Double.parseDouble(dataOld.getText());
                    double numNew = Double.parseDouble(dataNew.getText());
                    double coefficient = numNew / numOld;
                    double newCalories;
                    double newAmount;

                    for(int i = 0; i < listModel.getSize(); i++){
                        String tempString = removeSpaces(
                                listModel.getElementAt(i)
                                        .toString());
                        String[] tempArray = tempString.split(" ");
                        newCalories = Double.parseDouble(tempArray[1]) * coefficient;
                        String[] amount = tempArray[2].trim().split(" ");
                        newAmount = Double.parseDouble(amount[0]) * coefficient;
                        ingredientArray.get(i).setCalories(newCalories);
                        ingredientArray.get(i).setAmountIngredient(newAmount);
                    }
                    listModel.clear();
                    for(int i = 0; i < ingredientArray.size(); i++){
                        String amountString = ""+String.format(Locale.US, "%.2f", ingredientArray.get(i).getAmount());
                        String calcCalString = ""+String.format(Locale.US, "%.2f", ingredientArray.get(i).getCalories());
                        String ingredientString = ""+ingredientArray.get(i).getName();
                        String dataString = String.format("%-69s", ingredientString) 
                            + String.format("%-35s", calcCalString) 
                            + amountString
                            + " grams";
                        listModel.addElement(dataString);
                    }
                    String sumAmount = String.format(Locale.US, "%.2f", Calculations.sumAmount(ingredientArray))+"";
                    String sumCalories = String.format(Locale.US, "%.2f", Calculations.sumCalories(ingredientArray))+"";
                    txtFinalAmount.setText(sumAmount+"");
                    txtFinalCal.setText(sumCalories+"");
                }
            }
            else{
                JOptionPane.showMessageDialog(
                    null, 
                    "The field is empty", 
                    "Empty field", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
            
        }
    }                                         

    private void btnNewRecipeActionPerformed(java.awt.event.ActionEvent evt) {                                             
        // TODO add your handling code here:
        btnFinish.setEnabled(true);
        btnChange.setEnabled(false);
        
        ingredientArray.clear();
        listModel.clear();
        txtFinalCal.setText(""+0);
        txtFinalAmount.setText(""+0);
        boxIngredients.setSelectedIndex(0);
        txtCal.setText(""+0);
        txtAmount.setText(""+1);
    }                                            

    private void btnDivideActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
        String peopleString = txtPeople.getText();
        if(peopleString.length() != 0){
            tryParse(peopleString);
            if(works){
                double total = Double.parseDouble(txtFinalCal.getText());
                double people = Double.parseDouble(peopleString);
                
                String formattedNumber = String.format(Locale.US, "%.2f", total/people)+"";
                txtPerson.setText("" + formattedNumber);
            }
        }
        else{
            JOptionPane.showMessageDialog(
                null, 
                "The field is empty", 
                "Empty field", 
                JOptionPane.ERROR_MESSAGE);
        }
    }                                         

    private void hint(java.awt.event.MouseEvent evt) {                      
        // TODO add your handling code here:
        listIngredients.getToolTipText();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(IAFile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(IAFile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(IAFile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(IAFile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new IAFile().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> boxIngredients;
    private javax.swing.JComboBox<String> boxMeasure;
    private javax.swing.JButton btnAddToDatabase;
    private javax.swing.JButton btnAddToList;
    private javax.swing.JButton btnChange;
    private javax.swing.JButton btnDivide;
    private javax.swing.JButton btnFinish;
    private javax.swing.JButton btnNewRecipe;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAmount;
    private javax.swing.JLabel lblCal;
    private javax.swing.JLabel lblChosen;
    private javax.swing.JLabel lblFinalAmount;
    private javax.swing.JLabel lblFinalCal;
    private javax.swing.JLabel lblIngredient;
    private javax.swing.JLabel lblKcal;
    private javax.swing.JList<String> listIngredients;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtCal;
    private javax.swing.JTextField txtFinalAmount;
    private javax.swing.JTextField txtFinalCal;
    private javax.swing.JTextField txtPeople;
    private javax.swing.JTextField txtPerson;
    // End of variables declaration//GEN-END:variables
}
