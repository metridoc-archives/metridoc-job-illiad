package metridoc.illiad

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import metridoc.core.tools.CamelTool
import metridoc.core.tools.RunnableTool
import metridoc.illiad.entities.IllGroup
import metridoc.illiad.entities.IllLenderGroup
import metridoc.illiad.entities.IllLendingTracking
import metridoc.illiad.entities.IllTracking
import metridoc.service.gorm.GormService
import metridoc.utils.DataSourceConfigUtil

import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat

/**
 * Created with IntelliJ IDEA on 9/6/13
 * @author Tommy Barker
 */
@Slf4j
class IlliadService extends RunnableTool {

    Sql sql
    Sql fromIlliadSql
    DataSource dataSource
    DataSource dataSource_from_illiad
    GormService gormService

    def fromIlliadSqlStatements = new IlliadMsSqlQueries()
    def toIlliadSqlStatements = new IlliadMysqlQueries()
    def illiadHelper = new IlliadHelper(illiadTool: this)

    def _lenderTableName
    def _userTableName
    String startDate
    static final LENDER_ADDRESSES_ALL = "LenderAddressesAll"
    static final LENDER_ADDRESSES = "LenderAddresses"
    static final USERS = "Users"
    static final USERS_ALL = "UsersAll"
    public static final String OTHER = "Other"

    List illiadTables = [
            "ill_group",
            "ill_lending",
            "ill_borrowing",
            "ill_user_info",
            "ill_transaction",
            "ill_lender_info",
            "ill_lender_group",
            "ill_lending_tracking",
            "ill_location",
            "ill_reference_number",
            "ill_tracking"
    ]

    @Override
    def configure() {

        target(runFullWorkflow: "running full workflow") {
            depends("initializeDb",
                    "truncateLoadingTables",
                    "migrateData",
                    "migrateBorrowingDataToIllTracking",
                    "doUpdateBorrowing",
                    "doUpdateLending",
                    "doIllGroupOtherInsert",
                    "cleanUpIllTransactionLendingLibraries",
                    "updateCache"
            )
        }

        target(initializeDb: "initializing Gorm objects, db, etc.") {
            //this will ensure that we are using the same dataSource that gorm uses
            dataSource = gormService.applicationContext.getBean(DataSource)
            dataSource_from_illiad = DataSourceConfigUtil.getDataSource(binding.config, "dataSource_from_illiad")
            fromIlliadSql = new Sql(dataSource_from_illiad)
        }

        target(truncateLoadingTables: "truncating loading tables") {
            illiadTables.each {
                log.info "truncating table ${it} in the repository"
                getSql().execute("truncate ${it}" as String)
            }
        }

        target(migrateData: "migrates data from illiad to repository instance") {
            log.info "migrating data to ${dataSource.connection.metaData.getURL()}"
            def camelTool = includeTool(CamelTool)
            camelTool.bind("dataSource", dataSource)
            camelTool.bind("dataSource_from_illiad", dataSource_from_illiad)

            [
                    ill_group: fromIlliadSqlStatements.groupSqlStmt,
                    ill_lender_group: fromIlliadSqlStatements.groupLinkSqlStmt,
                    ill_lender_info: fromIlliadSqlStatements.lenderAddrSqlStmt(lenderTableName as String),
                    ill_reference_number: fromIlliadSqlStatements.referenceNumberSqlStmt,
                    ill_transaction: fromIlliadSqlStatements.transactionSqlStmt(getStartDate()),
                    ill_lending: fromIlliadSqlStatements.lendingSqlStmt(getStartDate()),
                    ill_borrowing: fromIlliadSqlStatements.borrowingSqlStmt(getStartDate()),
                    ill_user_info: fromIlliadSqlStatements.userSqlStmt(userTableName as String)

            ].each { key, value ->
                log.info("migrating to ${key} using \n    ${value}" as String)
                camelTool.with {
                    consumeNoWait("sqlplus:${value}?dataSource=dataSource_from_illiad") { ResultSet resultSet ->
                        send("sqlplus:${key}?dataSource=dataSource", resultSet)
                    }
                }
            }
        }

        target(migrateBorrowingDataToIllTracking: "migrates data from illborrowing to ill_tracking") {
            depends("initializeDb")
            IllTracking.updateFromIllBorrowing()
        }

        target(doUpdateBorrowing: "updates the borrowing tables") {
            [
                    fromIlliadSqlStatements.orderDateSqlStmt,
                    fromIlliadSqlStatements.shipDateSqlStmt,
                    fromIlliadSqlStatements.receiveDateSqlStmt,
                    fromIlliadSqlStatements.articleReceiveDateSqlStmt
            ].each {
                log.info "update borrowing with sql statement $it"
                getSql().execute(it as String)
            }
        }

        target(doUpdateLending: "updates the lending table") {
            [
                    fromIlliadSqlStatements.arrivalDateSqlStmt,
                    fromIlliadSqlStatements.completionSqlStmt,
                    fromIlliadSqlStatements.shipSqlStmt,
                    fromIlliadSqlStatements.cancelledSqlStmt
            ].each {
                log.info "updating lending with sql statement $it"
                getSql().execute(it as String)
            }
        }

        target(doIllGroupOtherInsert: "inserts extra records into ill_group to deal with 'OTHER'") {
            IllGroup.withNewTransaction {
                new IllGroup(groupNo: IlliadHelper.GROUP_ID_OTHER, groupName: OTHER).save(failOnError: true)
                new IllLenderGroup(groupNo: IlliadHelper.GROUP_ID_OTHER, lenderCode: OTHER).save(failOnError: true)
            }
        }

        target(cleanUpIllTransactionLendingLibraries: "cleans up data in ill_transaction, ill_lending_tracking and ill_tracking to facilitate agnostic sql queries in the dashboard") {

            getSql().withTransaction {
                int updates
                updates = getSql().executeUpdate("update ill_transaction set lending_library = 'Other' where lending_library is null")
                log.info "changing all lending_library entries in ill_transaction from null to other caused $updates updates"
                updates = getSql().executeUpdate("update ill_transaction set lending_library = 'Other' where lending_library not in (select distinct lender_code from ill_lender_group)")
                log.info "changing all lending_library entries in ill_transaction that are not in ill_lender_group to other caused $updates updates"
            }

            IllTracking.updateTurnAroundsForAllRecords()
            IllLendingTracking.updateTurnAroundsForAllRecords()
        }

        target(updateCache: "updates reporting cache") {
            depends("initializeDb")
            illiadHelper.storeCache()
        }

        target(dropTables: "drops illiad tables") {
            illiadTables.each {
                getSql().execute("drop table $it" as String)
            }
        }

        setDefaultTarget("runFullWorkflow")
    }

    Sql getSql() {
        if (sql) return sql
        if (getDataSource()) {
            sql = new Sql(dataSource as DataSource)
        }

        return sql
    }

    def getLenderTableName() {
        if (_lenderTableName) return _lenderTableName

        _lenderTableName = pickTable(LENDER_ADDRESSES_ALL, LENDER_ADDRESSES)
    }

    def getUserTableName() {
        if (_userTableName) return _userTableName

        _userTableName = pickTable(USERS, USERS_ALL)
    }

    String getStartDate() {
        if (startDate) return startDate

        def formatter = new SimpleDateFormat('yyyyMMdd')
        def fiscalYear = DateUtil.currentFiscalYear
        def startDateAsDate = DateUtil.getFiscalYearStartDate(fiscalYear)

        startDate = formatter.format(startDateAsDate)
    }

    private pickTable(option1, option2) {
        if (tableExists(option1)) {
            return option1
        }
        else {
            return option2
        }
    }

    private tableExists(tableName) {
        try {
            getFromIlliadSql().execute("select count(*) from $tableName" as String)
            return true
        }
        catch (SQLException ignored) {
            //table does not exist
            return false
        }
    }
}
