# Plugin information

This plugin contains the JUnit tests used to check the correctness of the results computed by ePMC.
In order to perform the tests, the different plugins have to be included. 
This is obtained by creating an appropriate file inside the directory `src/test/resources/` whose name should follow the format
```
hostname[.domainname]_username.pluginlist
```

See the already present files for reference.
