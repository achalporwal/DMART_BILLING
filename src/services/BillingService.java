package services;
import models.Bill;
import models.BillItem;
import models.Product;
import repositories.BillRepository;
import repositories.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class BillingService
{
private ProductRepository productRepo;
private BillRepository billRepo;
public BillingService()
{
this.productRepo = new ProductRepository();
this.billRepo = new BillRepository();
}
public boolean processCheckout(String customerName, String cashierId, String[] productIds, int[] quantities) {
System.out.println("\nBilling Service Started");
String billId = "BILL-" + System.currentTimeMillis(); 
List<BillItem> billItems = new ArrayList<>();
BigDecimal grandTotal = new BigDecimal("0.00");
for (int i = 0; i < productIds.length; i++)
{
String pId = productIds[i];
int qty = quantities[i];
Product product = productRepo.getProductById(pId);
if (product != null)
{
BigDecimal qtyBD = new BigDecimal(qty);
BigDecimal baseAmount = product.getPrp().multiply(qtyBD);
BigDecimal gstAmount = baseAmount.multiply(product.getGstPercentage()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
BigDecimal halfGst = gstAmount.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
BigDecimal finalItemAmount = baseAmount.add(gstAmount);
grandTotal = grandTotal.add(finalItemAmount);
String itemId = "ITM-" + System.currentTimeMillis() + i;
BillItem item = new BillItem(itemId, billId, pId, qty,product.getMrp(), product.getPrp(),baseAmount, halfGst, halfGst, new BigDecimal("0.00"),finalItemAmount);
billItems.add(item);
System.out.println("Added: " + product.getProductName() + " | Qty: " + qty + " | Final Price: Rs " + finalItemAmount);
}
else
{
System.out.println("Error: Product ID " + pId + " not found!");
}
}
Bill finalBill = new Bill();
finalBill.setBillId(billId);
finalBill.setCustomerName(customerName);
finalBill.setCashierId(cashierId);
for (BillItem item : billItems) {
finalBill.addItem(item);
}
finalBill.setFinalAmount(grandTotal);
boolean isSaved = billRepo.saveCompleteBill(finalBill);
if (isSaved)
{
System.out.println("Bill successfully generated and saved!");
System.out.println("Grand Total: Rs " + grandTotal);
return true;
}
else 
{
System.out.println("Failed to save bill in database.");
return false;
}
}
}