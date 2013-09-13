import metridoc.core.MetridocScript
import metridoc.core.tools.ConfigTool
import metridoc.core.tools.ParseArgsTool
import metridoc.illiad.DateUtil
import metridoc.illiad.IlliadTool
import metridoc.illiad.entities.IllFiscalStartMonth
import metridoc.utils.DataSourceConfigUtil

use(MetridocScript) {
    //populate argsMap with cli info
    includeTool(ParseArgsTool)

    if(argsMap.containsKey("preview")) {
        doPreview()
        return
    }

    def month = "july"
    if(argsMap.containsKey("fiscalMonth")) {
        month = argsMap.fiscalMonth
        DateUtil.setMonth(month)
    }

    includeTool(IlliadTool).execute()
    IllFiscalStartMonth.updateMonth(month)
}

def doPreview() {
    doConnect("dataSource")
    doConnect("dataSource_from_illiad")
}

def doConnect(String name) {
    use(MetridocScript) {
        //creates and binds config
        includeTool(ConfigTool)
    }

    def dataSource = DataSourceConfigUtil.getDataSource(config, name)

    try {
        dataSource.getConnection()
        println "INFO - Connected successfully to $name"
    }
    catch(Throwable throwable) {
        println "ERROR - Could not connect to $name"
        throw throwable
    }
}