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
import java.time.LocalDateTime;
import java.util.*;
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

      LOGGER.fine("Begin of Transaction");
      EntityTransaction tx = manager.getTransaction();

      tx.begin();
      // List of Products that I want to persist.  I could just as easily done this with the seed-data.sql
      List <Products> products = new ArrayList<Products>(Arrays.asList(
            new Products("076174517163", "16 oz. hickory hammer", "Stanely Tools", "1", 9.97, 50),
            new Products("000000000001", "16 oz. spatula", "Waterfall Tools", "3", 3.50, 50),
            new Products("076174533211", "16 oz. bolts", "Hardware Place", "10", 4.20, 50),
            new Products("022222222222", "16 oz. anvil", "Drop Stuff", "2", 100.10, 50)
      ));

      // Create the list of owners in the database.
      List<Customers> customers = new ArrayList<Customers>(Arrays.asList(
            new Customers("Shirley", "Cho", "555-555-5555", "hello st", "91770"),
            new Customers("Shi", "C", "555-555-5554", "hello st", "91770"),
            new Customers("Shirl", "Ch", "555-555-5553", "hello st", "91770"),
            new Customers("Shelly", "Choo", "555-555-5552", "hello st", "91770")
      ));

      customerOrders.createEntity (products);
      customerOrders.createEntity(customers);
      tx.commit();

      Scanner in = new Scanner(System.in);

      System.out.println("Starting an order! ... ");
      System.out.println("(0)Begin or (1)Exit program:");
      int terminate = in.nextInt();

      if(terminate == 0) {
         List<Products> inventory = customerOrders.getInventory();
         List<OrderLines> orderLines = new ArrayList<OrderLines>();

         // Ask for customer identity and product identity
         Customers customer = customerOrders.selectCustomer(in);

         // Starting order
         System.out.println("Starting your order: ");
         Orders order = customerOrders.getOrderTime(customer);
         boolean productsGo = true;

         while (productsGo) {
            // Print order lines
            if(!orderLines.isEmpty()){
               System.out.println("Shopping Cart: ");
               customerOrders.displayOrderLines(orderLines);
            }

            // Select product
            Products productForOrder = customerOrders.selectProduct(in, inventory);

            // Get quantity
            System.out.println("Enter Quantity: ");
            int quantity = in.nextInt();

            // Validate quantity
            if (!customerOrders.checkInStock(productForOrder.getUPC(), quantity)) {
               // Display Choices
               System.out.println("0: Want All");
               System.out.println("1: Want None");
               System.out.println("2: Don't want to order anything anymore");

               //SELECT OPTION. ?switch case> >.> or something.
               System.out.println("Select:");
               int option = in.nextInt();
               switch (option) {
                  //want all
                  case 0: {
                     System.out.println("I'm putting in all...");
                     quantity = productForOrder.getUnits_in_stock();

                     // Add order line to list of order lines
                     customerOrders.addOrderLine(orderLines, new OrderLines(order, productForOrder, quantity, productForOrder.getUnit_list_price()));
                     break;
                  }
                  //want none
                  case 1: {
                     System.out.println("I did not add this to your order...");
                     break;
                  }
                  //want to stop ordering completely
                  case 2: {
                     System.out.println("Deleting your order...");
                     productsGo = false;
                     break;
                  }
               }
            } else {
               customerOrders.addOrderLine(orderLines, new OrderLines(order, productForOrder, quantity, productForOrder.getUnit_list_price()));
            }

            System.out.println("Order another product? 0(no) 1(yes)");
            int endProductCycle = in.nextInt();

            // Customer is finished with ordering products
            if (endProductCycle == 0) {
               productsGo = false;
            }
         }//end of asking for products

         double totalOrder = 0.0;

         // Tally up the order
         for(OrderLines orderLine : orderLines){
            totalOrder += orderLine.getQuantity() * orderLine.getUnit_sale_price();
         }

         System.out.println("Here is your current order total: $" + totalOrder);
         customerOrders.displayOrderLines(orderLines);
         System.out.println("Do you want to (0)Place order/ (1)Abort?");
         //Emily parts
         //If abort remember to add this statement in to reset the total for the other customer
            /*
            //reset totalOrder for other customers
                     totalOrder = 0;
             */
         int response = in.nextInt();

         while(response != 1 && response != 0){
            System.out.println("Please choose (0)Place order / (1)abort");
            response = in.nextInt();
         }

         if(response == 1)
         {
            System.out.println("Order not placed");
            //abort transaction
            customerOrders.entityManager.clear();
            System.out.println("Your cart is empty: ");
         } else {
            System.out.println("Order is placed");
            System.out.println("Here is your cart");

            customerOrders.displayOrderLines(orderLines);

            tx.begin();
            customerOrders.updateInventory(products);

            customerOrders.entityManager.persist(order);

            customerOrders.createEntity(orderLines);

            tx.commit();
         }
      }//end of that if
   } // End of the main method

   /**
    * Create and persist a list of objects to the database.
    * @param entities   The list of entities to persist.  These can be any object that has been
    *                   properly annotated in JPA and marked as "persistable."  I specifically
    *                   used a Java generic so that I did not have to write this over and over.
    */
   public <E> void createEntity(List <E> entities) {
      for (E next : entities) {
         //LOGGER.info("Persisting: " + next);
         // Use the CustomerOrders entityManager instance variable to get our EntityManager.
         this.entityManager.persist(next);
      }
   } // End of createEntity member method

   /**
    * This is the product selection menu in which a product entity is chosen and returned.
    * @param scanner        Scanner object being imported in method
    * @param inventory      The list of products
    * @return               The product object that matches
    */
   public Products selectProduct (Scanner scanner, List<Products> inventory)
   {
      System.out.printf("%-10s%-30s%-10s%-15s%n", "Choice", "Products", "Price", "Quantity");
      for (int i  = 0; i < inventory.size(); i++)
      {
         Products product = inventory.get(i);
         System.out.printf("%-10d%-30s$%-10.2f%-15d%n",
                 i,
                 product.getProd_name(),
                 product.getUnit_list_price(),
                 product.getUnits_in_stock());
      }

      System.out.println("Select number");

      int prodNum = scanner.nextInt();

      while(prodNum < 0 || prodNum >= inventory.size()) {
         System.out.println("Invalid Number. Please input valid option");
         prodNum = scanner.nextInt();
      }

      return getProduct(inventory.get(prodNum).getUPC());
   }// End of the getProduct method

   /**
    * Gets product using UPC and returns it.
    * @param UPC     Identifying string of the product
    * @return        The product that matches the input UPC.
    * */
   public Products getProduct(String UPC)
   {
      return this.entityManager.createNamedQuery("ReturnProduct", Products.class).setParameter(1, UPC).getSingleResult();
   }

   /**
    * Gets the inventory as it is from the database.
    * @return        Product list from the database.
    * */
   public List<Products> getInventory(){
      return this.entityManager.createNamedQuery("GetInventory", Products.class).getResultList();
   }

   /**
    * Compare the number of units the product has to the user requested quantity
    * @param UPC           Product to find the units it has.
    * @param quantity      The quantity the user requests.
    * @return              True if there are enough units, false if not.
    */
   public boolean checkInStock (String UPC, int quantity)
   {
      // Gets the product
      Products products = this.entityManager.createNamedQuery("ReturnProduct",
              Products.class).setParameter(1, UPC).getSingleResult();

      return quantity <= products.getUnits_in_stock();
   }// End of the checkInStock method

   /**
    * This is the select customer menu which returns a customer entity.
    * @param scanner       Scanner object that is passed in.
    * @return              Customer entity that the user chose.
    */
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

   /**
    * Gets the customer entity from the database.
    * @param custNum       Identifying number for the customer
    * @return              Customer entity from the database.
    */
   public Customers getCustomer(long custNum)
   {
      return this.entityManager.createNamedQuery("GetCustomer", Customers.class)
              .setParameter(1, custNum).getSingleResult();
   }

   /**
    * Returns order to be started with the sold_by inputted by default.
    * @param cust       Customer to be associated with the order.
    * @return           Order entity to be used with order lines.
    */
   public Orders getOrderTime(Customers cust)
   {
      return new Orders(cust,LocalDateTime.now(),"Shirley");
   }

   /**
    * Adds order line to list of order lines. It does a check whether the product for the order line is
    * already in the list of order lines.
    * @param orderLines       The list of order lines which order line could be added to.
    * @param orderLine        The order line to be evaluated.
    */
   public void addOrderLine(List<OrderLines> orderLines, OrderLines orderLine){
      int position = isInOrder(orderLines, orderLine.getProduct().getUPC());
      if(position >= 0){
         OrderLines line = orderLines.get(position);
         line.setQuantity(line.getQuantity() + orderLine.getQuantity());
      } else {
         orderLines.add(orderLine);
      }

      Products orderLineProduct = orderLine.getProduct();

      orderLineProduct.setUnits_in_stock(orderLineProduct.getUnits_in_stock() - orderLine.getQuantity());
   }

   /**
    * Finds the position of the product in the order lines list.
    * @param orderlines       The list of order lines to be iterated through.
    * @param UPC              The search UPC to be looked for.
    * @return                 A non-negative number if the product is already in list. -1 if it is not.
    */
   public int isInOrder(List<OrderLines> orderlines, String UPC){
      int i = 0;
      for (OrderLines orderline : orderlines){
         if(Objects.equals(orderline.getProduct().getUPC(), UPC)){
            return i;
         }

         i++;
      }

      return -1;
   }

   /**
    * Displays the order lines with total cost at the bottom.
    * @param orderLines       The list of order lines to be displayed.
    */
   public void displayOrderLines(List<OrderLines> orderLines){
      double total = 0;
      System.out.printf("%-25s%-10s%-20s%-10s%n", "Product", "Quantity", "Price Each", "Subtotal");
      for (OrderLines orderLine : orderLines){
         System.out.printf("%-25s%-10d$%-19.2f$%-10.2f%n", orderLine.getProduct().getProd_name(),
                 orderLine.getQuantity(),
                 orderLine.getUnit_sale_price(),
                 orderLine.getUnit_sale_price() * orderLine.getQuantity());
         total += orderLine.getQuantity() * orderLine.getUnit_sale_price();
      }

      System.out.printf("%nTotal%51s%1.2f%n%n", "$", total);
   }

   public void updateInventory(List<Products> inventory){
      for(Products product : inventory){
         this.entityManager.createNamedQuery("UpdateInventory", Products.class)
                 .setParameter(1, product.getUnits_in_stock())
                 .setParameter(2, product.getUPC());
      }
   }
} // End of CustomerOrders class
