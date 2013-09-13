This is the metridoc job to ingest illiad data into the metridoc repo.  First you will need the metridoc 
command line utility `mdoc` to run it.  To install `mdoc` in a bash environment, you can run:

```bash
curl -s https://raw.github.com/metridoc/metridoc-job-cli/master/src/etc/install-mdoc.sh | sh
```

for more details on `mdoc`, please see the [wiki](https://github.com/metridoc/metridoc-wiki/wiki) or the
`mdoc` [page](https://github.com/metridoc/metridoc-job-cli).

#### Installation and Running

You can either install one of the [releases](https://github.com/metridoc/metridoc-job-illiad/releases), or install the
code in its current state.  To install the code in its current form, do 

```bash
mdoc install https://github.com/metridoc/metridoc-job-illiad/archive/master.zip
```

To install a specific release, do


```bash
#replace the version number with the one you want
mdoc install https://github.com/metridoc/metridoc-job-illiad/archive/v0.1.0.zip
```

After you have installed the job run `mdoc list-jobs` to make sure it installed correctly and `mdoc help illiad` to get 
usage info.

#### Setting up the DataSource

The job needs to know where the Illiad and MetriDoc databases are.  The recommended aproach is to store the information 
in an external config file.  This can be done either use the `-config` flag or by putting everything into 
`~/.metridoc/MetridocConfig.groovy`.  If you are using the illiad 
[view](https://github.com/metridoc/metridoc-grails-illiad), using the central `MetridocConfig.groovy` file might be
the best approach so the two applications can share the connection parameters.  When editing the config file, the data 
sources would look something like:

```groovy
dataSource {
    pooled = true
    dbCreate = "update"
    url = "jdbc:mysql://localhost:3306/metridoc"
    driverClassName = "com.mysql.jdbc.Driver"
    dialect = MySQL5InnoDBDialect
    password = "password"
    username = "metridoc"
    properties {
        maxActive = -1
        minEvictableIdleTimeMillis = 1800000
        timeBetweenEvictionRunsMillis = 1800000
        numTestsPerEvictionRun = 3
        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = true
        validationQuery = "SELECT 1"
    }
}

dataSource_from_illiad {
    pooled = true
    driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    dbCreate = "none"
    username = "metridoc"
    password = "password"
    url = "jdbc:sqlserver://localhost:1433;databaseName=ILLData"
    properties {
        maxActive = -1
        minEvictableIdleTimeMillis = 1800000
        timeBetweenEvictionRunsMillis = 1800000
        numTestsPerEvictionRun = 3
        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = true
        validationQuery = "SELECT 1"
    }
}
```

After you have created the data sources, you can run `mdoc illiad --preview` to see if you can connect to them.
To run the job with its defaults, simply run `mdoc illiad`.  There are 2 flags you can add, `-startDate` and 
`-fiscalMonth`.  

* *startDate* - The date the job should start at for migrating data.  By default, it is the first day of the first 
fiscal month.

* *fiscalMonth* - The first month of the fiscal year, defaults to `July`


