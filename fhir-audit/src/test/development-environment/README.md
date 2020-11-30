# Development Environment

This development environment folder is intentionally not included in the src/test/resources folder.

1. Export the KAFKA_SSL_SECRETS_DIR environment variable

```
export KAFKA_SSL_SECRETS_DIR=$(pwd)
```

2. Build the two node zookeeper and kafka docker compose.

```
docker-compose build
```

Note, you shouldn't see any warnings or errors.

3. Startup the Docker Compose configuration

```
docker-compose up -d
```


Create CA 


# Generate CA key
```

openssl req -new -x509 -keyout ca.key -out ca.crt -days 365 -subj '/CN=ca1.test.fhir.ibm.com/OU=TEST/O=IBM FHIR SERVER/L=CAMBRIDGE/S=MA/C=US' -passin pass:change-password -passout pass:change-password

for i in broker1 broker2 producer consumer
do
    echo $i
    # Create keystores
    keytool -genkey -noprompt \
                 -alias $i \
                 -dname "CN=$i.test.fhir.ibm.com, OU=TEST, O=IBM FHIR SERVER, L=CAMBRIDGE, S=MA, C=US" \
                 -keystore kafka.$i.keystore.jks \
                 -keyalg RSA \
                 -storepass change-password \
                 -keypass change-password

    # Create CSR, sign the key and import back into keystore
    keytool -keystore kafka.$i.keystore.jks -alias $i -certreq -file $i.csr -storepass change-password -keypass change-password

    openssl x509 -req -CA ca.crt -CAkey ca.key -in $i.csr -out $i-ca1-signed.crt -days 9999 -CAcreateserial -passin pass:change-password

    keytool -keystore kafka.$i.keystore.jks -alias CARoot -import -file ca.crt -storepass change-password -keypass change-password -noprompt

    keytool -keystore kafka.$i.keystore.jks -alias $i -import -file $i-ca1-signed.crt -storepass change-password -keypass change-password -noprompt

    # Create truststore and import the CA cert.
    keytool -keystore kafka.$i.truststore.jks -alias 'Certificate Authority Root' -import -file ca.crt -storepass change-password -keypass change-password

  echo "change-password" > ${i}_sslkey_creds
  echo "change-password" > ${i}_keystore_creds
  echo "change-password" > ${i}_truststore_creds
done
```


https://hub.docker.com/r/confluentinc/cp-kafka/tags?page=1&ordering=last_updated
https://github.com/confluentinc/kafka-images/blob/master/examples/kafka-cluster-ssl/secrets/host.producer.ssl.config

export KAFKA_SSL_SECRETS_DIR="$(pwd)"
docker-compose up -d

https://github.com/confluentinc/kafka-images/tree/master/examples/kafka-cluster-ssl