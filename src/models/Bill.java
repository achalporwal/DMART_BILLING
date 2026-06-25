package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bill {
private String billId;
private String customerId;
private String cashierId;
private LocalDateTime billDate;
private BigDecimal taxableValue;
private BigDecimal cgst;
private BigDecimal sgst;
private BigDecimal discount;
private BigDecimal finalAmount;
private String paymentMode;
private BigDecimal cashReceived = BigDecimal.ZERO;
private BigDecimal cashReturned = BigDecimal.ZERO;
private String status = "COMPLETED";
private List<BillItem> items = new ArrayList<>();

private String customerName;
private String customerMobile;
private String customerLocation;

public Bill() {}

public Bill(String billId, String customerId, String cashierId, LocalDateTime billDate, 
BigDecimal taxableValue, BigDecimal cgst, BigDecimal sgst, BigDecimal discount, BigDecimal finalAmount) {
this.billId = billId;
this.customerId = customerId;
this.cashierId = cashierId;
this.billDate = billDate;
this.taxableValue = taxableValue;
this.cgst = cgst;
this.sgst = sgst;
this.discount = discount;
this.finalAmount = finalAmount;
}

public String getBillId() {
return billId;
}

public void setBillId(String billId) {
this.billId = billId;
}

public String getCustomerId() {
return customerId;
}

public void setCustomerId(String customerId) {
this.customerId = customerId;
}

public String getCashierId() {
return cashierId;
}

public void setCashierId(String cashierId) {
this.cashierId = cashierId;
}

public LocalDateTime getBillDate() {
return billDate;
}

public void setBillDate(LocalDateTime billDate) {
this.billDate = billDate;
}

public BigDecimal getTaxableValue() {
return taxableValue;
}

public void setTaxableValue(BigDecimal taxableValue) {
this.taxableValue = taxableValue;
}

public BigDecimal getCgst() {
return cgst;
}

public void setCgst(BigDecimal cgst) {
this.cgst = cgst;
}

public BigDecimal getSgst() {
return sgst;
}

public void setSgst(BigDecimal sgst) {
this.sgst = sgst;
}

public BigDecimal getDiscount() {
return discount;
}

public void setDiscount(BigDecimal discount) {
this.discount = discount;
}

public BigDecimal getFinalAmount() {
return finalAmount;
}

public void setFinalAmount(BigDecimal finalAmount) {
this.finalAmount = finalAmount;
}

public List<BillItem> getItems() {
return items;
}

public void setItems(List<BillItem> items) {
this.items = items;
}

public void addItem(BillItem item) {
this.items.add(item);
}

public String getCustomerName() {
return customerName;
}

public void setCustomerName(String customerName) {
this.customerName = customerName;
}

public String getCustomerMobile() {
return customerMobile;
}

public void setCustomerMobile(String customerMobile) {
this.customerMobile = customerMobile;
}

public String getCustomerLocation() {
return customerLocation;
}

public void setCustomerLocation(String customerLocation) {
this.customerLocation = customerLocation;
}

public String getPaymentMode() {
return paymentMode;
}

public void setPaymentMode(String paymentMode) {
this.paymentMode = paymentMode;
}

public BigDecimal getCashReceived() {
return cashReceived;
}

public void setCashReceived(BigDecimal cashReceived) {
this.cashReceived = cashReceived;
}

public BigDecimal getCashReturned() {
return cashReturned;
}

public void setCashReturned(BigDecimal cashReturned) {
this.cashReturned = cashReturned;
}

public String getStatus() {
return status;
}

public void setStatus(String status) {
this.status = status;
}
}
