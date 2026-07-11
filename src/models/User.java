package models;
public class User
{
private String userId;
private String name;
private String password;
private String role;
private boolean isActive;
public User()
{
}
public User(String userId, String name, String password, String role, boolean isActive)
{
this.userId = userId;
this.name = name;
this.password = password;
this.role = role;
this.isActive = isActive;
}
public String getUserId()
{
return userId;
}
public void setUserId(String userId)
{
this.userId = userId;
}
public String getName()
{
return name;
}
public void setName(String Name)
{
this.name = name;
}
public String getPassword()
{
return password;
}
public void setPassword(String password)
{
this.password = password;
}
public String getRole()
{
return role;
}
public void setRole(String role)
{
this.role = role;
}
public boolean isActive()
{
return isActive;
}
public void setActive(boolean active)
{
this.isActive = active;
}
}
