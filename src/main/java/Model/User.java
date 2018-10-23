/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Minh Quan
 */
public class User implements Serializable {

    private String id;
    private String name;

    public User() {
    }

    public User(String name) {
        this.name = name;
        id = UUID.randomUUID().toString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (null == o || o.getClass() != this.getClass()) {
            return false;
        }

        User other = (User) o;

        return Objects.equals(this.id, other.id) && Objects.equals(this.name, other.name);
    }
}
