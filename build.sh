./mvnw clean package -DskipTests=true  -Drevision=latest || exit
docker build -t mtls-server:latest .