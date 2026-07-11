package models;
public class Customer
{
private String customerId;
private String name;
private String mobileNumber;
private int age;
private String location;
public Customer()
{
}
public Customer(String customerID, String name, String mobileNumber, int age, String location)
{
this.customerId = customerID;
this.name = name;
this.mobileNumber = mobileNumber;
this.age = age;
this.location = location;
}
public String getCustomerId()
{
return customerId;
}
public void setCustomerId(String customerId)
{
this.customerId = customerId;
}
public String getName()
{
return name;
}
public void setName(String name)
{
this.name = name;
}
public String getMobileNumber()
{
return mobileNumber;
}
public void setMobileNUmber(String mobileNumber)
{
this.mobileNumber = mobileNumber;
}
public int getAge()
{
return age;
}
public void setAge(int age)
{
this.age = age;
}
public String getLocation()
{
return location;
}
public void setLocation(String location)
{
this.location = location;
}
}
