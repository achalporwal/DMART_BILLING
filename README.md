For compile



javac src/models/\*.java src/db/\*.java src/repositories/\*.java src/services/\*.java src/handlers/\*.java src/Main.java





For run


java -cp ".;mysql-connector-j-8.4.0.jar;src" Main





Test Server

Run powershell as administrator and run the following command



Invoke-RestMethod -Uri "http://localhost:8080/api/login" -Method Post -Body '{"userId":"USR-001", "password":"admin123"}' 



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

3\.	

4\.

5\.





Folder: src/

1\.	Main.java: For testing.

