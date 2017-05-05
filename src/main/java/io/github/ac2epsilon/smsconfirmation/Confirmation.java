package io.github.ac2epsilon.smsconfirmation;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ac2 on 24.01.17.
 */

/*
  Entity to encapsulate Confirmation request and answer(s), including
*/

@Entity
public class Confirmation {
    @PrimaryKey
    String id;
    String code;
    String issued;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE, name = "keyByHash")
    String hash;

    /**
     * Default constructor as needed for Berkley DB JE requirements, not to be used in code
     */
    public Confirmation() {

    }

    /**
     * Constructor to use in code
     *
     * @param _id Actually id stands for phone number, but we want no more then one unique request for one
     *            number, with rewriting, so using phone as id fulfills our needs
     * @param _code Last 4-digit confirmation code, sent to given phone. While not confirmed record will be rewritten
     *              by any following request, to keep only one open confirmation request for given number
     */
    public Confirmation(String _id, String _code) {
        id = _id;
        code=_code;
    }
/**
*
* Internal byte[] to hexString converter
*
* */
    private String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) builder.append(String.format("%02x", b));
        return builder.toString();
    }
/**
 *
 * Method to mark Confirmation as checked, filling in fields "issued" and "hash"
 *
 * */
    public void setTokenHash() {
        issued = SmsUtil.timestamp();
        String sign = getSign();
        byte[] m= {};
        try {
            m = MessageDigest.getInstance("SHA-1").digest(sign.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {} catch (UnsupportedEncodingException e) {}
        hash = bytesToHex(m);
    }

    /**
     * Compose hash key value
     *
     * @return Key to form hash
     */
    public String getSign() {
        if (issued!=null)
            return id+"-"+ code+"-"+issued;
        else
            return id+"-"+ code;
    }
    @Override
    public String toString() {
        return "Confirmation: ["+id+"-"+code+"-"+issued+"-"+hash+"]";
    }
}
