package metridoc.illiad.entities

import metridoc.core.MetridocScript
import metridoc.tool.gorm.GormTool
import spock.lang.Specification

/**
 * Created with IntelliJ IDEA on 9/7/13
 * @author Tommy Barker
 */
class IllBorrowingSpec extends Specification {

    void "test methods"() {
        given:
        Binding binding = new Binding()
        use(MetridocScript) {
            def gorm = binding.includeTool(embeddedDataSource:true, GormTool)
            gorm.enableGormFor(IllBorrowing)
        }

        when:
        IllBorrowing.findAllByTransactionStatus(IllBorrowing.AWAITING_COPYRIGHT_CLEARANCE)

        then:
        noExceptionThrown()
    }
}
