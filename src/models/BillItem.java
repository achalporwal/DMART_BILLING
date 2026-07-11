package models;

import java.math.BigDecimal;

public class BillItem {
    private String billItemId;
    private String billId;
    private String productId;
    private int quantity;
    private BigDecimal mrp;
    private BigDecimal prp;
    private BigDecimal taxableValue;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    
    // Product name loaded at runtime via JOIN
    private String productName;

    public BillItem() {}

    public BillItem(String billItemId, String billId, String productId, int quantity, 
                    BigDecimal mrp, BigDecimal prp, BigDecimal taxableValue, 
                    BigDecimal cgst, BigDecimal sgst, BigDecimal discount, BigDecimal finalAmount) {
        this.billItemId = billItemId;
        this.billId = billId;
        this.productId = productId;
        this.quantity = quantity;
        this.mrp = mrp;
        this.prp = prp;
        this.taxableValue = taxableValue;
        this.cgst = cgst;
        this.sgst = sgst;
        this.discount = discount;
        this.finalAmount = finalAmount;
    }

    public String getBillItemId() {
        return billItemId;
    }

    public void setBillItemId(String billItemId) {
        this.billItemId = billItemId;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public BigDecimal getPrp() {
        return prp;
    }

    public void setPrp(BigDecimal prp) {
        this.prp = prp;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
