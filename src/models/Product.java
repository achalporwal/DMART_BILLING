package models;
import java.math.BigDecimal;
public class Product
{
private String productId;
private String productName;
private BigDecimal mrp;
private BigDecimal prp;
private BigDecimal gstPercentage;
private int availableQuantity;
private int alertThreshold;
private int heldQuantity;
public Product()
{
}
public Product(String productId, String productName, BigDecimal mrp, BigDecimal prp, BigDecimal gstPercentage, int availableQuantity)
{
this(productId, productName, mrp, prp, gstPercentage, availableQuantity, 10, 0);
}
public Product(String productId, String productName, BigDecimal mrp, BigDecimal prp, BigDecimal gstPercentage, int availableQuantity, int alertThreshold)
{
this(productId, productName, mrp, prp, gstPercentage, availableQuantity, alertThreshold, 0);
}
public Product(String productId, String productName, BigDecimal mrp, BigDecimal prp, BigDecimal gstPercentage, int availableQuantity, int alertThreshold, int heldQuantity)
{
this.productId = productId;
this.productName = productName;
this.mrp = mrp;
this.prp = prp;
this.gstPercentage = gstPercentage;
this.availableQuantity = availableQuantity;
this.alertThreshold = alertThreshold;
this.heldQuantity = heldQuantity;
}
public int getAlertThreshold()
{
return alertThreshold;
}
public void setAlertThreshold(int alertThreshold) 
{ 
this.alertThreshold = alertThreshold; 
}
public int getHeldQuantity() 
{ 
return heldQuantity; 
}
public void setHeldQuantity(int heldQuantity)
{
this.heldQuantity = heldQuantity;
}
public String getProductId()
{
return productId;
}
public void setProductId(String productId)
{
this.productId = productId;
}
public String getProductName()
{
return productName;
}
public void setProductName(String productName)
{
this.productName = productName;
}
public BigDecimal getMrp()
{
return mrp;
}
public void setMrp(BigDecimal mrp)
{
this.mrp = mrp;
}
public BigDecimal getPrp()
{
return prp;
}
public void setPrp(BigDecimal prp)
{
this.prp = prp;
}
public BigDecimal getGstPercentage()
{
return gstPercentage;
}
public void setGstPercentage(BigDecimal gstPercentage)
{
this.gstPercentage = gstPercentage;
}
public int getAvailableQuantity()
{
return availableQuantity;
}
public void setAvailableQuantity(int availableQuantity)
{
this.availableQuantity = availableQuantity;
}
}
