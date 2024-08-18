PROJ_DIR=/Users/mustafa/test/mtls-client

DEVICE_ID="123ABC456"
CR_PASSWORD="12345678"
CERT_DIR=$PROJ_DIR/work/certs
ROOT_CERT_DIR=/Users/mustafa/test/mtls-server/work/certs

echo $ROOT_CERT_DIR

mkdir -p $CERT_DIR
rm -rf $CERT_DIR/*.*


# CLIENT
echo -e "SG\nSG\nSG\nZMYORGC\nZMYORGUNITC\n${DEVICE_ID}\na@a.com\n12345678\n12345678" | openssl req -new -newkey rsa:4096 -keyout $CERT_DIR/client.key -out $CERT_DIR/client.csr -passout pass:$CR_PASSWORD

openssl x509 -req -CA $ROOT_CERT_DIR/rootCA.crt -CAkey $CERT_DIR/rootCA.key -in $CERT_DIR/client.csr -out $CERT_DIR/client.crt -days 365 -CAcreateserial -passin pass:$CR_PASSWORD

openssl pkcs12 -export -out $CERT_DIR/client.p12 -name "client" -inkey $CERT_DIR/client.key -in $CERT_DIR/client.crt -passin pass:$CR_PASSWORD -password pass:$CR_PASSWORD


#cp -rf $CERT_DIR/*.jks $PROJ_DIR/src/main/resources