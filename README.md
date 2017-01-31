SMS-confirmation library to wrap Nexmo.com sms service.
======
First you have to register at nexmo.com, or modify code to use other provider.
Finally you should obtain two hex numbers: 8-hex-digit API_KEY and 16-hex-digit API_SECRET.

Usage:
------

1. Add ossrh SHAPSHOT repository to yous pom.xml file:

```xml
     <repositories>
         ...
         <repository>
             <id>ossrh</id>
             <url>
                 https://oss.sonatype.org/content/repositories/snapshots/
             </url>
         </repository>
     </repositories>
```
2. Import package
```java
import io.github.ac2epsilon
```
3. Create new confirmation object:
```java
    SmsConfirmation sms = new SmsConfirmation("MyCompany", API_KEY, API_SECRET);
```
First parameter _company_ used as "namespace", so you can serve several projects/companies/parties.
API_KEY and API_SECRET are values, provided by Nexmo.com during your registration.

 4. Call _send_ method:
```java
    String code = sms.send("380639003365");
```
This will generate random 4-digit number and send it to given phone. Sole parameter is
_phone_, which have to be in international format, starting with country code, and consist
of numbers (see Nexmo API documentation for further information).

SMS body will be like "Your confirmation code: 9304" and From: will consist of _company_, set in
constructor. If you wish to change this there is overloaded version of method:
```java
    String code = sms.send("380639003365","Enter this code: ~");
```
Tilda ~ denotes place, where code have to be. If provided message contains no tilda character,
IllegalArgumentException exception will be thrown.

Return value will be generated code, anything else but 4-digit string should be treated as error
(actually code returns "fail" in such event). You can, of course, keep this code. However our lib
either persist this code along with phone number during 24h, so you can simplify your life just
keeping track of phone.

We use phone number as primary key. This means that it is unique, so no duplications allowed.
If new request arrives before code confirmation, new code will overwrite old one.

5. Call _check_ method:
```java
    String hash = sms.check(phone, code);
```
As soon as user get code on his/her phone and enters it into confirmation dialogue, you can compare
it against stored one. You provide two parameters, phone and code. Be careful, as code valid during 24h
period of time. If provided code matches with sent and stored one result of call will be SHA-1 hash
as function of phone+code+datetime-of-confirmation. So you can back track confirmation.

Once confirmation complete record will persist in DB forever and stay immutable. Further valid
conformations will not affect date of confirmation, so resulting hash will be unchanged.