/*
 * Licensed under the Academic Free License (AFL 3.0).
 *     http://opensource.org/licenses/AFL-3.0
 *
 *  This code is distributed to CSULB students in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, other than educational.
 *
 *  2018 Alvaro Monge <alvaro.monge@csulb.edu>
 *
 */

package csulb.cecs323.app;

// Import all of the entity classes that we have written for this application.
import csulb.cecs323.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * A simple application to demonstrate how to persist an object in JPA.
 * <p>
 * This is for demonstration and educational purposes only.
 * </p>
 * <p>
 *     Originally provided by Dr. Alvaro Monge of CSULB, and subsequently modified by Dave Brown.
 * </p>
 * Licensed under the Academic Free License (AFL 3.0).
 *     http://opensource.org/licenses/AFL-3.0
 *
 *  This code is distributed to CSULB students in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, other than educational.
 *
 *  2021 David Brown <david.brown@csulb.edu>
 *
 */
public class CustomerOrders {
   /**
    * You will likely need the entityManager in a great many functions throughout your application.
    * Rather than make this a global variable, we will make it an instance variable within the CustomerOrders
    * class, and create an instance of CustomerOrders in the main.
    */
   private EntityManager entityManager;

   /**
    * The Logger can easily be configured to log to a file, rather than, or in addition to, the console.
    * We use it because it is easy to control how much or how little logging gets done without having to
    * go through the application and comment out/uncomment code and run the risk of introducing a bug.
    * Here also, we want to make sure that the one Logger instance is readily available throughout the
    * application, without resorting to creating a global variable.
    */
   private static final Logger LOGGER = Logger.getLogger(CustomerOrders.class.getName());

   /**
    * The constructor for the CustomerOrders class.  All that it does is stash the provided EntityManager
    * for use later in the application.
    * @param manager    The EntityManager that we will use.
    */
   public CustomerOrders(EntityManager manager) {
      this.entityManager = manager;
   }

   public static void main(String[] args) {
      LOGGER.fine("Creating EntityManagerFactory and EntityManager");
      EntityManagerFactory factory = Persistence.createEntityManagerFactory("CustomerOrders");
      EntityManager manager = factory.createEntityManager();
      // Create an instance of CustomerOrders and store our new EntityManager as an instance variable.
      CustomerOrders customerOrders = new CustomerOrders(manager);

      // Any changes to the database need to be done within a transaction.
      // See: https://en.wikibooks.org/wiki/Java_Persistence/Transactions

      LOGGER.fine("Begin of Transaction");
      EntityTransaction tx = manager.getTransaction();

      tx.begin();
      // List of Products that I want to persist.  I could just as easily done this with the seed-data.sql
      List <Products> products = new ArrayList<Products>();
      // Load up my List with the Entities that I want to persist.  Note, this does not put them
      // into the database.
      products.add(new Products("076174517163", "16 oz. hickory hammer", "Stanely Tools", "1", 9.97, 50));
      products.add(new Products("000000000001", "16 oz. spatula", "Waterfall Tools", "3", 3.50, 50));
      products.add(new Products("076174533211", "16 oz. bolts", "Hardware Place", "10", 4.20, 50));
      products.add(new Products("022222222222", "16 oz. anvil", "Drop Stuff", "2", 100.10, 50));
      // Create the list of owners in the database.

      List<Customers> customers = new ArrayList<Customers>();
      customers.add(new Customers("Shirley", "Cho", "555-555-5555", "hello st", "91770"));
      customers.add(new Customers("Shi", "C", "555-555-5554", "hello st", "91770"));
      customers.add(new Customers("Shirl", "Ch", "555-555-5553", "hello st", "91770"));
      customers.add(new Customers("Shelly", "Choo", "555-555-5552", "hello st", "91770"));

      customerOrders.createEntity (products);
      customerOrders.createEntity(customers);
      tx.commit();
      Scanner in = new Scanner(System.in);

      boolean go = true;

      while(go)
      {

         System.out.println("Starting an order! ... ");
         System.out.println("(0)Begin or (1)Exit program:");
         int terminate = in.nextInt();
         if(terminate == 0) {
            // tx.begin();

            //THESE SHOULD NOT BE PERSISTED EVER. TEMPORARY PARALLEL ARRAYS FOR FINAL ORDERED PRODUCTS/ORDERLINES.
            List<Products> orderProducts = new ArrayList<Products>();
            List<Integer> orderQuantity = new ArrayList<Integer>();
            List<Double> orderUnitPrice = new ArrayList<Double>();
            List<Order_lines> order_lines = new ArrayList<Order_lines>();

            // Commit the changes so that the new data persists and is visible to other users.

            //ask for customer identity and product identity
            Customers customer = customerOrders.selectCustomer(in);

            //THIS SHOULD GET PERSISTED IF THE ORDER GOES THROUGH.
            //ask to start order
            System.out.println("Starting your order: ");
            Orders order = customerOrders.getOrderTime(customer);
            boolean productsGo = true;

            while (productsGo) {

               if(!order_lines.isEmpty()){
                  for(Order_lines orderLine : order_lines){
                     System.out.println(orderLine);
                  }
               }

               // Select product
               Products productForOrder = customerOrders.selectProduct(in);
               //TESTING IF I CAN POPULATE ORDERLINES AND STUFF
//            customerOrders.entityManager.persist(order);
//            Order_lines o = new Order_lines();
//            o.setProduct(productForOrder);
//            o.setOrder(order);
//            o.setQuantity(10);
//            o.setUnit_sale_price(3.2);
//            System.out.println(o);
//            customerOrders.entityManager.persist(o);

               //Testing Check In Stock function and that you can print out product list again with missing product since
               //you already ordered it.

               // Get quantity

               // Select quantity
               System.out.println("Enter Quantity: ");
               int quantity = in.nextInt();

               // Check if the quantity is within inStockNumber
               if (!customerOrders.checkInStock(productForOrder.getUPC(), quantity)) {
                  //ask if they want the rest
                  System.out.println("0: Want All");
                  //ask if they don't want the product
                  System.out.println("1: Want None");
                  //ask to abort
                  System.out.println("2: Don't want to order anything anymore");
                  //SELECT OPTION. ?switch case> >.> or something.
                  System.out.println("Select:");
                  int option = in.nextInt();
                  switch (option) {
                     //want all
                     case 0: {
                        System.out.println("I'm putting in all...");
                        orderProducts.add(productForOrder);
                        orderQuantity.add(productForOrder.getUnits_in_stock());
                        orderUnitPrice.add(3.00);
                        break;
                     }
                     //want none
                     case 1: {
                        System.out.println("I did not add this to your order...");
                        //do nothing?
                        break;
                     }
                     //want to stop ordering completely
                     case 2: {
                        System.out.println("Deleting your order...");
                        orderProducts.clear();
                        orderQuantity.clear();
                        orderUnitPrice.clear();
                        productsGo = false;
                        break;
                        //go = false;
                        //tx.commit();
                     }
                  }
               } else {
                  System.out.println("AHHHHHH! it's less");
                  //DO NOT PERSIST THIS STUFF. THESE ARE PARALLEL ARRAYS. MAN.
                  //USE INFO TO PERSIST NEW PRODUCTS MADE FROM THIS INFO. (Don't persist the products?)
                  order_lines.add(new Order_lines(order, productForOrder, quantity, productForOrder.getUnit_list_price()));
                  //Not sure if we're adding money here.
                  orderUnitPrice.add(3.00);
               }

               System.out.println("Order another product? 0(no) 1(yes)");
               int endProductCycle = in.nextInt();

               //
               if (endProductCycle == 0) {
                  productsGo = false;
                  if (!orderProducts.isEmpty()) {
                     customerOrders.entityManager.persist(order);
                     for (int i = 0; i < orderProducts.size(); i++) {
                        Order_lines o = new Order_lines();
                        o.setProduct(orderProducts.get(i));
                        o.setOrder(order);
                        o.setQuantity(orderQuantity.get(i));
                        o.setUnit_sale_price(orderUnitPrice.get(i));

                        //change quantity
                        orderProducts.get(i).setUnits_in_stock(orderProducts.get(i).getUnits_in_stock()-orderQuantity.get(i));
                        customerOrders.entityManager.persist(orderProducts.get(i));
                        customerOrders.entityManager.persist(o);
                     }
                  }

                  //tx.commit();
               }
            }//end of asking for products
         }//end of that if
         else if(terminate == 1)
         {
            go = false;
         }

         //tx.commit();
      }//end of ordering loop
      tx.commit();
      LOGGER.fine("End of Transaction");
   } // End of the main method

   /**
    * Create and persist a list of objects to the database.
    * @param entities   The list of entities to persist.  These can be any object that has been
    *                   properly annotated in JPA and marked as "persistable."  I specifically
    *                   used a Java generic so that I did not have to write this over and over.
    */
   public <E> void createEntity(List <E> entities) {
      for (E next : entities) {
         LOGGER.info("Persisting: " + next);
         // Use the CustomerOrders entityManager instance variable to get our EntityManager.
         this.entityManager.persist(next);
      }

      // The auto generated ID (if present) is not passed in to the constructor since JPA will
      // generate a value.  So the previous for loop will not show a value for the ID.  But
      // now that the Entity has been persisted, JPA has generated the ID and filled that in.
      for (E next : entities) {
         LOGGER.info("Persisted object after flush (non-null id): " + next);
      }
   } // End of createEntity member method

   /**
    * Think of this as a simple map from a String to an instance of Products that has the
    * same name, as the string that you pass in.  To create a new Cars instance, you need to pass
    * in an instance of Products to satisfy the foreign key constraint, not just a string
    * representing the name of the style.
    * @param scanner        The name of the product that you are looking for.
    * @return           The Products instance corresponding to that UPC.
    */
   public Products selectProduct (Scanner scanner) {
      // Run the native query that we defined in the Products entity to find the right style.
      List<Products> products = this.entityManager.createNamedQuery("ReturnProducts",
              Products.class).getResultList();

      System.out.println("Products:");
      for (int i  = 0; i < products.size(); i++)
      {
         System.out.println(i + " " + products.get(i));
      }

      System.out.println("Select number");

      int prodNum = scanner.nextInt();

      while(prodNum < 0 || prodNum >= products.size()) {
         System.out.println("Invalid Number. Please input valid option");
         prodNum = scanner.nextInt();
      }

      return getProduct(products.get(prodNum).getUPC());
   }// End of the getProduct method

   public Products getProduct(String UPC){
      return this.entityManager.createNamedQuery("ReturnProduct", Products.class).setParameter(1, UPC).getSingleResult();
   }

   /**Checks if the product has the quantity. it returns the product back if it does, else returns null
    * Precondition: We should already have the product object we want, but we pass in upc with product.getUPC
    * RETURNS NULL IF QUANITY IS TOO HIGH. RETURNS THE PRODUCT FROM DATABASE BACK IF QUANITY IS FINE*/
   public boolean checkInStock (String UPC, int quantity) {
      // Gets the product
      Products products = this.entityManager.createNamedQuery("ReturnProduct",
              Products.class).setParameter(1, UPC).getSingleResult();

      return quantity <= products.getUnits_in_stock();
   }// End of the checkInStock method

   /**Puts a list of all current customers in database and lets you pick one.
    * Returns a single customer object*/
   public Customers selectCustomer(Scanner scanner)
   {
      // Query for all customers
      List<Customers> customers = this.entityManager.createNamedQuery("ReturnCustomers",
              Customers.class).getResultList();

      System.out.println("Customers:");
      for (int i  = 0; i < customers.size(); i++)
      {
         System.out.println(i + " " + customers.get(i));
      }

      System.out.println("Hi: Who are you: Select number");

      int custNum = scanner.nextInt();

      while(custNum < 0 || custNum >= customers.size()) {
         System.out.println("Invalid Number. Please input valid customer number");
         custNum = scanner.nextInt();
      }

      return getCustomer(customers.get(custNum).getCustomer_id());
   }

   public Customers getCustomer(long custNum){
      return this.entityManager.createNamedQuery("GetCustomer", Customers.class).setParameter(1, custNum).getSingleResult();
   }

   /**Puts a list of all current products in database and lets you pick one.
    * Returns a single product object*/
   public Products displayAllProducts(List<Products> productsInOrder)
   {
      List<Products> products = this.entityManager.createNamedQuery("ReturnProducts",
              Products.class).getResultList();
      if(products.size() == 0)
      {
         return null;
      }
      System.out.println("Products:");
      int count = 0;
      for (int i  = 0; i < products.size(); i++)
      {
         //only prints out products that were not in the products array list passed in
         //cause we technically are ordering /have ordered these currently.
         if(productsInOrder.contains(products.get(i)) )
         {}
         else {
            System.out.println(i + " " + products.get(i));
            count++;
         }
      }

      //asks for product number from list in console.
      Scanner in = new Scanner(System.in);
      int prod = -1;
      while(prod < 0 || prod >= products.size()) {
         System.out.println("Enter Product: ");
         prod = in.nextInt();
      }

      System.out.println(prod + ": " + products.get(prod));
      return products.get(prod);
   }

   /**Takes in a customer and creates an Order object with constructor and localdate and time with soldby person
    * Right now it mostly has default params tbh.
    * Returns Order object back*/
   public Orders getOrderTime(Customers cust)
   {
      //literally just defaults to current date and time. kind of confusing what he asked for us to do.
      //since he said no future dates??
      System.out.println("Order Date & Time: ");
      LocalDateTime currentDate;
      Scanner in = new Scanner(System.in);

      int defaultTime = -1;
      while(defaultTime != 0 && defaultTime != 1)
      {
         System.out.println("Default Present Time: 0(yes)/1(no)");
         defaultTime = in.nextInt();


      }
      if(defaultTime == 0)
      {
         currentDate = LocalDateTime.now();
      }
      else
      {
         //int year = 0;
//         int month= 0;
//         int dayOfMonth = 0;
//        Date d = new Date(year,month, dayOfMonth);
//        Time t = new Time(12,5,3);
//         currentDate = new LocalDateTime(d,t);
         currentDate = LocalDateTime.now();
      }
      return new Orders(cust,currentDate,"Shirley");

   }
} // End of CustomerOrders class
