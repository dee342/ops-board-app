Feature: Person Detachment Validations


Scenario: Test detachment validation  1

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150325' to '22150328'
And then detached back to 'BX01' from '22150326' to '22150326'
#Then You 'should' be able to detach from 'BX01' starting '22150326' to '22150326', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150324' to '22150324', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150323' to '22150324', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150323' to '22150325', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150325' to '22150325', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150325' to '22150326', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150327' to '22150327', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150328' to '22150328', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150325' to '22150328', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150326' to '22150328', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150326' to '22150329', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150323' to '22150329', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150329' to '22150329', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150330' to '22150330', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150329' to '22150330', current day being Today
Then You 'should' be able to detach from 'BX02' starting '22150325' to '22150325', current day being Today
Then You 'should' be able to detach from 'BX02' starting '22150327' to '22150327', current day being Today
Then You 'should' be able to detach from 'BX02' starting '22150328' to '22150328', current day being Today
Then You 'should' be able to detach from 'BX02' starting '22150327' to '22150328', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150329' to '22150329', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150329' to '22150330', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150323' to '22150330', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150326' to '22150326', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150324' to '22150324', current day being Today

Scenario: Test detachment validation  2

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150320' to '22150328'
And then detached back to 'BX01' from '22150320' to '22150328'
Then You 'should' be able to detach from 'BX01' starting '22150320' to '22150328', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150319' to '22150329', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150329' to '22150330', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150320' to '22150328', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150319' to '22150329', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150329' to '22150330', current day being Today


Scenario: Test detachment validation  3

Given a person with id '10059' at homeLocation 'BX01'
Then You 'should' be able to detach from 'BX01' starting '22150320' to '22150328', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150319' to '22150329', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150329' to '22150330', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150320' to '22150328', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150319' to '22150329', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150329' to '22150330', current day being Today


Scenario: Test detachment validation  4

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150325' to '22150328'
And that person is detached to 'BX03' from '22150326' to '22150326'
Then You 'should' be able to detach from 'BX01' starting '22150324' to '22150324', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150323' to '22150324', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150329' to '22150329', current day being Today
Then You 'should' be able to detach from 'BX01' starting '22150329' to '22150330', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150325' to '22150325', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150323' to '22150326', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150325' to '22150330', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150325' to '22150328', current day being Today
Then You 'should' be able to detach from 'BX03' starting '22150326' to '22150326', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150326' to '22150326', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150324' to '22150329', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150325' to '22150328', current day being Today
Then You 'should' be able to detach from 'BX02' starting '22150327' to '22150327', current day being Today

Scenario: Test detachment validation  5

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150320' to '22150328'
And that person is detached to 'BX03' from '22150320' to '22150322'
And that person is detached to 'BX04' from '22150323' to '22150327'
And then detached back to 'BX01' from '22150322' to '22150322'
And then detached back to 'BX01' from '22150323' to '22150323'
Then You 'should' be able to detach from 'BX01' starting '22150322' to '22150323', current day being Today

Scenario: Test detachment validation  6

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' to '22150423'
Then You 'should not' be able to detach from 'BX01' starting '22150423' to '22150423', current day being Today

Scenario: Test detachment validation  7

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' to '22150423'
Then You 'should not' be able to detach from 'BX01' starting '22150423' to '22150423', current day being '22150423'


Scenario: Test detachment validation  8

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' to '22150423'
Then You 'should not' be able to detach from 'BX01' starting '22150423' to ''



Scenario: Test detachment validation  9

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' to '22150423'
Then You 'should not' be able to detach from 'BX01' starting '22150423' permanently, current day being '22150423'

Scenario: Test detachment validation  10

Given a person with id '10059' at homeLocation 'BX01'
Then You 'should' be able to detach from 'BX01' starting '22150423' permanently, current day being '22150423'


Scenario: Test detachment validation  11

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' permanently
Then You 'should not' be able to detach from 'BX01' starting '22150423' permanently, current day being '22150423'

Scenario: Test detachment validation  12

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' permanently
Then You 'should' be able to detach from 'BX02' starting '22150423' permanently, current day being '22150423'

Scenario: Test detachment validation  13

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' permanently
Then You 'should not' be able to detach from 'BX01' starting '22150423' to '22150423', current day being '22150423'

Scenario: Test detachment validation  14

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' permanently
When that person is detached to 'BX01' from '22150423' permanently
Then You 'should not' be able to detach from 'BX02' starting '22150423' permanently, current day being '22150423'
Then You 'should' be able to detach from 'BX01' starting '22150423' permanently, current day being '22150423'
Then You 'should not' be able to detach from 'BX03' starting '22150423' permanently, current day being '22150423'


Scenario: Test detachment validation  15

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' permanently
When that person is detached to 'BX01' from '22150425' permanently
Then You 'should' be able to detach from 'BX02' starting '22150423' to '22150424', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150423' permanently, current day being '22150423'
Then You 'should not' be able to detach from 'BX02' starting '22150424' permanently, current day being '22150423'
Then You 'should not' be able to detach from 'BX01' starting '22150423' to '22150423', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150424' to '22150424', current day being Today
Then You 'should not' be able to detach from 'BX01' starting '22150423' to '22150424', current day being Today

Scenario: Test detachment validation  16

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150423' permanently
When that person is detached to 'BX01' from '22150424' permanently
Then You 'should' be able to detach from 'BX02' starting '22150423' to '22150423', current day being Today
Then You 'should not' be able to detach from 'BX02' starting '22150423' permanently, current day being '22150423'
Then You 'should' be able to detach from 'BX01' starting '22150424' permanently, current day being '22150423'
#Then You 'should not' be able to detach from 'BX01' starting '22150423' to '22150423', current day being '22150423' 

Scenario: Test detachment validation  17

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '22150320' permanently
And that person is detached to 'BX01' from '22150320' permanently
Then You 'should' be able to detach from 'BX01' starting '22150320' to '22150328', current day being Today