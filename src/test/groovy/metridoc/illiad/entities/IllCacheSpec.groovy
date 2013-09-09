package metridoc.illiad.entities

import spock.lang.Specification

/**
 * Created with IntelliJ IDEA on 9/7/13
 * @author Tommy Barker
 */
class IllCacheSpec extends Specification {

    void "test marshalling a map"() {
        given:
        def map = [foo:"bar", blah:[bam:"boom"]]

        when:
        IllCache.marshal(map)

        then:
        noExceptionThrown()
    }
}
