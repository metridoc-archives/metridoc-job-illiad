package metridoc.illiad

import spock.lang.Specification

/**
 * Created with IntelliJ IDEA on 9/9/13
 * @author Tommy Barker
 */
class DateUtilsSpec extends Specification{

    def cleanup() {
        DateUtil.FY_START_MONTH = Calendar.JULY
    }

    void "test standard fiscal years"() {
        given:
        DateUtil.FY_START_MONTH = Calendar.JULY

        expect:
        a == DateUtil.getFiscalYear(b, c)

        where:
        a    | b    | c
        2012 | 2012 | Calendar.JANUARY
        2012 | 2011 | Calendar.DECEMBER
        2005 | 2004 | Calendar.JULY
    }

    void "testing getting fiscal year with defaults"() {
        given:
        DateUtil.FY_START_MONTH = Calendar.JANUARY

        expect:
        a == DateUtil.getFiscalYear(b, c)

        where:
        a    | b    | c
        2012 | 2012 | Calendar.DECEMBER
        2012 | 2012 | Calendar.JANUARY
    }

    void "test difference by days"() {
        given:
        def now = new Date()

        when:
        def littleBitInFuture = new Date(new Date().time + DateUtil.ONE_DAY + (Long) (DateUtil.ONE_DAY / 2))
        double difference = DateUtil.differenceByDays(littleBitInFuture, now)

        then:
        Math.abs(difference - 1.5) < 0.001 //since we are dealing with decimals it wont be perfect
    }

    void "test valid month"() {
        when: "month is invalid"
        boolean valid = DateUtil.isValidMonth("foo")

        then:
        !valid

        when:
        valid = DateUtil.isValidMonth("june")

        then:
        valid

        when:
        valid = DateUtil.isValidMonth("June")

        then:
        valid
    }
}
