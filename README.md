This is the metridoc job to ingest illiad data into the metridoc repo.  First you will need the metridoc 
command line utility `mdoc` to run it.  To install `mdoc` in a bash environment, you can run:

```bash
curl -s https://raw.github.com/metridoc/metridoc-job-cli/master/src/etc/install-mdoc.sh | sh
```

for more details on `mdoc`, please see the [wiki](https://github.com/metridoc/metridoc-wiki/wiki) or the
`mdoc` [page](https://github.com/metridoc/metridoc-job-cli).

#### Installation

You can either install one of the [releases](https://github.com/metridoc/metridoc-job-illiad/releases), or install the
code in its current state.  To install the code in its current form, do 

```bash
mdoc install https://github.com/metridoc/metridoc-job-illiad/archive/master.zip
```

To install a specific release, do


```bash
coming soon
```

#### Setting up the DataSource

The job needs to know where the Illiad and MetriDoc database is.  The recommended aproach is to store the information in 
an external config file.




