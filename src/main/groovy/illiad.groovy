import metridoc.core.MetridocScript
import metridoc.core.tools.ParseArgsTool
import metridoc.illiad.DateUtil
import metridoc.illiad.IlliadTool
import metridoc.illiad.entities.IllFiscalStartMonth

use(MetridocScript) {
    //populate argsMap with cli info
    includeTool(ParseArgsTool)
    def month = "july"
    if(argsMap.containsKey("fiscalMonth")) {
        month = argsMap.fiscalMonth
        DateUtil.setMonth(month)
    }

    includeTool(IlliadTool).execute()
    IllFiscalStartMonth.updateMonth(month)
}