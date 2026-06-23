// D-Mart Billing Client Core Application Logic

class DMartBillingApp {
    constructor(userId, userRole) {
        this.userId = userId;
        this.userRole = userRole;
        this.cart = []; // Array of { product, quantity }
        this.sessionBills = [];
        this.currentCustomerId = '';
    }

    // ==========================================
    // BILLING PAGE INITIALIZATION & LOGIC
    // ==========================================
    initBillingPage() {
        // Setup Date inputs and values
        const mobileInput = document.getElementById('custMobile');
        const scanInput = document.getElementById('scanProductId');
        const scanBtn = document.getElementById('scanBtn');
        const clearBtn = document.getElementById('clearBtn');
        const checkoutBtn = document.getElementById('checkoutBtn');
        
        // Modal buttons
        const receiptModal = document.getElementById('receiptModal');
        const closeModalBtn = document.getElementById('closeModalBtn');
        const closeModalBtn2 = document.getElementById('closeModalBtn2');
        const printInvoiceBtn = document.getElementById('printInvoiceBtn');

        // 1. Customer Auto-Populate Mobile Listener
        mobileInput.addEventListener('input', () => {
            const val = mobileInput.value.trim();
            if (val.length === 10 && /^\d+$/.test(val)) {
                this.fetchCustomerByMobile(val);
            }
        });

        // Location Dropdown suggestions for area name-city name-state name matching any character
        const locInput = document.getElementById('custLoc');
        const citiesDatalist = document.getElementById('citiesDatalist');
        if (locInput && citiesDatalist) {
            const locations = [
                "Udaipole-Udaipur-Rajasthan",
                "Hiran Magri-Udaipur-Rajasthan",
                "Vijay Nagar-Indore-Madhya Pradesh",
                "Palasia-Indore-Madhya Pradesh",
                "Andheri West-Mumbai-Maharashtra",
                "Bandra-Mumbai-Maharashtra",
                "Kothrud-Pune-Maharashtra",
                "Hadapsar-Pune-Maharashtra",
                "Salt Lake-Kolkata-West Bengal",
                "Park Street-Kolkata-West Bengal",
                "Connaught Place-New Delhi-Delhi",
                "Karol Bagh-New Delhi-Delhi",
                "Jayanagar-Bengaluru-Karnataka",
                "Indiranagar-Bengaluru-Karnataka",
                "Adyar-Chennai-Tamil Nadu",
                "T. Nagar-Chennai-Tamil Nadu",
                "Banjara Hills-Hyderabad-Telangana",
                "Gachibowli-Hyderabad-Telangana",
                "Ujjain Kothi-Ujjain-Madhya Pradesh",
                "Freeganj-Ujjain-Madhya Pradesh",
                "Arera Colony-Bhopal-Madhya Pradesh",
                "MP Nagar-Bhopal-Madhya Pradesh",
                "C-Scheme-Jaipur-Rajasthan",
                "Malviya Nagar-Jaipur-Rajasthan",
                "Sector 62-Noida-Uttar Pradesh"
            ];
            locInput.addEventListener('input', () => {
                const val = locInput.value.trim().toUpperCase();
                citiesDatalist.innerHTML = '';
                if (val.length > 0) {
                    const matches = locations.filter(loc => loc.toUpperCase().includes(val));
                    matches.forEach(loc => {
                        const opt = document.createElement('option');
                        opt.value = loc;
                        citiesDatalist.appendChild(opt);
                    });
                }
            });
        }

        // Scanner Autocomplete setup
        const dropdown = document.getElementById('scannerDropdown');
        if (scanInput && dropdown) {
            let catalog = [];
            const loadCatalogForAutocomplete = async () => {
                try {
                    const res = await fetch(`/api/product?cashierId=${this.userId}`, {
                        headers: this.getAuthHeaders()
                    });
                    if (res.ok) {
                        catalog = await res.json();
                    }
                } catch (e) {
                    console.error("Failed to load catalog for autocomplete", e);
                }
            };
            
            scanInput.addEventListener('focus', loadCatalogForAutocomplete);
            scanInput.addEventListener('input', () => {
                const val = scanInput.value.trim().toUpperCase();
                dropdown.innerHTML = '';
                if (!val) {
                    dropdown.style.display = 'none';
                    return;
                }
                
                const matches = catalog.filter(p => 
                    p.productId.toUpperCase().startsWith(val) || 
                    p.productName.toUpperCase().includes(val)
                );
                
                if (matches.length > 0) {
                    dropdown.style.display = 'block';
                    matches.forEach(p => {
                        const item = document.createElement('div');
                        item.className = 'autocomplete-item';
                        item.innerText = `${p.productName} - ${p.availableQuantity} - ${p.productId}`;
                        item.addEventListener('mousedown', (e) => {
                            e.preventDefault();
                            scanInput.value = p.productId;
                            dropdown.style.display = 'none';
                            this.scanItem();
                        });
                        dropdown.appendChild(item);
                    });
                } else {
                    dropdown.style.display = 'none';
                }
            });
            
            scanInput.addEventListener('blur', () => {
                setTimeout(() => {
                    dropdown.style.display = 'none';
                }, 200);
            });
        }

        // 2. Scan Item Listeners (Button & Enter Key)
        scanBtn.addEventListener('click', () => this.scanItem());
        scanInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.scanItem();
            }
        });

        // 3. Billing Action Buttons
        clearBtn.addEventListener('click', () => this.clearCart());
        checkoutBtn.addEventListener('click', () => this.generateInvoice());

        // 4. Modal Interactions
        const closeModal = () => receiptModal.classList.remove('active');
        closeModalBtn.addEventListener('click', closeModal);
        closeModalBtn2.addEventListener('click', closeModal);
        printInvoiceBtn.addEventListener('click', () => window.print());

        // 5. Search Invoice Listener
        document.getElementById('searchInvoiceBtn').addEventListener('click', () => {
            const billId = document.getElementById('searchInvoiceId').value.trim();
            if (billId) this.searchAndShowInvoice(billId);
        });

        // 6. Payment Mode and Cash Change Calculator setup
        const paymentMode = document.getElementById('paymentMode');
        const cashCalcBlock = document.getElementById('cashCalculatorBlock');
        const cashReceivedInput = document.getElementById('cashReceived');
        const changeReturnedEl = document.getElementById('changeReturned');
        const updateCustBtn = document.getElementById('updateCustBtn');

        if (paymentMode && cashCalcBlock) {
            paymentMode.addEventListener('change', () => {
                if (paymentMode.value === 'CASH') {
                    cashCalcBlock.style.display = 'block';
                } else {
                    cashCalcBlock.style.display = 'none';
                    if (cashReceivedInput) cashReceivedInput.value = '';
                    if (changeReturnedEl) changeReturnedEl.innerText = '₹0.00';
                }
            });
        }

        this.updateChangeReturned = () => {
            if (cashReceivedInput && changeReturnedEl) {
                const cash = parseFloat(cashReceivedInput.value) || 0;
                const total = parseFloat(document.getElementById('summaryTotal').innerText.replace('₹', '')) || 0;
                const change = Math.max(0, cash - total);
                changeReturnedEl.innerText = `₹${change.toFixed(2)}`;
            }
        };

        if (cashReceivedInput) {
            cashReceivedInput.addEventListener('input', this.updateChangeReturned);
        }

        if (updateCustBtn) {
            updateCustBtn.addEventListener('click', () => this.updateCustomerProfile());
        }

        // 7. Initial Session Loads
        this.loadSessionLogs();
        
        // Restore Draft
        this.checkAndRestoreDraft();
        
        // Add beforeunload save draft sync
        window.addEventListener('beforeunload', () => {
            if (this.cart.length > 0) {
                const draftPayload = this.cart.map(item => ({
                    productId: item.product.productId,
                    quantity: item.quantity
                }));
                fetch('/api/draft', {
                    method: 'POST',
                    headers: {
                        ...this.getAuthHeaders(),
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        cashierId: this.userId,
                        draftJson: JSON.stringify(draftPayload)
                    }),
                    keepalive: true
                });
            }
        });

        // Heartbeat to poll catalog every 7 seconds to sync other cashiers' locks & updates
        setInterval(async () => {
            if (document.activeElement !== scanInput) {
                // If scanner input isn't active, refresh catalog for scanner in background
                try {
                    const res = await fetch(`/api/product?cashierId=${this.userId}`, {
                        headers: this.getAuthHeaders()
                    });
                    if (res.ok) {
                        catalog = await res.json();
                    }
                } catch (e) {}
            }
        }, 7000);
    }

    async fetchCustomerByMobile(mobile) {
        this.showAlert('info', 'Searching customer database...');
        try {
            const response = await fetch(`/api/customer?mobile=${mobile}`, {
                headers: this.getAuthHeaders()
            });
            const data = await response.json();
            if (response.ok && data.status === 'found') {
                this.currentCustomerId = data.customer.customerId;
                document.getElementById('customerId').value = data.customer.customerId;
                document.getElementById('custName').value = data.customer.name;
                document.getElementById('custAge').value = data.customer.age;
                document.getElementById('custLoc').value = data.customer.location;
                // Keep fields editable so cashier can update details
                document.getElementById('custName').readOnly = false;
                document.getElementById('custAge').readOnly = false;
                document.getElementById('custLoc').readOnly = false;
                
                // Show Customer Purchasing History Block
                const histSec = document.getElementById('custHistorySection');
                if (histSec) {
                    histSec.style.display = 'block';
                    document.getElementById('custMonthAmt').innerText = `₹${(data.customer.monthAmount || 0).toFixed(2)}`;
                    document.getElementById('custMonthQty').innerText = `${data.customer.monthQty || 0} item(s)`;
                    document.getElementById('custFyAmt').innerText = `₹${(data.customer.fyAmount || 0).toFixed(2)}`;
                    document.getElementById('custFyQty').innerText = `${data.customer.fyQty || 0} item(s)`;
                }

                const updateCustBtn = document.getElementById('updateCustBtn');
                if (updateCustBtn) updateCustBtn.style.display = 'inline-block';
                this.showAlert('success', 'Customer record resolved! You can update details if needed.');
            } else {
                this.showAlert('info', 'New customer! Please fill details manually.');
                this.makeCustomerFieldsEditable();
            }
        } catch (err) {
            console.error('Failed to lookup customer:', err);
            this.showAlert('danger', 'Unable to check customer records.');
        }
    }

    makeCustomerFieldsEditable() {
        this.currentCustomerId = '';
        const idField = document.getElementById('customerId');
        if (idField) idField.value = 'Auto-generated';
        
        const name = document.getElementById('custName');
        const age = document.getElementById('custAge');
        const loc = document.getElementById('custLoc');
        
        name.value = '';
        age.value = '';
        loc.value = '';
        
        name.readOnly = false;
        age.readOnly = false;
        loc.readOnly = false;

        const histSec = document.getElementById('custHistorySection');
        if (histSec) histSec.style.display = 'none';

        const updateCustBtn = document.getElementById('updateCustBtn');
        if (updateCustBtn) updateCustBtn.style.display = 'none';
    }

    async updateCustomerProfile() {
        if (!this.currentCustomerId) {
            this.showAlert('danger', 'No active customer selected to update.');
            return;
        }

        const name = document.getElementById('custName').value.trim();
        const mobile = document.getElementById('custMobile').value.trim();
        const ageStr = document.getElementById('custAge').value.trim();
        const location = document.getElementById('custLoc').value.trim();

        if (!name || !mobile || mobile.length !== 10) {
            this.showAlert('danger', 'Customer Name and 10-digit mobile number are required.');
            return;
        }

        // Age validation
        const ageVal = parseFloat(ageStr);
        if (isNaN(ageVal) || ageVal <= 0 || ageVal > 150 || ageVal % 1 !== 0) {
            this.showAlert('danger', 'Age must be a positive integer, not a decimal, and not more than 150 years.');
            return;
        }

        const age = parseInt(ageStr) || 0;

        const payload = {
            customerId: this.currentCustomerId,
            name: name,
            mobileNumber: mobile,
            age: age,
            location: location
        };

        this.showAlert('info', 'Updating customer profile...');

        try {
            const response = await fetch('/api/customer/update', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(payload)
            });

            const result = await response.json();
            if (response.ok) {
                this.showAlert('success', 'Customer profile updated successfully!');
            } else {
                this.showAlert('danger', result.message || 'Failed to update customer details.');
            }
        } catch (err) {
            console.error('Failed to update customer details:', err);
            this.showAlert('danger', 'Network fault during customer profile update.');
        }
    }

    async scanItem() {
        const scanInput = document.getElementById('scanProductId');
        const productId = scanInput.value.trim().toUpperCase();
        if (!productId) return;

        scanInput.value = ''; // Reset scanner input immediately for speed
        this.showAlert('info', `Scanning Product ID: ${productId}...`);

        try {
            const response = await fetch(`/api/product?id=${productId}&cashierId=${this.userId}`, {
                headers: this.getAuthHeaders()
            });
            if (!response.ok) {
                this.showAlert('danger', `Product ID '${productId}' not found in catalog.`);
                return;
            }

            const product = await response.json();
            
            // Check if already in cart
            const cartItem = this.cart.find(item => item.product.productId === product.productId);
            if (cartItem) {
                if (cartItem.quantity >= product.availableQuantity) {
                    this.showAlert('danger', `Cannot add more. Inventory limit reached! Stock: ${product.availableQuantity}`);
                    return;
                }
                cartItem.quantity++;
            } else {
                if (product.availableQuantity < 1) {
                    this.showAlert('danger', `Product '${product.productName}' is currently out of stock.`);
                    return;
                }
                this.cart.push({ product, quantity: 1 });
            }

            this.showAlert('success', `Added: ${product.productName}`);
            this.renderCart();
        } catch (err) {
            console.error('Failed to scan product:', err);
            this.showAlert('danger', 'Connection failure during item scanning.');
        }
    }

    updateCartQuantity(productId, newQty) {
        const cartItem = this.cart.find(item => item.product.productId === productId);
        if (!cartItem) return;

        newQty = parseInt(newQty);
        if (isNaN(newQty) || newQty <= 0) {
            this.removeFromCart(productId);
            return;
        }

        if (newQty > cartItem.product.availableQuantity) {
            this.showAlert('danger', `Only ${cartItem.product.availableQuantity} items available in inventory.`);
            document.getElementById(`qty-${productId}`).value = cartItem.product.availableQuantity;
            cartItem.quantity = cartItem.product.availableQuantity;
        } else {
            cartItem.quantity = newQty;
        }
        this.renderCart();
    }

    removeFromCart(productId) {
        this.cart = this.cart.filter(item => item.product.productId !== productId);
        this.renderCart();
    }

    clearCart() {
        this.cart = [];
        this.renderCart();
        this.makeCustomerFieldsEditable();
        document.getElementById('custMobile').value = '';
        document.getElementById('custMobile').readOnly = false;
        
        const histSec = document.getElementById('custHistorySection');
        if (histSec) histSec.style.display = 'none';

        // Clear Draft on Server
        this.saveDraftToServer();

        this.showAlert('info', 'Cart cleared successfully.');
    }

    renderCart() {
        const tbody = document.getElementById('cartItems');
        if (this.cart.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="10" style="text-align: center; color: var(--text-secondary); padding: 3rem;">
                        No items scanned. Use the scanner above to add items to cart.
                    </td>
                </tr>`;
            this.updateTotalsUI(0, 0, 0, 0, 0, 0);
            return;
        }

        tbody.innerHTML = '';
        
        let subMrp = 0;
        let subTaxable = 0;
        let subCgst = 0;
        let subSgst = 0;
        let subDiscount = 0;
        let subTotal = 0;

        this.cart.forEach(item => {
            const p = item.product;
            const qty = item.quantity;
            
            // GST-inclusive calculations (Offer price PRP is inclusive of GST)
            const itemMrp = p.mrp * qty;
            const itemTotal = p.prp * qty; // final customer price is exactly PRP * Qty
            
            const gstPercentage = p.gstPercentage || 0;
            // taxableValue = itemTotal / (1 + (gstPercentage / 100))
            const itemTaxable = itemTotal / (1 + (gstPercentage / 100));
            const totalGst = itemTotal - itemTaxable;
            const itemCgst = totalGst / 2;
            const itemSgst = totalGst / 2;
            
            // Discount/Savings is the difference between MRP value and actual price paid
            const itemDiscount = itemMrp - itemTotal;

            subMrp += itemMrp;
            subTaxable += itemTaxable;
            subCgst += itemCgst;
            subSgst += itemSgst;
            subDiscount += itemDiscount;
            subTotal += itemTotal;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${p.productId}</strong></td>
                <td>${p.productName}</td>
                <td>₹${p.mrp.toFixed(2)}</td>
                <td>₹${p.prp.toFixed(2)}</td>
                <td>
                    <input type="number" id="qty-${p.productId}" value="${qty}" min="1" max="${p.availableQuantity}" 
                           class="form-control" style="width: 70px; padding: 0.25rem 0.5rem; text-align: center;"
                           onchange="app.updateCartQuantity('${p.productId}', this.value)">
                </td>
                <td><span class="badge badge-indigo">${p.gstPercentage}%</span></td>
                <td>₹${totalGst.toFixed(2)} <span style="font-size:0.75rem;color:var(--text-secondary);">(Incl.)</span></td>
                <td style="color: var(--accent-purple);">₹${itemDiscount.toFixed(2)}</td>
                <td><strong>₹${itemTotal.toFixed(2)}</strong></td>
                <td>
                    <button class="btn btn-danger" style="width: auto; padding: 0.25rem 0.5rem;" 
                            onclick="app.removeFromCart('${p.productId}')">&times;</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        this.updateTotalsUI(subMrp, subTaxable, subCgst, subSgst, subDiscount, subTotal);
        
        // Sync Cart Locks with Server
        this.syncCartLock();
        // Save current cart draft to server
        this.saveDraftToServer();
    }

    updateTotalsUI(mrp, taxable, cgst, sgst, discount, total) {
        document.getElementById('summaryMrp').innerText = `₹${mrp.toFixed(2)}`;
        document.getElementById('summaryTaxable').innerText = `₹${taxable.toFixed(2)}`;
        document.getElementById('summaryCgst').innerText = `₹${cgst.toFixed(2)}`;
        document.getElementById('summarySgst').innerText = `₹${sgst.toFixed(2)}`;
        document.getElementById('summaryDiscount').innerText = `₹${discount.toFixed(2)}`;
        document.getElementById('summaryTotal').innerText = `₹${total.toFixed(2)}`;
        if (this.updateChangeReturned) {
            this.updateChangeReturned();
        }
    }

    async generateInvoice() {
        if (this.cart.length === 0) {
            this.showAlert('danger', 'Pay failed: Your cart is empty.');
            return;
        }

        const mobile = document.getElementById('custMobile').value.trim();
        const name = document.getElementById('custName').value.trim();
        const ageStr = document.getElementById('custAge').value.trim();
        const location = document.getElementById('custLoc').value.trim();

        if (!mobile || !name || mobile.length !== 10) {
            this.showAlert('danger', 'Customer Mobile (10-digits) and Name are required.');
            return;
        }

        // Age validation
        const ageVal = parseFloat(ageStr);
        if (isNaN(ageVal) || ageVal <= 0 || ageVal > 150 || ageVal % 1 !== 0) {
            this.showAlert('danger', 'Age must be a positive integer, not a decimal, and not more than 150 years.');
            return;
        }

        const age = parseInt(ageStr) || 0;
        const items = this.cart.map(item => ({
            productId: item.product.productId,
            quantity: item.quantity
        }));

        const paymentMode = document.getElementById('paymentMode') ? document.getElementById('paymentMode').value : 'CASH';
        let cashReceived = 0;
        let cashReturned = 0;

        if (paymentMode === 'CASH') {
            const cashInput = document.getElementById('cashReceived');
            cashReceived = parseFloat(cashInput ? cashInput.value : 0) || 0;
            const netAmount = parseFloat(document.getElementById('summaryTotal').innerText.replace('₹', '')) || 0;
            if (cashReceived < netAmount) {
                this.showAlert('danger', `Insufficient cash received. Needed at least ₹${netAmount.toFixed(2)}.`);
                return;
            }
            cashReturned = cashReceived - netAmount;
        }

        const payload = {
            customerId: this.currentCustomerId || '',
            customerMobile: mobile,
            customerName: name,
            customerAge: age,
            customerLocation: location,
            paymentMode: paymentMode,
            cashReceived: cashReceived,
            cashReturned: cashReturned,
            items: items
        };

        this.showAlert('info', 'Processing transaction and building invoice...');

        try {
            const response = await fetch('/api/bill', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(payload)
            });

            const result = await response.json();
            if (response.ok) {
                this.showAlert('success', `Invoice generated successfully! ID: ${result.billId}`);
                this.showReceiptModal(result, name, mobile, location);
                this.clearCart();
                this.loadSessionLogs();
            } else {
                this.showAlert('danger', result.message || 'Failed to complete checkout.');
            }
        } catch (err) {
            console.error('Invoice creation failed:', err);
            this.showAlert('danger', 'Network fault during checkout process.');
        }
    }

    showReceiptModal(bill, custName, custMobile, custLoc) {
        document.getElementById('recBillId').innerText = bill.billId;
        document.getElementById('recCashier').innerText = bill.cashierId;
        document.getElementById('recDate').innerText = new Date(bill.billDate).toLocaleString();
        
        const payModeEl = document.getElementById('recPaymentMode');
        if (payModeEl) {
            payModeEl.innerText = bill.paymentMode || 'CASH';
        }
        
        document.getElementById('recCustName').innerText = custName || bill.customerName || 'Walk-in Customer';
        document.getElementById('recCustMobile').innerText = custMobile || bill.customerMobile || '';
        document.getElementById('recCustLoc').innerText = custLoc || bill.customerLocation || 'NA';

        const tbody = document.getElementById('recItems');
        tbody.innerHTML = '';

        bill.items.forEach(item => {
            const tr = document.createElement('tr');
            const pNameDisplay = item.productName ? `${item.productName} (${item.productId})` : `Product ID: ${item.productId}`;
            
            let returnButtonHtml = '';
            if (item.quantity > 0) {
                returnButtonHtml = `
                    <td style="padding: 0.5rem 0; text-align: center;" class="no-print">
                        <div style="display: flex; gap: 0.25rem; align-items: center; justify-content: center;">
                            <input type="number" id="retQty-${item.productId}" min="1" max="${item.quantity}" value="1" 
                                   class="form-control" style="width: 50px; padding: 0.1rem 0.25rem; text-align: center; margin: 0; font-size: 0.75rem; height: auto; background: rgba(255,255,255,0.05); color: #fff; border: 1px solid var(--border-color);">
                            <button class="btn btn-danger" style="width: auto; padding: 0.25rem 0.5rem; font-size: 0.75rem; border-radius: 4px; line-height: 1.2;" 
                                    onclick="app.returnItem('${bill.billId}', '${item.productId}')">Return</button>
                        </div>
                    </td>
                `;
            } else {
                returnButtonHtml = `
                    <td style="padding: 0.5rem 0; text-align: center; color: var(--text-secondary);" class="no-print">
                        Returned
                    </td>
                `;
            }

            tr.innerHTML = `
                <td style="padding: 0.5rem 0;">${pNameDisplay}</td>
                <td style="padding: 0.5rem 0; text-align: right;">₹${item.mrp.toFixed(2)}</td>
                <td style="padding: 0.5rem 0; text-align: right;">₹${item.prp.toFixed(2)}</td>
                <td style="padding: 0.5rem 0; text-align: center;">${item.quantity}</td>
                <td style="padding: 0.5rem 0; text-align: right;">₹${item.finalAmount.toFixed(2)}</td>
                ${returnButtonHtml}
            `;
            tbody.appendChild(tr);
        });

        document.getElementById('recTaxable').innerText = `₹${bill.taxableValue.toFixed(2)}`;
        document.getElementById('recCgst').innerText = `₹${bill.cgst.toFixed(2)}`;
        document.getElementById('recSgst').innerText = `₹${bill.sgst.toFixed(2)}`;
        document.getElementById('recSavings').innerText = `₹${bill.discount.toFixed(2)}`;
        document.getElementById('recTotal').innerText = `₹${bill.finalAmount.toFixed(2)}`;

        const recCashDetails = document.getElementById('recCashDetails');
        if (recCashDetails) {
            if ((bill.paymentMode || 'CASH') === 'CASH') {
                recCashDetails.style.display = 'block';
                document.getElementById('recCashReceived').innerText = `₹${(bill.cashReceived || 0).toFixed(2)}`;
                document.getElementById('recChangeReturned').innerText = `₹${(bill.cashReturned || 0).toFixed(2)}`;
            } else {
                recCashDetails.style.display = 'none';
            }
        }

        const recStatus = document.getElementById('recStatus');
        if (recStatus) {
            recStatus.innerText = bill.status || 'COMPLETED';
            if (bill.status === 'REVISED') {
                recStatus.style.color = 'rgb(165, 180, 252)'; // Soft indigo/purple
            } else {
                recStatus.style.color = 'rgb(52, 211, 153)'; // Soft emerald
            }
        }

        document.getElementById('receiptModal').classList.add('active');
    }

    async loadSessionLogs() {
        const tbody = document.getElementById('sessionLogsBody');
        try {
            const response = await fetch(`/api/bill?cashierId=${this.userId}`, {
                headers: this.getAuthHeaders()
            });
            if (!response.ok) return;

            const bills = await response.json();
            this.sessionBills = bills;

            // Calculate session summary
            let cashCount = 0;
            let cashReceived = 0;
            let cashReturned = 0;
            let netCash = 0;
            let onlineCount = 0;
            let onlineVolume = 0;

            bills.forEach(bill => {
                const mode = (bill.paymentMode || 'CASH').toUpperCase();
                if (mode === 'ONLINE') {
                    onlineCount++;
                    onlineVolume += bill.finalAmount;
                } else {
                    cashCount++;
                    cashReceived += (bill.cashReceived || 0);
                    cashReturned += (bill.cashReturned || 0);
                    netCash += bill.finalAmount;
                }
            });

            const cashInvEl = document.getElementById('sessionCashInvoices');
            const cashReceivedEl = document.getElementById('sessionCashReceived');
            const cashReturnedEl = document.getElementById('sessionCashReturned');
            const netCashEl = document.getElementById('sessionNetCash');
            const onlineInvEl = document.getElementById('sessionOnlineInvoices');
            const onlineVolEl = document.getElementById('sessionOnlineVolume');

            if (cashInvEl) cashInvEl.innerText = cashCount;
            if (cashReceivedEl) cashReceivedEl.innerText = `₹${cashReceived.toFixed(2)}`;
            if (cashReturnedEl) cashReturnedEl.innerText = `₹${cashReturned.toFixed(2)}`;
            if (netCashEl) netCashEl.innerText = `₹${netCash.toFixed(2)}`;
            if (onlineInvEl) onlineInvEl.innerText = onlineCount;
            if (onlineVolEl) onlineVolEl.innerText = `₹${onlineVolume.toFixed(2)}`;

            if (bills.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="4" style="text-align: center; color: var(--text-secondary); padding: 1.5rem;">
                            No sales in this session yet.
                        </td>
                    </tr>`;
                return;
            }

            tbody.innerHTML = '';
            bills.forEach(bill => {
                const tr = document.createElement('tr');
                const timeStr = new Date(bill.billDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                tr.innerHTML = `
                    <td><strong>${bill.billId}</strong></td>
                    <td>₹${bill.finalAmount.toFixed(2)}</td>
                    <td>${timeStr}</td>
                    <td>
                        <button class="btn btn-secondary" style="width: auto; padding: 0.1rem 0.4rem; font-size: 0.75rem;"
                                onclick="app.showHistoricalReceipt('${bill.billId}')">View</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } catch (err) {
            console.error('Failed to load logs:', err);
        }
    }

    showHistoricalReceipt(billId) {
        const bill = this.sessionBills.find(b => b.billId === billId);
        if (!bill) {
            // If not found in current session bills (e.g. searched on admin side), query API
            this.searchAndShowInvoice(billId);
            return;
        }
        this.showReceiptModal(bill, bill.customerName, bill.customerMobile, bill.customerLocation);
    }

    async returnItem(billId, productId) {
        const qtyField = document.getElementById(`retQty-${productId}`);
        if (!qtyField) return;
        const quantity = parseInt(qtyField.value);

        if (isNaN(quantity) || quantity <= 0) {
            this.showAlert('danger', 'Please enter a valid return quantity.');
            return;
        }

        if (!confirm(`Are you sure you want to return ${quantity} unit(s) of product '${productId}'?`)) {
            return;
        }

        this.showAlert('info', 'Processing material return...');

        try {
            const response = await fetch('/api/bill/return', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify({ billId, productId, quantity })
            });

            const result = await response.json();
            if (response.ok) {
                this.showAlert('success', 'Return completed successfully! Stock updated.');
                // Re-render modal with the updated bill data
                this.showReceiptModal(result, result.customerName, result.customerMobile, result.customerLocation);
                
                // Refresh local lists
                if (document.getElementById('sessionLogsBody')) {
                    this.loadSessionLogs();
                }
                
                // If on Admin page, refresh reports and tabs
                if (document.getElementById('runReportBtn')) {
                    this.loadReports();
                }
            } else {
                this.showAlert('danger', result.message || 'Failed to process return.');
            }
        } catch (err) {
            console.error('Failed to process return:', err);
            this.showAlert('danger', 'Connection error while processing return.');
        }
    }


    // ==========================================
    // ADMIN DASHBOARD PAGE INITIALIZATION & LOGIC
    // ==========================================
    initAdminPage() {
        const reportType = document.getElementById('reportType');
        const runReportBtn = document.getElementById('runReportBtn');
        const resetProductFormBtn = document.getElementById('resetProductFormBtn');
        const productForm = document.getElementById('productForm');

        // Setup report defaults to today
        document.getElementById('reportDate').valueAsDate = new Date();

        // Modal buttons for Admin Page
        const receiptModal = document.getElementById('receiptModal');
        const closeModalBtn = document.getElementById('closeModalBtn');
        const closeModalBtn2 = document.getElementById('closeModalBtn2');
        const printInvoiceBtn = document.getElementById('printInvoiceBtn');

        if (receiptModal && closeModalBtn && closeModalBtn2 && printInvoiceBtn) {
            const closeModal = () => receiptModal.classList.remove('active');
            closeModalBtn.addEventListener('click', closeModal);
            closeModalBtn2.addEventListener('click', closeModal);
            printInvoiceBtn.addEventListener('click', () => window.print());
        }

        // 1. Report Filters Toggle
        reportType.addEventListener('change', () => {
            const val = reportType.value;
            if (val === 'monthly') {
                document.getElementById('dateFilterGroup').style.display = 'none';
                document.getElementById('monthFilterGroup').style.display = 'block';
                document.getElementById('yearFilterGroup').style.display = 'block';
            } else {
                document.getElementById('dateFilterGroup').style.display = 'block';
                document.getElementById('monthFilterGroup').style.display = 'none';
                document.getElementById('yearFilterGroup').style.display = 'none';
            }
        });

        // 2. Load Reports Click
        runReportBtn.addEventListener('click', () => this.loadReports());

        // 3. Product Catalog Form Reset
        resetProductFormBtn.addEventListener('click', () => this.clearProductForm());

        // 4. Product Save Form Submit
        productForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveProduct();
        });

        // 5. Search Invoice Listener
        document.getElementById('searchInvoiceBtn').addEventListener('click', () => {
            const billId = document.getElementById('searchInvoiceId').value.trim();
            if (billId) this.searchAndShowInvoice(billId);
        });

        // 6. CSV Export is now tab-aware — wired via exportActiveTab()

        // 7. Cashier management forms
        const cashierForm = document.getElementById('cashierForm');
        if (cashierForm) {
            cashierForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.saveCashier();
            });
        }
        
        const resetPasswordForm = document.getElementById('resetPasswordForm');
        if (resetPasswordForm) {
            resetPasswordForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.resetCashierPassword();
            });
        }

        // Initial Load — default date is today, so pass it explicitly
        const todayStr = new Date().toISOString().slice(0, 10); // YYYY-MM-DD
        this.currentDataTab = 'sales';
        this.loadReports(); // loadReports() also calls loadAllSalesTab() internally
        this.loadCustomersTab();
        this.loadItemsSoldTab();
        this.loadAnalytics();
    }

    async loadReports() {
        const type = document.getElementById('reportType').value;
        const dateVal = document.getElementById('reportDate').value;
        const monthVal = document.getElementById('reportMonth').value;
        const yearVal = document.getElementById('reportYear').value;

        let query = `/api/admin/reports?type=${type}`;
        if (type === 'daily') {
            query += `&date=${dateVal}`;
        } else {
            query += `&month=${monthVal}&year=${yearVal}`;
        }

        this.showAlert('info', 'Loading group-by database metrics...');

        try {
            const response = await fetch(query, {
                headers: this.getAuthHeaders()
            });

            if (response.status === 403) {
                this.showAlert('danger', 'Unauthorized. ONLY SUPER_ADMIN role can query reports.');
                return;
            }

            const reportItems = await response.json();
            const tbody = document.getElementById('reportTableBody');

            if (reportItems.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" style="text-align: center; color: var(--text-secondary); padding: 3rem;">
                            No cashier performance reports resolved for this interval.
                        </td>
                    </tr>`;
                document.getElementById('statBillsCount').innerText = '0';
                document.getElementById('statRevenue').innerText = '₹0.00';
                this.showAlert('info', 'Reports retrieved. Zero sales logs found.');
                return;
            }

            tbody.innerHTML = '';
            let grandTotalRevenue = 0;
            let grandTotalBills = 0;

            reportItems.forEach(item => {
                grandTotalRevenue += item.totalRevenue;
                grandTotalBills += item.totalBills;

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${item.cashierId}</strong></td>
                    <td>${item.cashierName}</td>
                    <td style="text-align: center;">${item.totalBills}</td>
                    <td style="text-align: right; font-weight: 600; color: var(--accent-emerald);">₹${item.totalRevenue.toFixed(2)}</td>
                    <td><span class="badge ${item.totalBills > 0 ? 'badge-success' : 'badge-indigo'}">${item.totalBills > 0 ? 'Active' : 'Idle'}</span></td>
                `;
                tbody.appendChild(tr);
            });

            document.getElementById('statBillsCount').innerText = grandTotalBills;
            document.getElementById('statRevenue').innerText = `₹${grandTotalRevenue.toFixed(2)}`;
            document.getElementById('salesCount').innerText = `${reportItems.length} cashier(s)`;
            this.showAlert('success', 'Group-by metrics loaded from SQL schema!');

            // ── Also refresh All Invoices tab with the same date filter ──
            const type2 = document.getElementById('reportType').value;
            if (type2 === 'daily') {
                const dv = document.getElementById('reportDate').value;
                await this.loadAllSalesTab({ date: dv });
            } else {
                const mv = document.getElementById('reportMonth').value;
                const yv = document.getElementById('reportYear').value;
                await this.loadAllSalesTab({ month: mv, year: yv });
            }
            this.loadAnalytics();
        } catch (err) {
            console.error('Failed to run reports:', err);
            this.showAlert('danger', 'Unable to retrieve reports.');
        }
    }

    async loadCatalog() {
        const tbody = document.getElementById('catalogTableBody');
        try {
            const response = await fetch('/api/product', {
                headers: this.getAuthHeaders()
            });
            const products = await response.json();

            if (products.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="9" style="text-align: center; color: var(--text-secondary); padding: 3rem;">
                            Inventory is empty. Use the form to seed stock.
                        </td>
                    </tr>`;
                return;
            }

            tbody.innerHTML = '';
            products.forEach(p => {
                const tr = document.createElement('tr');
                const threshold = p.alertThreshold || 10;
                tr.innerHTML = `
                    <td><strong>${p.productId}</strong></td>
                    <td>${p.productName}</td>
                    <td>₹${p.mrp.toFixed(2)}</td>
                    <td>₹${p.prp.toFixed(2)}</td>
                    <td><span class="badge badge-indigo">${p.gstPercentage}%</span></td>
                    <td>
                        <span class="badge ${p.availableQuantity < threshold ? 'badge-danger' : 'badge-success'}">
                            ${p.availableQuantity} units
                        </span>
                    </td>
                    <td>
                        <span class="badge badge-indigo">
                            ${p.heldQuantity || 0} units
                        </span>
                    </td>
                    <td>
                        <input type="number" value="${threshold}" min="1" 
                               class="form-control" style="width: 70px; padding: 0.25rem 0.5rem; text-align: center; margin: 0 auto; background: rgba(255,255,255,0.05); color: #fff; border: 1px solid var(--border-color);"
                               onchange="app.updateProductAlertThreshold('${p.productId}', this.value)">
                    </td>
                    <td>
                        <button class="btn btn-secondary" style="width: auto; padding: 0.25rem 0.5rem;" 
                                onclick="app.populateEditForm('${p.productId}', '${p.productName.replace(/'/g, "\\'")}', ${p.mrp}, ${p.prp}, ${p.gstPercentage}, ${p.availableQuantity}, ${threshold}, ${p.heldQuantity || 0})">
                            Edit
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } catch (err) {
            console.error('Failed to retrieve inventory:', err);
            this.showAlert('danger', 'Could not query catalog list.');
        }
    }

    populateEditForm(id, name, mrp, prp, gst, qty, alertVal, heldVal) {
        document.getElementById('formProductTitle').innerText = "Update Existing Product";
        
        document.getElementById('prodId').value = id;
        document.getElementById('prodId').readOnly = true; // Block code editing
        
        document.getElementById('prodName').value = name;
        document.getElementById('prodMrp').value = mrp;
        document.getElementById('prodPrp').value = prp;
        document.getElementById('prodGst').value = gst;
        document.getElementById('prodQty').value = qty;
        document.getElementById('prodAlert').value = alertVal || 10;
        document.getElementById('prodHeld').value = heldVal || 0;
        
        this.showAlert('info', `Selected ${name} for update.`);
    }

    clearProductForm() {
        document.getElementById('formProductTitle').innerText = "Add New Product";
        document.getElementById('prodId').readOnly = false;
        document.getElementById('productForm').reset();
        document.getElementById('prodAlert').value = 10;
        document.getElementById('prodHeld').value = 0;
    }

    async saveProduct() {
        const productId = document.getElementById('prodId').value.trim();
        const productName = document.getElementById('prodName').value.trim();
        const mrp = document.getElementById('prodMrp').value;
        const prp = document.getElementById('prodPrp').value;
        const gstPercentage = document.getElementById('prodGst').value;
        const availableQuantity = document.getElementById('prodQty').value;
        const alertThreshold = document.getElementById('prodAlert').value;
        const heldQuantity = document.getElementById('prodHeld').value;

        const payload = {
            productId,
            productName,
            mrp: parseFloat(mrp).toFixed(2),
            prp: parseFloat(prp).toFixed(2),
            gstPercentage: parseFloat(gstPercentage).toFixed(2),
            availableQuantity: parseInt(availableQuantity),
            alertThreshold: parseInt(alertThreshold) || 10,
            heldQuantity: parseInt(heldQuantity) || 0
        };

        this.showAlert('info', 'Saving product record...');

        try {
            const response = await fetch('/api/product', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(payload)
            });

            const result = await response.json();
            if (response.ok) {
                this.showAlert('success', 'Product record committed to DB!');
                this.clearProductForm();
                this.loadCatalog();
            } else {
                this.showAlert('danger', result.message || 'Failed to save product.');
            }
        } catch (err) {
            console.error('Save product failed:', err);
            this.showAlert('danger', 'Network fault while updating catalog.');
        }
    }

    async updateProductAlertThreshold(productId, newThreshold) {
        newThreshold = parseInt(newThreshold);
        if (isNaN(newThreshold) || newThreshold < 1) {
            this.showAlert('danger', 'Please enter a valid alert threshold.');
            return;
        }
        
        try {
            // First get the product details
            const response = await fetch(`/api/product?id=${productId}`, {
                headers: this.getAuthHeaders()
            });
            if (!response.ok) {
                this.showAlert('danger', 'Failed to retrieve product details.');
                return;
            }
            const product = await response.json();
            
            // Save updated product
            const saveResponse = await fetch('/api/product', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify({
                    productId: product.productId,
                    productName: product.productName,
                    mrp: product.mrp.toFixed(2),
                    prp: product.prp.toFixed(2),
                    gstPercentage: product.gstPercentage.toFixed(2),
                    availableQuantity: product.availableQuantity,
                    alertThreshold: newThreshold
                })
            });
            
            if (saveResponse.ok) {
                this.showAlert('success', `Alert threshold updated to ${newThreshold} for product ${product.productName}`);
                this.loadCatalog();
                this.loadAnalytics();
            } else {
                const resJson = await saveResponse.json();
                this.showAlert('danger', resJson.message || 'Failed to update alert threshold.');
            }
        } catch (err) {
            console.error('Failed to update alert threshold:', err);
            this.showAlert('danger', 'Error connecting to server.');
        }
    }

    // ==========================================
    // UTILITIES
    // ==========================================
    getAuthHeaders() {
        return {
            'Content-Type': 'application/json',
            'X-User-Role': sessionStorage.getItem('userRole') || this.userRole || '',
            'X-User-Id': sessionStorage.getItem('userId') || this.userId || ''
        };
    }

    showAlert(type, msg) {
        const el = document.getElementById('statusAlert');
        if (!el) return;
        el.className = `alert alert-${type}`;
        el.innerText = msg;
        el.style.display = 'block';

        // Auto close after 4 seconds
        if (this.alertTimeout) clearTimeout(this.alertTimeout);
        this.alertTimeout = setTimeout(() => {
            el.style.display = 'none';
        }, 4000);
    }
    async searchAndShowInvoice(billId) {
        this.showAlert('info', `Searching invoice: ${billId}...`);
        try {
            const response = await fetch(`/api/bill?id=${billId}`, {
                headers: this.getAuthHeaders()
            });
            if (!response.ok) {
                this.showAlert('danger', `Invoice '${billId}' not found.`);
                return;
            }
            const bill = await response.json();
            this.showReceiptModal(bill, bill.customerName, bill.customerMobile, bill.customerLocation);
            this.showAlert('success', `Invoice resolved!`);
        } catch (err) {
            console.error('Invoice search failed:', err);
            this.showAlert('danger', 'Failed to connect to server for invoice search.');
        }
    }

    async exportCsv(url, defaultFilename) {
        this.showAlert('info', 'Preparing CSV download...');
        try {
            const response = await fetch(url, {
                headers: this.getAuthHeaders()
            });
            if (!response.ok) {
                this.showAlert('danger', 'Failed to export CSV. Check role permissions.');
                return;
            }
            const blob = await response.blob();
            const downloadUrl = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = downloadUrl;
            a.download = defaultFilename;
            document.body.appendChild(a);
            a.click();
            a.remove();
            this.showAlert('success', 'CSV downloaded successfully.');
        } catch (err) {
            console.error('CSV export failed:', err);
            this.showAlert('danger', 'Connection failure during CSV export.');
        }
    }

    async loadCashiers() {
        const tbody = document.getElementById('cashierTableBody');
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">Loading...</td></tr>';
        try {
            const response = await fetch('/api/admin/users', {
                headers: this.getAuthHeaders()
            });
            const cashiers = await response.json();
            if (cashiers.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="4" style="text-align: center; color: var(--text-secondary); padding: 3rem;">
                            No cashier accounts registered yet.
                        </td>
                    </tr>`;
                return;
            }
            tbody.innerHTML = '';
            cashiers.forEach(u => {
                const tr = document.createElement('tr');
                const statusBadge = u.isActive 
                    ? '<span class="badge badge-success">Active</span>' 
                    : '<span class="badge" style="background: rgba(239, 68, 68, 0.1); color: rgb(248, 113, 113); border: 1px solid rgba(239, 68, 68, 0.2);">Inactive</span>';
                const toggleButtonText = u.isActive ? 'Deactivate' : 'Activate';
                const toggleButtonClass = u.isActive ? 'btn-danger' : 'btn-success';

                tr.innerHTML = `
                    <td><strong>${u.userId}</strong></td>
                    <td>${u.name}</td>
                    <td>
                        <span class="badge badge-indigo" style="margin-right: 0.5rem;">${u.role}</span>
                        ${statusBadge}
                    </td>
                    <td>
                        <button class="btn btn-secondary" style="width: auto; padding: 0.25rem 0.5rem; margin-right: 0.5rem;" 
                                onclick="app.selectCashierForReset('${u.userId}')">
                            Reset Pass
                        </button>
                        <button class="btn ${toggleButtonClass}" style="width: auto; padding: 0.25rem 0.5rem;" 
                                onclick="app.deleteCashier('${u.userId}')">
                            ${toggleButtonText}
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } catch (err) {
            console.error('Failed to load cashiers:', err);
            this.showAlert('danger', 'Could not retrieve cashiers list.');
        }
    }

    selectCashierForReset(userId) {
        document.getElementById('resetCashierId').value = userId;
        document.getElementById('resetNewPassword').focus();
    }

    async saveCashier() {
        const userId = document.getElementById('cashierIdInput').value.trim();
        const name = document.getElementById('cashierNameInput').value.trim();
        const password = document.getElementById('cashierPassInput').value;

        const payload = { userId, name, password };
        this.showAlert('info', 'Saving cashier account...');

        try {
            const response = await fetch('/api/admin/users', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(payload)
            });
            const result = await response.json();
            if (response.ok) {
                this.showAlert('success', 'Cashier account saved successfully!');
                document.getElementById('cashierForm').reset();
                this.loadCashiers();
            } else {
                this.showAlert('danger', result.message || 'Failed to save cashier.');
            }
        } catch (err) {
            console.error('Save cashier failed:', err);
            this.showAlert('danger', 'Network fault during cashier registration.');
        }
    }

    async deleteCashier(userId) {
        if (!confirm(`Are you sure you want to toggle access status for cashier '${userId}'?`)) return;
        this.showAlert('info', 'Toggling cashier access status...');
        try {
            const response = await fetch(`/api/admin/users?id=${userId}`, {
                method: 'DELETE',
                headers: this.getAuthHeaders()
            });
            const result = await response.json();
            if (response.ok) {
                this.showAlert('success', 'Cashier access status toggled successfully!');
                this.loadCashiers();
            } else {
                this.showAlert('danger', result.message || 'Failed to toggle cashier access status.');
            }
        } catch (err) {
            console.error('Toggle cashier access failed:', err);
            this.showAlert('danger', 'Connection error during cashier access toggle.');
        }
    }

    async resetCashierPassword() {
        const userId = document.getElementById('resetCashierId').value.trim();
        const newPassword = document.getElementById('resetNewPassword').value;

        const payload = { userId, newPassword };
        this.showAlert('info', 'Resetting password...');

        try {
            const response = await fetch('/api/admin/users/reset-password', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(payload)
            });
            const result = await response.json();
            if (response.ok) {
                this.showAlert('success', 'Password reset committed successfully!');
                document.getElementById('resetPasswordForm').reset();
            } else {
                this.showAlert('danger', result.message || 'Failed to reset password.');
            }
        } catch (err) {
            console.error('Reset password failed:', err);
            this.showAlert('danger', 'Connection error during password reset.');
        }
    }

    // ==========================================
    // DATA TABS — INLINE SUMMARY TABLES
    // ==========================================

    switchDataTab(tabName) {
        this.currentDataTab = tabName;

        // All tab content panels
        ['tabSales', 'tabCustomers', 'tabItems', 'tabAllSales'].forEach(id => {
            const panel = document.getElementById(id);
            if (panel) panel.style.display = 'none';
        });

        // All tab buttons — reset style
        ['tabBtnSales', 'tabBtnCustomers', 'tabBtnItems', 'tabBtnAllSales'].forEach(btnId => {
            const btn = document.getElementById(btnId);
            if (btn) {
                btn.style.color = 'var(--text-secondary)';
                btn.style.fontWeight = '600';
                btn.style.borderBottom = '3px solid transparent';
            }
        });

        // Activate selected
        const tabMap = {
            sales:     { panel: 'tabSales',    btn: 'tabBtnSales' },
            customers: { panel: 'tabCustomers', btn: 'tabBtnCustomers' },
            items:     { panel: 'tabItems',     btn: 'tabBtnItems' },
            allsales:  { panel: 'tabAllSales',  btn: 'tabBtnAllSales' }
        };

        const selected = tabMap[tabName];
        if (selected) {
            document.getElementById(selected.panel).style.display = 'block';
            const activeBtn = document.getElementById(selected.btn);
            if (activeBtn) {
                activeBtn.style.color = 'var(--accent-indigo)';
                activeBtn.style.fontWeight = '700';
                activeBtn.style.borderBottom = '3px solid var(--accent-indigo)';
            }
        }

        // Lazy load tab contents on switch
        if (tabName === 'customers') {
            this.loadCustomersTab();
        } else if (tabName === 'items') {
            this.loadItemsSoldTab();
        } else if (tabName === 'allsales') {
            const type = document.getElementById('reportType').value;
            if (type === 'daily') {
                const dv = document.getElementById('reportDate').value;
                this.loadAllSalesTab({ date: dv });
            } else {
                const mv = document.getElementById('reportMonth').value;
                const yv = document.getElementById('reportYear').value;
                this.loadAllSalesTab({ month: mv, year: yv });
            }
        }
    }

    // ── Customers Tab ────────────────────────────────────────────────────────

    async loadCustomersTab() {
        const tbody = document.getElementById('customersTableBody');
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-secondary);padding:2rem;">Loading...</td></tr>`;
        try {
            const response = await fetch('/api/admin/export/customers', {
                headers: this.getAuthHeaders()
            });
            if (!response.ok) throw new Error('Failed to load customers');
            const csv = await response.text();
            const rows = this._parseCsv(csv);
            this._allCustomers = rows.slice(1); // skip header

            this._renderCustomersTable(this._allCustomers);
        } catch (err) {
            console.error('Failed to load customers tab:', err);
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--accent-red);padding:2rem;">Could not load customers. Please retry.</td></tr>`;
        }
    }

    _renderCustomersTable(rows) {
        const tbody = document.getElementById('customersTableBody');
        document.getElementById('customerCount').innerText = `${rows.length} customer(s)`;
        if (rows.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-secondary);padding:2rem;">No customers found.</td></tr>`;
            return;
        }
        tbody.innerHTML = rows.map((cols, i) => `
            <tr style="cursor: pointer;" onclick="app.showCustomerHistory('${cols[0] || ''}', '${(cols[1] || '').replace(/'/g, "\\'")}')" title="Click to view purchase history">
                <td style="color:var(--text-secondary);font-size:0.8rem;">${i + 1}</td>
                <td><span style="font-family:monospace;font-size:0.82rem;color:var(--accent-indigo);">${cols[0] || ''}</span></td>
                <td><strong>${cols[1] || ''}</strong></td>
                <td>${cols[2] || ''}</td>
                <td>${cols[3] || ''}</td>
                <td>${cols[4] || ''}</td>
            </tr>`).join('');
    }

    filterCustomersTable(query) {
        if (!this._allCustomers) return;
        const q = query.toLowerCase();
        const filtered = q
            ? this._allCustomers.filter(r => r.join(' ').toLowerCase().includes(q))
            : this._allCustomers;
        this._renderCustomersTable(filtered);
    }

    // ── Items Sold Tab ───────────────────────────────────────────────────────

    async loadItemsSoldTab() {
        const tbody = document.getElementById('itemsTableBody');
        tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;color:var(--text-secondary);padding:2rem;">Loading...</td></tr>`;
        try {
            const response = await fetch('/api/admin/export/items-sold', {
                headers: this.getAuthHeaders()
            });
            if (!response.ok) throw new Error('Failed');
            const csv = await response.text();
            const rows = this._parseCsv(csv);
            this._allItems = rows.slice(1);

            this._renderItemsTable(this._allItems);
        } catch (err) {
            console.error('Failed to load items tab:', err);
            tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;color:var(--accent-red);padding:2rem;">Could not load items sold data.</td></tr>`;
        }
    }

    _renderItemsTable(rows) {
        const tbody = document.getElementById('itemsTableBody');
        document.getElementById('itemsCount').innerText = `${rows.length} line item(s)`;
        if (rows.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;color:var(--text-secondary);padding:2rem;">No items sold data found.</td></tr>`;
            return;
        }
        tbody.innerHTML = rows.map(cols => {
            const gstTotal = (parseFloat(cols[7] || 0) + parseFloat(cols[8] || 0)).toFixed(2);
            return `
            <tr>
                <td><span style="font-family:monospace;font-size:0.8rem;color:var(--accent-indigo);">${cols[0] || ''}</span></td>
                <td style="font-size:0.8rem;color:var(--text-secondary);">${cols[1] || ''}</td>
                <td><span style="font-family:monospace;font-size:0.8rem;">${cols[2] || ''}</span></td>
                <td><strong>${cols[3] || ''}</strong></td>
                <td style="text-align:center;">${cols[4] || ''}</td>
                <td style="text-align:right;">₹${parseFloat(cols[5] || 0).toFixed(2)}</td>
                <td style="text-align:right;color:var(--accent-emerald);">₹${parseFloat(cols[6] || 0).toFixed(2)}</td>
                <td style="text-align:right;color:var(--text-secondary);">₹${gstTotal}</td>
                <td style="text-align:right;color:var(--accent-purple);">₹${parseFloat(cols[10] || 0).toFixed(2)}</td>
                <td style="text-align:right;font-weight:700;">₹${parseFloat(cols[11] || 0).toFixed(2)}</td>
            </tr>`;
        }).join('');
    }

    filterItemsTable(query) {
        if (!this._allItems) return;
        const q = query.toLowerCase();
        const filtered = q
            ? this._allItems.filter(r => r.join(' ').toLowerCase().includes(q))
            : this._allItems;
        this._renderItemsTable(filtered);
    }

    // ── All Sales Tab ────────────────────────────────────────────────────────

    async loadAllSalesTab(filter = {}) {
        const tbody = document.getElementById('allSalesTableBody');
        tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;color:var(--text-secondary);padding:2rem;">Loading...</td></tr>`;

        // Build query string from filter: { date } or { month, year } or {}
        let qs = '';
        if (filter.date) {
            qs = `?date=${filter.date}`;
        } else if (filter.month && filter.year) {
            qs = `?month=${filter.month}&year=${filter.year}`;
        }

        // Update tab label to show active date range
        const labelEl = document.getElementById('allSalesCount');
        if (labelEl && qs) {
            labelEl.innerText = 'Loading...';
        }

        try {
            const response = await fetch(`/api/bill${qs}`, {
                headers: this.getAuthHeaders()
            });
            if (!response.ok) throw new Error('Failed');
            const bills = await response.json();
            this._allSales = bills;

            // Calculate Cash vs Online totals
            let cashRevenue = 0;
            let cashCount = 0;
            let onlineRevenue = 0;
            let onlineCount = 0;

            bills.forEach(b => {
                const mode = (b.paymentMode || 'CASH').toUpperCase();
                if (mode === 'ONLINE') {
                    onlineRevenue += b.finalAmount;
                    onlineCount++;
                } else {
                    cashRevenue += b.finalAmount;
                    cashCount++;
                }
            });

            const statCashRevEl = document.getElementById('statCashRevenue');
            const statCashBillsEl = document.getElementById('statCashBills');
            const statOnlineRevEl = document.getElementById('statOnlineRevenue');
            const statOnlineBillsEl = document.getElementById('statOnlineBills');

            if (statCashRevEl) statCashRevEl.innerText = `₹${cashRevenue.toFixed(2)}`;
            if (statCashBillsEl) statCashBillsEl.innerText = `${cashCount} bill(s)`;
            if (statOnlineRevEl) statOnlineRevEl.innerText = `₹${onlineRevenue.toFixed(2)}`;
            if (statOnlineBillsEl) statOnlineBillsEl.innerText = `${onlineCount} bill(s)`;

            this._renderAllSalesTable(bills);
        } catch (err) {
            console.error('Failed to load all sales:', err);
            tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;color:var(--accent-red);padding:2rem;">Could not load invoices.</td></tr>`;
        }
    }

    _renderAllSalesTable(bills) {
        const tbody = document.getElementById('allSalesTableBody');
        document.getElementById('allSalesCount').innerText = `${bills.length} invoice(s)`;
        if (bills.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;color:var(--text-secondary);padding:2rem;">No invoices found.</td></tr>`;
            return;
        }
        tbody.innerHTML = bills.map(b => {
            const gst = (b.cgst + b.sgst).toFixed(2);
            const dateStr = new Date(b.billDate).toLocaleString('en-IN', { dateStyle: 'short', timeStyle: 'short' });
            const mode = b.paymentMode || 'CASH';
            const badgeStyle = mode === 'ONLINE' 
                ? 'background: rgba(16, 185, 129, 0.1); color: rgb(52, 211, 153); border: 1px solid rgba(16, 185, 129, 0.2);' 
                : 'background: rgba(99, 102, 241, 0.1); color: rgb(165, 180, 252); border: 1px solid rgba(99, 102, 241, 0.2);';
            return `
            <tr>
                <td><span style="font-family:monospace;font-size:0.82rem;color:var(--accent-indigo);cursor:pointer;"
                          onclick="app.searchAndShowInvoice('${b.billId}')" title="Click to view invoice">${b.billId}</span></td>
                <td><strong>${b.customerName || 'Walk-in'}</strong></td>
                <td style="font-size:0.82rem;">${b.customerMobile || '—'}</td>
                <td>
                    <span class="badge badge-indigo" style="margin-right: 0.25rem;">${b.cashierId}</span>
                    <span class="badge" style="${badgeStyle}">${mode}</span>
                </td>
                <td style="font-size:0.8rem;color:var(--text-secondary);">${dateStr}</td>
                <td style="text-align:right;">₹${b.taxableValue.toFixed(2)}</td>
                <td style="text-align:right;color:var(--text-secondary);">₹${gst}</td>
                <td style="text-align:right;color:var(--accent-purple);">₹${b.discount.toFixed(2)}</td>
                <td style="text-align:right;font-weight:700;color:var(--accent-emerald);">₹${b.finalAmount.toFixed(2)}</td>
                <td>
                    <button class="btn btn-secondary" style="width:auto;padding:0.2rem 0.6rem;font-size:0.75rem;"
                            onclick="app.searchAndShowInvoice('${b.billId}')">View</button>
                </td>
            </tr>`;
        }).join('');
    }

    filterAllSalesTable(query) {
        if (!this._allSales) return;
        const q = query.toLowerCase();
        const filtered = q
            ? this._allSales.filter(b =>
                (b.billId || '').toLowerCase().includes(q) ||
                (b.customerName || '').toLowerCase().includes(q) ||
                (b.customerMobile || '').toLowerCase().includes(q) ||
                (b.cashierId || '').toLowerCase().includes(q))
            : this._allSales;
        this._renderAllSalesTable(filtered);
    }

    // ── Smart Export ─────────────────────────────────────────────────────────

    exportActiveTab() {
        const tab = this.currentDataTab || 'sales';
        const exportMap = {
            sales:     ['/api/admin/export/sales',      'sales_report.csv'],
            customers: ['/api/admin/export/customers',   'customers_list.csv'],
            items:     ['/api/admin/export/items-sold',  'items_sold_report.csv'],
            allsales:  ['/api/admin/export/sales',       'all_invoices.csv']
        };
        const [url, filename] = exportMap[tab] || exportMap.sales;
        this.exportCsv(url, filename);
    }

    // ── CSV Parser helper ─────────────────────────────────────────────────────

    _parseCsv(text) {
        return text.trim().split('\n').map(line => {
            const cols = [];
            let cur = '';
            let inQuotes = false;
            for (let i = 0; i < line.length; i++) {
                const ch = line[i];
                if (ch === '"') {
                    inQuotes = !inQuotes;
                } else if (ch === ',' && !inQuotes) {
                    cols.push(cur.trim());
                    cur = '';
                } else {
                    cur += ch;
                }
            }
            cols.push(cur.trim());
            return cols;
        });
    }
    // ── Analytics & Visual Reports ────────────────────────────────────────────

    async loadAnalytics() {
        const thresholdInput = document.getElementById('lowStockThreshold');
        const threshold = thresholdInput ? parseInt(thresholdInput.value) || 10 : 10;

        try {
            const response = await fetch(`/api/admin/analytics?threshold=${threshold}`, {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) return;

            const data = await response.json();

            // 1. Render Low Stock Alerts
            const lowStockBody = document.getElementById('lowStockTableBody');
            if (lowStockBody) {
                if (data.lowStockProducts.length === 0) {
                    lowStockBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--text-secondary); padding: 1.5rem;">No low stock products.</td></tr>`;
                } else {
                    lowStockBody.innerHTML = data.lowStockProducts.map(p => `
                        <tr>
                            <td><strong>${p.productId}</strong></td>
                            <td>${p.productName}</td>
                            <td style="text-align: right; color: rgb(248, 113, 113); font-weight: bold;">${p.availableQuantity} units</td>
                        </tr>
                    `).join('');
                }
            }

            // 2. Render Dead Stock Alerts
            const deadStockBody = document.getElementById('deadStockTableBody');
            if (deadStockBody) {
                if (data.deadStockProducts.length === 0) {
                    deadStockBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--text-secondary); padding: 1.5rem;">No dead stock products.</td></tr>`;
                } else {
                    deadStockBody.innerHTML = data.deadStockProducts.map(p => `
                        <tr style="background: rgba(220, 38, 38, 0.05);">
                            <td><strong>${p.productId}</strong></td>
                            <td style="color: rgb(248, 113, 113);">${p.productName}</td>
                            <td style="text-align: right; color: var(--text-secondary);">${p.availableQuantity} units</td>
                        </tr>
                    `).join('');
                }
            }

            // 3. Render Charts
            this.renderCharts(data.dailySalesTrend, data.monthlyCashierShare);

        } catch (err) {
            console.error('Failed to load analytics:', err);
        }
    }

    renderCharts(dailySales, monthlyShare) {
        // Daily Sales Chart
        const dailyCanvas = document.getElementById('dailySalesChart');
        if (dailyCanvas) {
            if (this.dailyChartInstance) {
                this.dailyChartInstance.destroy();
            }

            const labels = dailySales.map(d => d.date);
            const salesData = dailySales.map(d => d.sales);

            this.dailyChartInstance = new Chart(dailyCanvas, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Revenue (₹)',
                        data: salesData,
                        backgroundColor: 'rgba(99, 102, 241, 0.6)',
                        borderColor: 'rgb(99, 102, 241)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            grid: {
                                color: 'rgba(255, 255, 255, 0.05)'
                            },
                            ticks: {
                                color: 'var(--text-secondary)'
                            }
                        },
                        x: {
                            grid: {
                                color: 'rgba(255, 255, 255, 0.05)'
                            },
                            ticks: {
                                color: 'var(--text-secondary)'
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            display: false
                        }
                    }
                }
            });
        }

        // Monthly Revenue Share by Cashier (Doughnut Chart)
        const monthlyCanvas = document.getElementById('monthlyShareChart');
        if (monthlyCanvas) {
            if (this.monthlyChartInstance) {
                this.monthlyChartInstance.destroy();
            }

            const labels = monthlyShare.map(m => m.cashierName);
            const sharesData = monthlyShare.map(m => m.totalSales);

            const colors = [
                'rgba(99, 102, 241, 0.7)',
                'rgba(16, 185, 129, 0.7)',
                'rgba(139, 92, 246, 0.7)',
                'rgba(245, 158, 11, 0.7)',
                'rgba(239, 68, 68, 0.7)'
            ];

            this.monthlyChartInstance = new Chart(monthlyCanvas, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: sharesData,
                        backgroundColor: colors.slice(0, labels.length),
                        borderWidth: 1,
                        borderColor: '#111827'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                color: 'var(--text-secondary)',
                                boxWidth: 12
                            }
                        }
                    }
                }
            });
        }
    }

    exportLowStock() {
        const thresholdInput = document.getElementById('lowStockThreshold');
        const threshold = thresholdInput ? parseInt(thresholdInput.value) || 10 : 10;
        this.exportCsv(`/api/admin/export/low-stock?threshold=${threshold}`, `low_stock_report_threshold_${threshold}.csv`);
    }

    showCustomerHistory(customerId, customerName) {
        if (!this._allSales) return;
        const history = this._allSales.filter(b => b.customerId === customerId);

        const historyPanel = document.getElementById('customerHistoryPanel');
        const historyTitle = document.getElementById('historyPanelTitle');
        const historyBody = document.getElementById('customerHistoryTableBody');

        if (historyPanel && historyTitle && historyBody) {
            historyTitle.innerText = `Purchase History for ${customerName} (ID: ${customerId})`;
            
            if (history.length === 0) {
                historyBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-secondary); padding: 1.5rem;">No purchases found for this customer.</td></tr>`;
            } else {
                historyBody.innerHTML = history.map(b => {
                    const dateStr = new Date(b.billDate).toLocaleString('en-IN', { dateStyle: 'short', timeStyle: 'short' });
                    return `
                        <tr>
                            <td><span style="font-family:monospace;font-weight:bold;color:var(--accent-indigo);">${b.billId}</span></td>
                            <td>${dateStr}</td>
                            <td>${b.paymentMode || 'CASH'}</td>
                            <td style="text-align: right; font-weight: bold; color: var(--accent-emerald);">₹${b.finalAmount.toFixed(2)}</td>
                            <td>
                                <button class="btn btn-secondary" style="width: auto; padding: 0.25rem 0.5rem; font-size: 0.75rem;" onclick="app.searchAndShowInvoice('${b.billId}')">View Bill</button>
                            </td>
                        </tr>
                    `;
                }).join('');
            }
            historyPanel.style.display = 'block';
            historyPanel.scrollIntoView({ behavior: 'smooth' });
        }
    }

    // ── Cart Sync & Draft helper functions ────────────────────────────────────
    async syncCartLock() {
        try {
            const cartPayload = this.cart.map(item => ({
                productId: item.product.productId,
                quantity: item.quantity
            }));
            await fetch('/api/product/cart-sync', {
                method: 'POST',
                headers: {
                    ...this.getAuthHeaders(),
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    cashierId: this.userId,
                    cart: cartPayload
                })
            });
        } catch (e) {
            console.error("Cart sync failed", e);
        }
    }

    async saveDraftToServer() {
        const draftPayload = this.cart.map(item => ({
            productId: item.product.productId,
            quantity: item.quantity
        }));
        
        try {
            await fetch('/api/draft', {
                method: 'POST',
                headers: {
                    ...this.getAuthHeaders(),
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    cashierId: this.userId,
                    draftJson: JSON.stringify(draftPayload)
                })
            });
        } catch (e) {
            console.error("Failed to save draft", e);
        }
    }

    async checkAndRestoreDraft() {
        try {
            const res = await fetch(`/api/draft?cashierId=${this.userId}`, {
                headers: this.getAuthHeaders()
            });
            if (res.ok) {
                const data = await res.json();
                if (data.status === 'found' && data.draft && data.draft.length > 0) {
                    this.cart = [];
                    for (let d of data.draft) {
                        const prodRes = await fetch(`/api/product?id=${d.productId}&cashierId=${this.userId}`, {
                            headers: this.getAuthHeaders()
                        });
                        if (prodRes.ok) {
                            const product = await prodRes.json();
                            this.cart.push({
                                product: product,
                                quantity: Math.min(d.quantity, product.availableQuantity)
                            });
                        }
                    }
                    this.renderCart();
                    this.showAlert('success', 'Draft bill restored successfully.');
                }
            }
        } catch (e) {
            console.error("Failed to restore draft", e);
        }
    }
}
