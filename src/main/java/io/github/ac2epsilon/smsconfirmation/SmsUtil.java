package io.github.ac2epsilon.smsconfirmation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * Created by ac2 on 23.01.17.
 *
 * Some static util code to be used in SMS confirmation code
 *
 */
public class SmsUtil {
/**
 *
 *  Returns formatted string of current date/time
 *
 * @return Long man readable representation of current date/time
 */
    static public String timestamp() {
        DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        return format.format(new Date(System.currentTimeMillis()));
    }
/**
 *
 * Generates random 4-digit string
 *
 * @return Random 4-digit confirmation code as string
 */
    static public String getVerificationToken()  {
        return IntStream.generate(()->ThreadLocalRandom.current().nextInt(10))
            .limit(4)
            .mapToObj(Integer::toString)
            .collect(Collectors.joining());
    }
/**
 *
 *  Detects confirmation types by destination address/phone.
 *  Actually detects only phone numbers in very basic vanilla form
 *
 * @param string Phone number or e-mail, or something else
 * @return One character, with meaning 'U' for undefined, 'P" for phone or 'M" for e-mail
 */
    static Character detectType(String string) {
        Character result = 'U';
        if (Pattern.compile("(^[0-9]{7,23}$)").matcher(string).matches()) result = 'P';
//        if (Pattern.compile("(^[A-Za-z0-9\\.]+@[A-Za-z0-9\\.]+$)").matcher(string).matches()) result = 'M'
        return result;
    }
}
