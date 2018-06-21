package za.org.samac.harvest.util;

/**
 * Below are all of the classes that will store and manipulate all of the individual information.
 */

public class Farm{
    protected String name;
    protected String company;
    protected String email;
    protected String phone;
    protected String province;
    protected String town;
    protected String further;
    protected String ID;

    public Farm(){

    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getProvince() {
        return province;
    }

    public String getTown() {
        return town;
    }

    public String getFurther() {
        return further;
    }

    public String getID() {
        return ID;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setFurther(String further) {
        this.further = further;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
