package za.org.samac.harvest.util;

import java.util.ArrayList;

/**
 * Below are all of the classes that will store and manipulate all of the individual information.
 */

public class Farm extends DBInfoObject {
    protected String name;
    protected String company;
    protected String email;
    protected String phone;
    protected String province;
    protected String town;
    protected String further;

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

    @Override
    public String toString(){
        return name;
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

    public ArrayList<SearchedItem> search(String text) {
        ArrayList<SearchedItem> result = new ArrayList<>();

        text = text.toLowerCase();

        if (getName() != null && getName().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Name", getName()));
        }

        if (getCompany() != null && getCompany().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Company Name", getCompany()));
        }

        if (getEmail() != null && getEmail().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Email", getEmail()));
        }

        if (getPhone() != null && getPhone().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Phone Number", getPhone()));
        }

        if (getProvince() != null && getProvince().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Province", getProvince()));
        }

        if (getTown() != null && getTown().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Nearest Town", getTown()));
        }

        return result;
    }

    public ArrayList<SearchedItem> search(String text, boolean searchName) {
        ArrayList<SearchedItem> result = new ArrayList<>();

        text = text.toLowerCase();

        if (getCompany() != null && getCompany().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Company Name", getCompany()));
        }

        if (getEmail() != null && getEmail().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Email", getEmail()));
        }

        if (getPhone() != null && getPhone().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Phone Number", getPhone()));
        }

        if (getProvince() != null && getProvince().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Province", getProvince()));
        }

        if (getTown() != null && getTown().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Nearest Town", getTown()));
        }

        return result;
    }
}
