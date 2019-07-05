package com.redhat.gpte;

public class Applicant implements java.io.Serializable {

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     *
     */
    private static final long serialVersionUID = -7080955793548250917L;
    private String firstName;
    private String lastName;
    private String group;
    private int id;
    private int requestAmount;
    private int creditScore;

    private boolean approved;

    public Applicant() {
    }

    public Applicant(int _id, String _firstName, String _lastName, int _requestAmount, int _creditScore, String _group) {
        id = _id;
        firstName = _firstName;
        lastName = _lastName;
        requestAmount = _requestAmount;
        creditScore = _creditScore;
        group = _group;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param requestAmount the requestAmount to set
     */
    public void setRequestAmount(int requestAmount) {
        this.requestAmount = requestAmount;
    }

    /**
     * @param creditScore the creditScore to set
     */
    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getId() {
        return id;
    }

    public int getRequestAmount() {
        return requestAmount;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean _approved) {
        approved = _approved;
    }
}
