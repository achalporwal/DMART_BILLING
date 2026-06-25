package services;

import db.DatabaseConfig;
import models.Bill;
import models.BillItem;
import models.Product;
import repositories.BillRepository;
import repositories.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReturnService {
private final BillRepository billRepository = new BillRepository();
private final ProductRepository productRepository = new ProductRepository();

public Bill processReturn(String billId, String productId, int returnQty) throws Exception {
if (returnQty <= 0) {
throw new Exception("Return quantity must be greater than zero.");
}

Connection conn = null;
try {
conn = DatabaseConfig.getConnection();
conn.setAutoCommit(false);

Bill bill = billRepository.findById(billId);
if (bill == null) {
throw new Exception("Invoice " + billId + " not found.");
}

List<BillItem> items = billRepository.findItemsByBillId(billId);
BillItem targetItem = null;
for (BillItem item : items) {
if (item.getProductId().equals(productId)) {
targetItem = item;
break;
}
}

if (targetItem == null) {
throw new Exception("Product ID " + productId + " is not part of this invoice.");
}

if (targetItem.getQuantity() < returnQty) {
throw new Exception("Cannot return more than purchased. Purchased: " + targetItem.getQuantity() + ", Requested return: " + returnQty);
}

boolean replenished = productRepository.replenishQuantity(productId, returnQty, conn);
if (!replenished) {
throw new Exception("Failed to replenish product quantity.");
}

int newQty = targetItem.getQuantity() - returnQty;
if (newQty == 0) {
billRepository.deleteBillItem(targetItem.getBillItemId(), conn);
} else {
BigDecimal newQtyBd = BigDecimal.valueOf(newQty);
BigDecimal mrp = targetItem.getMrp();
BigDecimal prp = targetItem.getPrp();

Product product = productRepository.findById(productId);
BigDecimal gstPct = product != null ? product.getGstPercentage() : BigDecimal.valueOf(18); // fallback

BigDecimal finalAmount = prp.multiply(newQtyBd).setScale(2, RoundingMode.HALF_UP);

BigDecimal onePlusGstDiv100 = BigDecimal.ONE.add(gstPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
BigDecimal taxableValue = finalAmount.divide(onePlusGstDiv100, 2, RoundingMode.HALF_UP);

BigDecimal totalGst = finalAmount.subtract(taxableValue);
BigDecimal cgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
BigDecimal sgst = totalGst.subtract(cgst);

BigDecimal discount = mrp.multiply(newQtyBd).subtract(finalAmount).setScale(2, RoundingMode.HALF_UP);

targetItem.setQuantity(newQty);
targetItem.setTaxableValue(taxableValue);
targetItem.setCgst(cgst);
targetItem.setSgst(sgst);
targetItem.setDiscount(discount);
targetItem.setFinalAmount(finalAmount);

billRepository.updateBillItem(targetItem, conn);
}

List<BillItem> remainingItems = billRepository.findItemsByBillId(billId);
List<BillItem> updatedItems = new ArrayList<>();
BigDecimal totalTaxable = BigDecimal.ZERO;
BigDecimal totalCgst = BigDecimal.ZERO;
BigDecimal totalSgst = BigDecimal.ZERO;
BigDecimal totalDiscount = BigDecimal.ZERO;
BigDecimal totalFinal = BigDecimal.ZERO;

for (BillItem item : remainingItems) {
if (item.getProductId().equals(productId)) {
if (newQty > 0) {
updatedItems.add(targetItem);
totalTaxable = totalTaxable.add(targetItem.getTaxableValue());
totalCgst = totalCgst.add(targetItem.getCgst());
totalSgst = totalSgst.add(targetItem.getSgst());
totalDiscount = totalDiscount.add(targetItem.getDiscount());
totalFinal = totalFinal.add(targetItem.getFinalAmount());
}
} else {
updatedItems.add(item);
totalTaxable = totalTaxable.add(item.getTaxableValue());
totalCgst = totalCgst.add(item.getCgst());
totalSgst = totalSgst.add(item.getSgst());
totalDiscount = totalDiscount.add(item.getDiscount());
totalFinal = totalFinal.add(item.getFinalAmount());
}
}

BigDecimal refundAmount = bill.getFinalAmount().subtract(totalFinal);
if ("CASH".equalsIgnoreCase(bill.getPaymentMode()) && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
bill.setCashReturned(bill.getCashReturned().add(refundAmount));
}

bill.setTaxableValue(totalTaxable);
bill.setCgst(totalCgst);
bill.setSgst(totalSgst);
bill.setDiscount(totalDiscount);
bill.setFinalAmount(totalFinal);
bill.setItems(updatedItems);
bill.setStatus("REVISED"); // Mark as revised

billRepository.updateBill(bill, conn);

conn.commit();
return bill;

} catch (Exception e) {
if (conn != null) {
try { conn.rollback(); } catch (SQLException ex) {
System.err.println("Return rollback failed: " + ex.getMessage());
}
}
throw e;
} finally {
if (conn != null) {
try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
DatabaseConfig.releaseConnection(conn);
}
}
}
}
