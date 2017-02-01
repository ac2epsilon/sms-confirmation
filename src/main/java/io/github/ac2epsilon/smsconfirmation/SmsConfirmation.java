package io.github.ac2epsilon;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static io.github.ac2epsilon.SmsUtil.getVerificationToken;
import static java.lang.System.exit;

/**
 * Created by ac2 on 25.01.17.
 *
 * @author  Arsenii Chebotarov
 * @version 1.0
  */
public class SmsConfirmation {
    BdbTools bdb;

    private final String BASE_URL = "https://rest.nexmo.com/sms/json";
    private String company;
    private String apiKey;
    private String apiSecret;
    private String defaultMessage = "Your confirmation code: ~";

    /**
     *
     * Check error condition and exit with system code 1 if it is true
     *
     * @param condition Condition to check
     * @param message Error message to print out
     * @param usage Common usage instructions
     *
     */
    static private void ifErrorExit(boolean condition, String message, String usage) {
        if (condition) {
            System.out.println(message);
            System.out.println(usage);
            exit(1);
        }
    };
    /**
     *
     * Static main method, to make easy check functionality in terminal menu driven style
     *
     * @param args Command-line parameters
     *
     */
    public static void main (String[] args) {
        Pattern hex16 = Pattern.compile("[0-9a-f]{16}");
        Pattern hex8 = Pattern.compile("[0-9a-f]{8}");
        String usage =
            "Usage: java -jar SmsConfirm <company-name> <8-hexdigit-API-key> <16-hexdigit-API-secret>\n"+
            "    - company-name will be used as From field of the message\n"+
            "    - API-key and API-secret are those provided by nexmo.com\n";
        ifErrorExit(args.length<3, "Not enought arguments, must be three", usage);
        ifErrorExit(args[1].length()!=8, "Second argument must be 8 chars long: "+args[0], usage);
        ifErrorExit(args[2].length()!=16, "Third argument must be 16 chars long: "+args[1], usage);
        ifErrorExit(!hex8.matcher(args[1]).matches(), "Second argument must be HEX", usage);
        ifErrorExit(!hex16.matcher(args[2]).matches(), "Third argument must be HEX", usage);
        SmsConfirmation sms = new SmsConfirmation(args[0],args[1],args[2]);
        boolean exitCondition = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (!exitCondition) {
            System.out.print("> ");
            String line = null;
            try { line = br.readLine(); } catch (IOException e) {}
            if (line.length()==0) line="h";

            String[] t = line.split(" ");
            switch (t[0].charAt(0)) {
                case 'q': exitCondition = true;
                case 'r':
                    if (t.length > 1) {
                        String phone = t[1];
                        String code = sms.send(phone);
                        System.out.println(code);
                    } else System.out.println("You have to specify arguments");
                    break;
                case 'c':
                    if (t.length > 2) {
                        String phone = t[1];
                        String code = t[2];
                        String hash = sms.check(phone, code);
                        System.out.println(hash);
                    } else System.out.println("You have to specify arguments");
                    break;
                case 'l':
                    sms.bdb.iterate(c -> System.out.println(c));
                    break;
                default:
                    System.out.println(
        "Commands:\nq - quit from app\nr <phone> - request new random 4-digit confirmation code\n"+
        "c <phone> <code> - check code validity\nl - list pending and confirmed recorded requests"
                    );
        }
    }}

    /**
     * Constructor accepts three parameters
     * @param company Serves as "namespace" to divide all confirmations by domains, also used in as part of hash
     *                and also mentioned as From: field for sent SMS
     * @param apiKey API_KEY provided by nexmo.com
     * @param apiSecret API_SECRET provided by nexmo.com
     */
    public SmsConfirmation(String company, String apiKey, String apiSecret) {
        this.company = company;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
// we use company name to divide records in separate "namespaces"
        this.bdb = new BdbTools(company);
    }
    /**
     * @param userPhone Phone number where you send confirmation code
     * @return 4-digit confirmation code. Any other
     */
    public String send(String userPhone) {
        return send(userPhone, defaultMessage);
    }
    /**
     * @param userPhone Phone number where you send confirmation code
     * @param message Optional user defined message. Message should contain ~ sign, which will be replaced
     * with 4-digit code, or IllegalArgumentException will rise.
     * @return 4-digit confirmation code as String. Any other return have to be treated as fail
     * @throws IllegalArgumentException Exception will throw
     */
    public String send(String userPhone, String message) {
        if (message.indexOf('~')<0) {
            throw new IllegalArgumentException("SMS message should include ~ sign");
        }
        Character kind = SmsUtil.detectType(userPhone);
        String code = "fail"; // generally speaking any non-4-digit return have to be treated as fail
        if (kind.equals('P')) {
            code = sendSms(userPhone, message);
            /* Confirmation confirmation = */ bdb.add(userPhone, code);
        } else {
            throw new IllegalArgumentException("Provided phone number not confirms API rules");
        }
        return code;
    }

    /**
     * Check if provided 4-digit code equals to generated and stored one
     * @param userPhone Phone to make check
     * @param code Given code, which will be checked
     * @return Hash code, which represents phone-code-time_of_check. Once code is checked
     * this hash will remain constant, so can be used as reference or Primary Key for user
     */
    public String check(String userPhone, String code) {
        String result = "fail";
        Confirmation saved =  bdb.get(userPhone);
        if (saved!=null && saved.code.equals(code)) {
            if (saved.hash==null) {
                saved.setTokenHash();
                bdb.putNoTTL(saved); // strip TTL if confirmation succeeds
            }
            result =  saved.hash;
        }
        return result;
    }

    /**
     * Real private method to send SMS with
     * @param userPhone Phone number to send SMS
     * @param body SMS text body
     * @return 4-digit random code of SMS sent
     */
    private String sendSms(String userPhone, String body)  {
        String code = getVerificationToken();
        String msgWithCode = body.replaceAll("~",code);

        List<BasicNameValuePair> parameters = new ArrayList<>(Arrays.asList(
            new BasicNameValuePair("from", company),
            new BasicNameValuePair("to", userPhone),
            new BasicNameValuePair("text", msgWithCode),
            new BasicNameValuePair("api_key", apiKey),
            new BasicNameValuePair("api_secret", apiSecret)
        ));

        HttpPost method = new HttpPost(BASE_URL);
        try {
          method.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
        }  catch (UnsupportedEncodingException uee) {}

        try {
          HttpResponse httpResponse = HttpClientUtils.getInstance(5000, 30000)
              .getNewHttpClient()
              .execute(method);
          int status = httpResponse.getStatusLine().getStatusCode();
          if (status != 200)
            throw new Exception("Non-200 response ["+status+"] from Nexmo-HTTPS");
          new BasicResponseHandler().handleResponse(httpResponse);
        } catch (Exception e) { method.abort(); }

        return code;
    }
}
