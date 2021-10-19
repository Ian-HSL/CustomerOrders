package csulb.cecs323.model;

import javax.persistence.*;
/*
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

@NamedNativeQuery(
        name="ReturnProduct",
        query = "SELECT * " +
                "FROM   products " +
                "WHERE  UPC = ? ",
        resultClass = Products.class
)

@NamedNativeQuery(
        name="GetInventory",
        query = "SELECT * " +
                "FROM   PRODUCTS " +
                "WHERE NOT units_in_stock = 0"
        ,
        resultClass = Products.class
)

@NamedNativeQuery(
        name="CheckInStock",
        query = "SELECT * " +
                "FROM   PRODUCTS " +
                "WHERE  UPC = ? AND units_in_stock >= ? ",
        resultClass = Products.class
)

@SqlResultSetMapping(name = "updateResult", columns = {@ColumnResult(name = "count")})

@NamedNativeQuery(
        name = "UpdateInventory",
        query = "UPDATE products " +
                "SET units_in_stock = ?" +
                "WHERE UPC = ? ",
        resultSetMapping = "updateResult"
)

/** Something that we stock, that the customer can order. */
@Entity
public class Products {
    @Id
    @Column(nullable = false, length = 30)
    /** The Product Universal Product Code */
    private String UPC;

    @Column(nullable = false, length = 128)
    /** Descriptive name for the product */
    private String prod_name;

    @Column(nullable = false, length = 40)
    /** The name of the manufacturer. */
    private String mfgr;

    @Column(nullable = false, length = 20)
    /** The manufacturer's model number for this product. */
    private String model;

    @Column(nullable = false)
    /** Price in US $ */
    private double unit_list_price;

    @Column(nullable = false)
    /** The quantity of this item that we have on hand. */
    private int units_in_stock;

    public Products(String UPC, String prod_name, String mfgr, String model, double unit_list_price, int units_in_stock) {
        this.UPC = UPC;
        this.prod_name = prod_name;
        this.mfgr = mfgr;
        this.model = model;
        this.unit_list_price = unit_list_price;
        this.units_in_stock = units_in_stock;
    }

    public Products() {}

    public String getUPC() {
        return UPC;
    }

    public void setUPC(String UPC) {
        this.UPC = UPC;
    }

    public String getProd_name() {
        return prod_name;
    }

    public void setProd_name(String prod_name) {
        this.prod_name = prod_name;
    }

    public String getMfgr() {
        return mfgr;
    }

    public void setMfgr(String mfgr) {
        this.mfgr = mfgr;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getUnit_list_price() {
        return unit_list_price;
    }

    public void setUnit_list_price(double unit_list_price) {
        this.unit_list_price = unit_list_price;
    }

    public int getUnits_in_stock() {
        return units_in_stock;
    }

    public void setUnits_in_stock(int units_in_stock) {
        this.units_in_stock = units_in_stock;
    }

    @Override
    public String toString () {
        return "Product- UPC: " + this.UPC + ", Name: " + this.prod_name + ", Price: " + this.unit_list_price
                + " QTY on hand: " + this.units_in_stock;
    }
}
