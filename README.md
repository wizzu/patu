# patu

Ohjelma konekielisen Nordean tiliotteen lisäämiseen LedgerSMB-kirjanpitoon.


# Käyttö

Esimerkiksi:

```
$ cat nordea.nda | \
  java -cp patu.jar:postgresql-42.2.5.jre7.jar \
    fi.kivimaa.patu.Ropecon \
    jdbc:postgresql://HOSTNAME/DBNAME USERNAME PASSWORD \
    | tee nordea.out
```

Ohjelma vaatii PostgreSQL ajurin classpathiin asetettuna.


# Kääntäminen

Esimerkiksi:

```
mkdir -p build
cd src
find -name "*.java" > sources.txt
javac -d ../build @sources.txt
cd ../build
jar cvf ../patu.jar *
cd ..
```
