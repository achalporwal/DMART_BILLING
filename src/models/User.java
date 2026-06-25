package models;

public class User {
private String userId;
private String name;
private String role; 
private String password;
private boolean isActive = true;

public User() {}

public User(String userId, String name, String role, String password) {
this.userId = userId;
this.name = name;
this.role = role;
this.password = password;
this.isActive = true;
}

public User(String userId, String name, String role, String password, boolean isActive) {
this.userId = userId;
this.name = name;
this.role = role;
this.password = password;
this.isActive = isActive;
}

public String getUserId() {
return userId;
}

public void setUserId(String userId) {
this.userId = userId;
}

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public String getRole() {
return role;
}

public void setRole(String role) {
this.role = role;
}

public String getPassword() {
return password;
}

public void setPassword(String password) {
this.password = password;
}

public boolean isActive() {
return isActive;
}

public void setActive(boolean active) {
isActive = active;
}
}
