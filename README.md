For compile



javac src/models/\*.java src/db/\*.java src/repositories/\*.java src/services/\*.java src/handlers/\*.java src/Main.java





For run



java -cp ".;mysql-connector-j-8.4.0.jar;src" Main





Test Server

Run powershell as administrator and run the following command



1\. Login Test (AuthHandler)



Invoke-RestMethod -Uri "http://localhost:8080/api/login" -Method Post -Body '{"userId":"USR-001", "password":"admin123"}'



2\. Barcode Scanner Test (ProductHandler)



Invoke-RestMethod -Uri "http://localhost:8080/api/product?id=PRODUCT-101" -Method Get



3\. Customer Test (CustomerHandler)



Invoke-RestMethod -Uri "http://localhost:8080/api/customer?mobile=9876543210" -Method Get



4\. Final Bill Generation Test (BillHandler)



Invoke-RestMethod -Uri "http://localhost:8080/api/checkout" -Method Post -Body '{"customerName":"Rahul Sharma", "cashierId":"USR-001", "productIds":\["PRODUCT-101"], "quantities":\[2]}'





5\. Return API Test (Customer Refund)



Invoke-RestMethod -Uri "http://localhost:8080/api/return" -Method Post -Body '{"productId":"PRODUCT-101", "quantity":"1"}'



6\. Admin Report API Test (Daily Sales)



Invoke-RestMethod -Uri "http://localhost:8080/api/admin/report" -Method Get



7\. Customer Update API Test (Edit Details)



Invoke-RestMethod -Uri "http://localhost:8080/api/customer/update" -Method Put -Body '{"mobile":"9876543210", "location":"Indore"}'



8\. Test DraftHandler (Bill Pause Request)



Invoke-RestMethod -Uri "http://localhost:8080/api/draft" -Method Post





9\. Test CartSyncHandler (Cart Backup Request)



Invoke-RestMethod -Uri "http://localhost:8080/api/cart/sync" -Method Post







List of Files created:



Folder: src/models/

1\.	Customer.java: Completed

2\.	Product.java: Completed

3\.	User.java: Completed

4\.	BillItem.java: Completed

5\.	Bill.java: Completed



Folder: src/db/

1\.	DatabaseConfig.java: Completed



Folder: src/repositories/

1\.	CustomerRepository.java: Completed

2\.	ProductRepository.java: Completed

3\.	UserRepository.java: Completed

4\.	BillRepository.java: Completed



Folder: src/services/

1\.	BillingService.java: Completed

2\.	ReportingService.java: Completed

3\.	ReturnService.java: Completed



Folder: src/handlers/

1\.	HandlerUtils.java: Completed

2\.	AuthHandler.java: Completed

3\.	ProductHandler.java: Completed

4\.	CustomerHandler.java: Completed

5\.	BillHandler.java: Completed

6\.	ReturnHandler.java: Completed

7\.	AdminReportHandler.java: Completed

8\.	CustomerUpdateHandler.java: Completed

9\.	DraftHandler.java: Completed





Folder: src/

1\.	Main.java: For testing (Test execution \& Web Server boot)

