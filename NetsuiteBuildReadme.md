1. build bom-pom-0.27.5-SNAPSHOT locally (components version changed back to 0.27.3, commons-compress changed back to 1.8.1, pax-url-aether version changed back to 2.4.7. diff with released version was used to identify changes)
2. build netsuite-definition 0.27.7 and create updatesite zip.
mvn clean install
mvn package -DskipTests -DskipITs -PgenerateP2
3. build netsuite-runtime 0.27.7
4. build netsuite-runtime_2014_2 0.27.7
5. build netsuite-runtime_2016_2 0.27.7
6. build netsuite-runtime_2018_2 0.27.7