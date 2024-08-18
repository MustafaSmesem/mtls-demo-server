#SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

PROJ_DIR=/Users/mustafa/test/mtls-server
CLIENT_DIR=/Users/mustafa/test/mtls-client

# --------------------------------------
ROOT_CA_CN="api-ca.joumer.com"
SERVER_CN="api-server.joumer.com"
CLIENT_CN="api-client.joumer.com"
# --------------------------------------

CERT_DIR=$PROJ_DIR/work/certs
mkdir -p $CERT_DIR
mkdir -p $CLIENT_DIR/work/certs

rm -rf $CERT_DIR/*.*
rm -rf $CLIENT_DIR/work/certs/*.*
rm -rf $PROJ_DIR/src/main/resources/*.jks
rm -rf $PROJ_DIR/src/main/resources/*.p12

# Generate Root Certs
echo -e "SA\nSA\nSA\nEDFAPAY\nEDFAPAYUNIT\n${ROOT_CA_CN}\nadmin@edfapay.com" | openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout $CERT_DIR/rootCA-default.key -out $CERT_DIR/rootCA-default.crt -passout pass:12345678
echo -e "SA\nSA\nSA\nEDFAPAY\nEDFAPAYUNIT\n${ROOT_CA_CN}\nadmin@edfapay.com" | openssl req -x509 -sha256 -days 3650 -newkey rsa:4096 -keyout $CERT_DIR/rootCA.key -out $CERT_DIR/rootCA.crt -passout pass:12345678


# Create OpenSSL configuration file to generate a server certificate
echo "authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
subjectAltName = @alt_names
[alt_names]
DNS.1 = ${SERVER_CN}" > $CERT_DIR/server.ext

# Generate Server csr and sign using default CA certificate
echo -e "SA\nSA\nSA\nEDFAPAYS\nEDFAPAYUNITS\n${SERVER_CN}\nadmin@edfapay.com\n12345678\n12345678" | openssl req -new -newkey rsa:4096 -keyout $CERT_DIR/server.key -out $CERT_DIR/server.csr -passout pass:12345678
openssl x509 -req -CA $CERT_DIR/rootCA-default.crt -CAkey $CERT_DIR/rootCA-default.key -in $CERT_DIR/server.csr -out $CERT_DIR/server.crt -days 365 -CAcreateserial -extfile $CERT_DIR/server.ext -passin pass:12345678

# Generate DEFAULT CLIENT CERTIFICATE and sign using default ca crt
echo -e "SA\nSA\nSA\nEDFAPAYC\nEDFAPAYUNITC\n${CLIENT_CN}\nadmin@edfapay.com\n12345678\n12345678" | openssl req -new -newkey rsa:4096 -keyout $CERT_DIR/client-default.key -out $CERT_DIR/client-default.csr -passout pass:12345678
openssl x509 -req -CA $CERT_DIR/rootCA-default.crt -CAkey $CERT_DIR/rootCA-default.key -in $CERT_DIR/client-default.csr -out $CERT_DIR/client-default.crt -days 365 -CAcreateserial -passin pass:12345678


# Convert certificates and keys files to p12 format
openssl pkcs12 -export -out $CERT_DIR/rootCA-default.p12 -name "${ROOT_CA_CN}" -inkey $CERT_DIR/rootCA-default.key -in $CERT_DIR/rootCA-default.crt -passin pass:12345678 -password pass:12345678
openssl pkcs12 -export -out $CERT_DIR/rootCA.p12 -name "${ROOT_CA_CN}" -inkey $CERT_DIR/rootCA.key -in $CERT_DIR/rootCA.crt -passin pass:12345678 -password pass:12345678
openssl pkcs12 -export -out $CERT_DIR/server.p12 -name "${SERVER_CN}" -inkey $CERT_DIR/server.key -in $CERT_DIR/server.crt -passin pass:12345678 -password pass:12345678
openssl pkcs12 -export -out $CERT_DIR/client-default.p12 -name "${CLIENT_CN}" -inkey $CERT_DIR/client-default.key -in $CERT_DIR/client-default.crt -passin pass:12345678 -password pass:12345678


# Create default keystore and add rootCa default, server, client-default certificates
echo -e '12345678\n12345678\n12345678' | keytool -importkeystore -srckeystore $CERT_DIR/rootCA-default.p12 -srcstoretype PKCS12 -destkeystore $CERT_DIR/myserver_keystore.jks -deststoretype JKS
echo -e '12345678\n12345678\n12345678' | keytool -importkeystore -srckeystore $CERT_DIR/server.p12 -srcstoretype PKCS12 -destkeystore $CERT_DIR/myserver_keystore.jks -deststoretype JKS
echo -e '12345678\n12345678\n12345678' | keytool -importkeystore -srckeystore $CERT_DIR/client.p12 -srcstoretype PKCS12 -destkeystore $CERT_DIR/myserver_keystore.jks -deststoretype JKS

# Create another keystore and add the generator RootCA certificate
echo -e '12345678\n12345678\n12345678' | keytool -importkeystore -srckeystore $CERT_DIR/rootCA.p12 -srcstoretype PKCS12 -destkeystore $CERT_DIR/myclient_keystore.jks -deststoretype JKS

# Create default truststore and add RootCa-default
echo -e '12345678\n12345678' | keytool -import -trustcacerts -noprompt -alias ca -ext san=dns:${SERVER_CN},ip:127.0.0.1 -file $CERT_DIR/rootCA-default.crt -keystore $CERT_DIR/myserver_truststore.jks

# Create another truststore and add the other RootCa certificate
echo -e '12345678\n12345678' | keytool -import -trustcacerts -noprompt -alias ca -ext san=dns:${SERVER_CN},ip:127.0.0.1 -file $CERT_DIR/rootCA.crt -keystore $CERT_DIR/myclient_truststore.jks



# Copy generated stores and add to server resources
cp -rf $CERT_DIR/*.jks $PROJ_DIR/src/main/resources



# Optional for test copy client certificates to client demo project
cp $CERT_DIR/client-default.crt $CLIENT_DIR/work/certs/client.crt
cp $CERT_DIR/client-default.key $CLIENT_DIR/work/certs/client.key
cp $CERT_DIR/client-default.crt $CLIENT_DIR/work/certs
cp $CERT_DIR/client-default.key $CLIENT_DIR/work/certs

#### rootCA certs
cp $CERT_DIR/rootCA-default.crt $CLIENT_DIR/work/certs
cp $CERT_DIR/rootCA.crt $CLIENT_DIR/work/certs