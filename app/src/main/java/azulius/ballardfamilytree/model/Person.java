package azulius.ballardfamilytree.model;

/**
 * Created by Azulius on 3/18/16.
 */
public class Person {

    private String firstName;
    private String lastName;
    private String gender;
    private String id;
    private String fatherId;
    private String motherId;

    public Person (String first, String last, String gender, String id, String dadId, String momId) {
        this.firstName = first;
        this.lastName = last;
        this.gender = gender;
        this.id = id;
        this.fatherId = dadId;
        this.motherId = momId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getId() {
        return id;
    }

    public String getFatherId() {
        return fatherId;
    }

    public String getMotherId() {
        return motherId;
    }
}
