
package net.lianbian.crm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "customer", propOrder = {
    "address",
    "decidedzoneId",
    "id",
    "name",
    "station",
    "telephone"
})
public class Customer {

    protected String address;
    @XmlElement(name = "decidedzone_id")
    protected String decidedzoneId;
    protected int id;
    protected String name;
    protected String station;
    protected String telephone;

    /**
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDecidedzoneId() {
        return decidedzoneId;
    }

    /**
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDecidedzoneId(String value) {
        this.decidedzoneId = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int value) {
        this.id = value;
    }

    /**
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStation() {
        return station;
    }

    /**
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStation(String value) {
        this.station = value;
    }

    /**
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTelephone(String value) {
        this.telephone = value;
    }

}
