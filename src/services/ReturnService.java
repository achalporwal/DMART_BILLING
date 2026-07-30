package services;
import models.Product;
import repositories.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class ReturnService
{
private ProductRepository productRepo;
public ReturnService()
{
this.productRepo = new ProductRepository();
}
public void processReturn(String productId, int quantityToReturn)
{
System.out.println("\n---Return Service Started ---");
Product product = productRepo.getProductById(productId);
if (product != null)
{
BigDecimal qtyBD = new BigDecimal(quantityToReturn);
BigDecimal baseRefund = product.getPrp().multiply(qtyBD);
BigDecimal gstAmount = baseRefund.multiply(product.getGstPercentage()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
BigDecimal totalRefund = baseRefund.add(gstAmount);
System.out.println("Item Returned: " + product.getProductName());
System.out.println("Quantity Returned: " + quantityToReturn);
System.out.println("Total Refund Amount to Customer: Rs " + totalRefund);
System.out.println("Return successfully processed.");
}
else
{
System.out.println("Error: Product ID " + productId + " not found in database!");
}
}
}