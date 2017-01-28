package io.github.ac2epsilon;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

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
    String hash;

    public Confirmation() {

    }

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
        String sign = id+"-"+ code+"-"+issued;
        byte[] m= {};
        try {
            m = MessageDigest.getInstance("SHA-1").digest(sign.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {} catch (UnsupportedEncodingException e) {}
        hash = bytesToHex(m);
    }
    @Override
    public String toString() {
        return "Confirmation: ["+id+"-"+code+"-"+issued+"-"+hash+"]";
    }
}
