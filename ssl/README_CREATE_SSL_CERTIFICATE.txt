Jetty SSL guide :

    http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html#generating-csr-from-keytool

==================

> keytool -keystore bv.keystore -alias budgetview -genkey -keyalg RSA -sigalg SHA256withRSA -ext 'SAN=dns:register.budgetview.fr'

    Enter keystore password: *******
    Re-enter new password: *******
    What is your first and last name?
      [Unknown]:  register.budgetview.fr
    What is the name of your organizational unit?
      [Unknown]:  budgetview
    What is the name of your organization?
      [Unknown]:  budgetview
    What is the name of your City or Locality?
      [Unknown]:  paris
    What is the name of your State or Province?
      [Unknown]:  -
    What is the two-letter country code for this unit?
      [Unknown]:  fr
    Is CN=register.budgetview.fr, OU=budgetview, O=budgetview, L=paris, ST=Unknown, C=fr correct?
      [no]:  yes

    Enter key password for <budgetview>
    	(RETURN if same as keystore password):


==================

The following command generates a key pair in the file bv.key:

> openssl genrsa -aes128 -out bv.key

    passphrase: *******


==================

The following command generates a certificate for the key into the file bv.crt

> openssl req -new -x509 -newkey rsa:2048 -sha256 -key bv.key -out bv.crt

    Enter pass phrase for bv.key: hRvSv53hD0g
    You are about to be asked to enter information that will be incorporated
    into your certificate request.
    What you are about to enter is what is called a Distinguished Name or a DN.
    There are quite a few fields but you can leave some blank
    For some fields there will be a default value,
    If you enter '.', the field will be left blank.
    -----
    Country Name (2 letter code) [AU]:FR
    State or Province Name (full name) [Some-State]:
    Locality Name (eg, city) []:Paris
    Organization Name (eg, company) [Internet Widgits Pty Ltd]:budgetview
    Organizational Unit Name (eg, section) []:budgetview
    Common Name (e.g. server FQDN or YOUR name) []:register.budgetview.fr
    Email Address []:admin@budgetview.fr

==================

Requesting a Trusted Certificate - The following command generates the file bv.csr using OpenSSL for a key in the file bv.key:

> keytool -certreq -alias budgetview -keystore bv.keystore -file bv.csr

==================

Apres envoi certificat Digicert

> cp bv.keystore bv.jks

> keytool -import -trustcacerts -alias budgetview -file register_mybudgetview_fr.p7b -keystore bv.jks

The resulting bv.jks file is the one to be used in the server