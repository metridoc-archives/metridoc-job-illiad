/*
 * Copyright 2013 Trustees of the University of Pennsylvania Licensed under the
 * 	Educational Community License, Version 2.0 (the "License"); you may
 * 	not use this file except in compliance with the License. You may
 * 	obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * 	Unless required by applicable law or agreed to in writing,
 * 	software distributed under the License is distributed on an "AS IS"
 * 	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * 	or implied. See the License for the specific language governing
 * 	permissions and limitations under the License.
 */



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
